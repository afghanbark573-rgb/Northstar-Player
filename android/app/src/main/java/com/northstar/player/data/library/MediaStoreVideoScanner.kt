package com.northstar.player.data.library

import android.content.ContentUris
import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.northstar.player.core.database.SafStagedMediaEntity
import com.northstar.player.core.database.ScanStateEntity
import com.northstar.player.core.database.SafSnapshotDao
import com.northstar.player.core.permissions.VideoPermissionScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

data class MediaScanResult(
    val discoveredCount: Int,
    val permissionScope: VideoPermissionScope,
    val completedAtMillis: Long
)

/**
 * Reads Android's indexed video collection and stages bounded batches in Room.
 * A complete query is published atomically; failed or cancelled queries leave the
 * previous visible snapshot intact. PARTIAL means the exact set currently visible
 * under Android's selected-media grant, not an additive hint.
 */
class MediaStoreVideoScanner(
    private val resolver: ContentResolver,
    private val repository: MediaLibraryRepository,
    private val snapshotDao: SafSnapshotDao,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val scanMutex = Mutex()

    suspend fun scan(permissionScope: VideoPermissionScope): Result<MediaScanResult> =
        scanMutex.withLock {
            withContext(Dispatchers.IO) {
                val sourceKey = SOURCE_KEY
                val previousState = repository.getScanState(sourceKey)
                val previousSuccess = previousState?.lastSuccessfulScanMillis

                if (permissionScope == VideoPermissionScope.DENIED) {
                    repository.markSourceUnavailable(sourceKey)
                    repository.refreshFolderIndex(
                        sourceKey,
                        VideoPermissionScope.PARTIAL,
                        clock()
                    )
                    repository.saveScanState(
                        ScanStateEntity(
                            sourceKey = sourceKey,
                            lastSuccessfulScanMillis = previousSuccess,
                            permissionScope = permissionScope.name,
                            inProgressSinceMillis = null,
                            lastError = "Video permission is not granted"
                        )
                    )
                    return@withContext Result.failure(
                        SecurityException("Video permission is not granted")
                    )
                }

                val latestStaged = snapshotDao.latestStagedGenerationMillis(sourceKey) ?: 0L
                val generation = maxOf(
                    clock(),
                    nextTimestamp(previousSuccess),
                    nextTimestamp(latestStaged)
                )
                repository.saveScanState(
                    ScanStateEntity(
                        sourceKey = sourceKey,
                        lastSuccessfulScanMillis = previousSuccess,
                        permissionScope = permissionScope.name,
                        inProgressSinceMillis = generation,
                        lastError = null
                    )
                )

                try {
                    val count = queryAndStage(generation)
                    currentCoroutineContext().ensureActive()

                    // The staged set is the exact accessible snapshot for both FULL
                    // and PARTIAL permission. Missing rows are hidden, never deleted.
                    snapshotDao.publishMediaStoreSnapshot(sourceKey, generation)

                    val completedAt = clock()
                    // Once the media snapshot commits, finish its folder projection and
                    // success marker even if the caller is cancelled during UI teardown.
                    withContext(NonCancellable) {
                        repository.refreshFolderIndex(sourceKey, permissionScope, completedAt)
                        repository.saveScanState(
                            ScanStateEntity(
                                sourceKey = sourceKey,
                                lastSuccessfulScanMillis = generation,
                                permissionScope = permissionScope.name,
                                inProgressSinceMillis = null,
                                lastError = null
                            )
                        )
                    }
                    Result.success(MediaScanResult(count, permissionScope, completedAt))
                } catch (cancelled: CancellationException) {
                    withContext(NonCancellable) {
                        runCatching { snapshotDao.clearStagedForSource(sourceKey) }
                        runCatching {
                            repository.saveScanState(
                                ScanStateEntity(
                                    sourceKey, previousSuccess, permissionScope.name,
                                    null, "Scan cancelled"
                                )
                            )
                        }
                    }
                    throw cancelled
                } catch (error: Exception) {
                    withContext(NonCancellable) {
                        runCatching { snapshotDao.clearStagedForSource(sourceKey) }
                        runCatching {
                            repository.saveScanState(
                                ScanStateEntity(
                                    sourceKey,
                                    previousSuccess,
                                    permissionScope.name,
                                    null,
                                    error.message ?: error.javaClass.simpleName
                                )
                            )
                        }
                    }
                    Result.failure(error)
                }
            }
        }

    private suspend fun queryAndStage(generationMillis: Long): Int {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.RELATIVE_PATH
        )
        var discovered = 0
        val batch = ArrayList<SafStagedMediaEntity>(BATCH_SIZE)

        resolver.query(collection, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
            val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
            val addedColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
            val modifiedColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
            val pathColumn = cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {
                currentCoroutineContext().ensureActive()
                val uri = ContentUris.withAppendedId(collection, cursor.getLong(idColumn))
                batch += SafStagedMediaEntity(
                    sourceKey = SOURCE_KEY,
                    generationMillis = generationMillis,
                    mediaKey = uri.toString(),
                    uri = uri.toString(),
                    displayName = cursor.stringOrNull(nameColumn) ?: "Untitled video",
                    mimeType = cursor.stringOrNull(mimeColumn),
                    durationMs = cursor.longOrZero(durationColumn),
                    sizeBytes = cursor.longOrZero(sizeColumn),
                    dateAddedSeconds = cursor.longOrNull(addedColumn),
                    dateModifiedSeconds = cursor.longOrNull(modifiedColumn),
                    sourceType = SOURCE_TYPE,
                    relativePath = cursor.stringOrNull(pathColumn)?.trimEnd('/')
                )
                discovered++
                if (batch.size >= BATCH_SIZE) {
                    snapshotDao.stageMedia(batch.toList())
                    batch.clear()
                }
            }
        } ?: throw IOException("Android MediaStore did not return a video cursor")

        currentCoroutineContext().ensureActive()
        if (batch.isNotEmpty()) snapshotDao.stageMedia(batch)
        return discovered
    }

    private fun nextTimestamp(value: Long?): Long = when {
        value == null || value <= 0L -> 0L
        value == Long.MAX_VALUE -> Long.MAX_VALUE
        else -> value + 1L
    }

    private fun Cursor.stringOrNull(index: Int): String? =
        if (index < 0 || isNull(index)) null else getString(index)

    private fun Cursor.longOrNull(index: Int): Long? =
        if (index < 0 || isNull(index)) null else getLong(index)

    private fun Cursor.longOrZero(index: Int): Long =
        longOrNull(index) ?: 0L

    companion object {
        const val SOURCE_TYPE = "MEDIA_STORE"
        const val SOURCE_KEY = "mediastore:external"
        private const val BATCH_SIZE = 400
    }
}
