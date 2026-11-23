package com.northstar.player.data.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.database.Cursor
import com.northstar.player.core.database.ExternalTreeGrantDao
import com.northstar.player.core.database.ExternalTreeGrantEntity
import com.northstar.player.core.database.FolderDao
import com.northstar.player.core.database.FolderEntity
import com.northstar.player.core.database.MediaItemDao
import com.northstar.player.core.database.MediaItemEntity
import com.northstar.player.core.database.ScanStateDao
import com.northstar.player.core.database.ScanStateEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.MessageDigest
import java.util.ArrayDeque

data class SafTreeScanResult(
    val sourceKey: String,
    val discoveredVideos: Int,
    val discoveredFolders: Int,
    val completedAtMillis: Long
)

/** Stable, privacy-preserving source identity. The raw tree URI stays in the grant table. */
object SafSourceIdentity {
    fun key(treeUri: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(treeUri.toByteArray(Charsets.UTF_8))
        return "saf:" + digest.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Recursively indexes a user-granted SAF tree.
 *
 * Media rows are written in bounded batches. Old rows are marked unavailable only after
 * the entire traversal succeeds, so a failed/disconnected scan cannot erase the last
 * known-good library snapshot.
 */
class SafTreeScanner(
    private val resolver: ContentResolver,
    private val mediaDao: MediaItemDao,
    private val folderDao: FolderDao,
    private val scanStateDao: ScanStateDao,
    private val grantDao: ExternalTreeGrantDao,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val scanMutex = Mutex()

    private data class PendingFolder(
        val documentId: String,
        val displayName: String,
        val relativePath: String,
        val folderKey: String,
        val ancestorIds: List<String>
    )

    private data class MutableFolder(
        val documentId: String,
        val displayName: String,
        val relativePath: String,
        val folderKey: String,
        val parentFolderKey: String?,
        var directVideoCount: Int = 0,
        var totalVideoCount: Int = 0,
        var childFolderCount: Int = 0
    )

    suspend fun scan(grant: ExternalTreeGrantEntity): Result<SafTreeScanResult> =
        scanMutex.withLock {
        withContext(Dispatchers.IO) {
            val treeUri = Uri.parse(grant.treeUri)
            val persisted = resolver.persistedUriPermissions.any {
                it.uri == treeUri && it.isReadPermission
            }
            if (!persisted) {
                grantDao.setAvailability(grant.treeUri, false)
                return@withContext Result.failure(SecurityException("Folder permission is no longer available"))
            }

            val sourceKey = SafSourceIdentity.key(grant.treeUri)
            val previous = scanStateDao.get(sourceKey)
            val scanStartedAt = maxOf(clock(), (previous?.lastSuccessfulScanMillis ?: 0L) + 1L)
            scanStateDao.save(
                ScanStateEntity(sourceKey, previous?.lastSuccessfulScanMillis, "SAF_TREE",
                    scanStartedAt, null)
            )

            try {
                val rootDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
                val rootKey = "$sourceKey|$rootDocumentId"
                val root = MutableFolder(
                    documentId = rootDocumentId,
                    displayName = grant.displayName,
                    relativePath = "",
                    folderKey = rootKey,
                    parentFolderKey = null
                )
                val folders = linkedMapOf(rootDocumentId to root)
                val queue = ArrayDeque<PendingFolder>()
                queue.add(PendingFolder(rootDocumentId, grant.displayName, "", rootKey, listOf(rootDocumentId)))
                val visited = mutableSetOf(rootDocumentId)
                val batch = ArrayList<MediaItemEntity>(BATCH_SIZE)
                var videoCount = 0

                while (queue.isNotEmpty()) {
                    currentCoroutineContext().ensureActive()
                    val parent = queue.removeFirst()
                    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                        treeUri, parent.documentId
                    )
                    resolver.query(childrenUri, PROJECTION, null, null, null)?.use { cursor ->
                        val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        val nameColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                        val mimeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                        val sizeColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                        val modifiedColumn = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                        while (cursor.moveToNext()) {
                            currentCoroutineContext().ensureActive()
                            val documentId = cursor.getString(idColumn) ?: continue
                            if (!visited.add(documentId)) continue
                            val name = cursor.stringOrNull(nameColumn) ?: "Untitled"
                            val mime = cursor.stringOrNull(mimeColumn)
                            if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                                val path = if (parent.relativePath.isBlank()) name else "${parent.relativePath}/$name"
                                val folderKey = "$sourceKey|$documentId"
                                val folder = MutableFolder(
                                    documentId, name, path, folderKey, parent.folderKey
                                )
                                folders[documentId] = folder
                                folders[parent.documentId]?.childFolderCount =
                                    (folders[parent.documentId]?.childFolderCount ?: 0) + 1
                                queue.add(
                                    PendingFolder(
                                        documentId, name, path, folderKey,
                                        parent.ancestorIds + documentId
                                    )
                                )
                            } else if (isVideo(name, mime)) {
                                val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                                val size = cursor.longOrNull(sizeColumn)?.takeIf { it >= 0L } ?: 0L
                                val modified = cursor.longOrNull(modifiedColumn)
                                    ?.takeIf { it > 0L }?.div(1000L)
                                batch += MediaItemEntity(
                                    mediaKey = "$sourceKey|$documentId",
                                    uri = documentUri.toString(),
                                    displayName = name,
                                    mimeType = mime,
                                    durationMs = 0L,
                                    sizeBytes = size,
                                    dateAddedSeconds = null,
                                    dateModifiedSeconds = modified,
                                    sourceType = SOURCE_TYPE,
                                    sourceKey = sourceKey,
                                    relativePath = parent.relativePath,
                                    isAvailable = true,
                                    indexedAtMillis = scanStartedAt
                                )
                                parent.ancestorIds.forEach { ancestorId ->
                                    folders[ancestorId]?.let { it.totalVideoCount++ }
                                }
                                folders[parent.documentId]?.let { it.directVideoCount++ }
                                videoCount++
                                if (batch.size >= BATCH_SIZE) {
                                    mediaDao.upsertAll(batch.toList())
                                    batch.clear()
                                }
                            }
                        }
                    } ?: throw IOException("Storage provider returned no folder listing for ${parent.displayName}")
                }

                currentCoroutineContext().ensureActive()
                if (batch.isNotEmpty()) mediaDao.upsertAll(batch)

                // Reconcile only after a complete traversal. Keep NEW/acknowledgement history.
                val previousFolders = folderDao.findForSource(sourceKey).associateBy { it.folderKey }
                val indexedFolders = folders.values.map { folder ->
                    val old = previousFolders[folder.folderKey]
                    FolderEntity(
                        folderKey = folder.folderKey,
                        parentFolderKey = folder.parentFolderKey,
                        displayName = folder.displayName,
                        sourceKey = sourceKey,
                        relativePath = folder.relativePath,
                        containsVideos = folder.directVideoCount > 0,
                        videoCount = folder.totalVideoCount,
                        childFolderCount = folder.childFolderCount,
                        discoveredAtMillis = old?.discoveredAtMillis ?: scanStartedAt,
                        acknowledgedAtMillis = old?.acknowledgedAtMillis
                    )
                }
                mediaDao.markOlderItemsUnavailable(sourceKey, scanStartedAt)
                folderDao.replaceForSource(sourceKey, indexedFolders)
                scanStateDao.save(
                    ScanStateEntity(sourceKey, scanStartedAt, "SAF_TREE", null, null)
                )
                grantDao.setAvailability(grant.treeUri, true)
                Result.success(
                    SafTreeScanResult(sourceKey, videoCount, indexedFolders.size, clock())
                )
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) {
                    scanStateDao.save(
                        ScanStateEntity(sourceKey, previous?.lastSuccessfulScanMillis, "SAF_TREE",
                            null, "Scan cancelled")
                    )
                }
                throw cancelled
            } catch (error: Exception) {
                if (error is SecurityException || error is IOException) {
                    grantDao.setAvailability(grant.treeUri, false)
                    mediaDao.markSourceUnavailable(sourceKey)
                    folderDao.deleteForSource(sourceKey)
                }
                scanStateDao.save(
                    ScanStateEntity(sourceKey, previous?.lastSuccessfulScanMillis, "SAF_TREE",
                        null, error.message ?: error.javaClass.simpleName)
                )
                Result.failure(error)
            }
        }
        }

    private fun isVideo(name: String, mime: String?): Boolean {
        if (mime?.startsWith("video/", ignoreCase = true) == true) return true
        return name.substringAfterLast('.', "").lowercase() in VIDEO_EXTENSIONS
    }

    private fun Cursor.stringOrNull(index: Int): String? =
        if (index < 0 || isNull(index)) null else getString(index)

    private fun Cursor.longOrNull(index: Int): Long? =
        if (index < 0 || isNull(index)) null else getLong(index)

    companion object {
        const val SOURCE_TYPE = "SAF_TREE"
        private const val BATCH_SIZE = 400
        private val PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )
        private val VIDEO_EXTENSIONS = setOf(
            "3gp", "asf", "avi", "flv", "mkv", "mov", "mp4", "mpeg", "mpg",
            "mts", "m2ts", "ogv", "ts", "webm", "wmv"
        )
    }
}
