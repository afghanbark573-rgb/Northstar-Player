package com.northstar.player.data.library

import android.content.ContentUris
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
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
    val completedAtMillis: Long,
    val incremental: Boolean = false
)

private data class MediaStoreGeneration(
    val version: String,
    val generation: Long
)

/**
 * Reads Android's indexed video collection and stages bounded batches in Room.
 *
 * API 30+ can use GENERATION_MODIFIED when full permission and a matching MediaStore
 * version/checkpoint are available. Partial access, older Android versions, periodic
 * reconciliation, and invalidated checkpoints always use a complete accessible snapshot.
 * Complete snapshots are what reconcile deletions; incremental runs upsert additions and
 * modifications, while the scheduled full reconciliation catches removals.
 */
class MediaStoreVideoScanner(
    private val context: Context,
    private val resolver: ContentResolver,
    private val repository: MediaLibraryRepository,
    private val snapshotDao: SafSnapshotDao,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val scanMutex = Mutex()

    suspend fun scan(
        permissionScope: VideoPermissionScope,
        allowIncremental: Boolean = false
    ): Result<MediaScanResult> = scanMutex.withLock {
        withContext(Dispatchers.IO) {
            val sourceKey = SOURCE_KEY
            val previousState = repository.getScanState(sourceKey)
            val previousSuccess = previousState?.lastSuccessfulScanMillis

            if (permissionScope == VideoPermissionScope.DENIED) {
                snapshotDao.clearStagedForSource(sourceKey)
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
                        lastError = "Video permission is not granted",
                        mediaStoreVersion = previousState?.mediaStoreVersion,
                        mediaStoreGeneration = previousState?.mediaStoreGeneration
                    )
                )
                return@withContext Result.failure(
                    SecurityException("Video permission is not granted")
                )
            }

            val generationSnapshot = readGenerationSnapshot()
            val incremental = MediaStoreScanPolicy.shouldUseIncremental(
                apiLevel = Build.VERSION.SDK_INT,
                permissionScope = permissionScope,
                previousState = previousState,
                currentVersion = generationSnapshot?.version,
                currentGeneration = generationSnapshot?.generation,
                forceFull = !allowIncremental
            )
            val scanTimestamp = nextTimestamp(
                maxOf(
                    clock(),
                    previousSuccess ?: 0L,
                    snapshotDao.latestStagedGenerationMillis(sourceKey) ?: 0L
                )
            )

            repository.saveScanState(
                ScanStateEntity(
                    sourceKey = sourceKey,
                    lastSuccessfulScanMillis = previousSuccess,
                    permissionScope = permissionScope.name,
                    inProgressSinceMillis = scanTimestamp,
                    lastError = null,
                    mediaStoreVersion = previousState?.mediaStoreVersion,
                    mediaStoreGeneration = previousState?.mediaStoreGeneration
                )
            )

            try {
                var usedIncremental = incremental
                val count = if (usedIncremental) {
                    try {
                        queryAndStage(
                            generationMillis = scanTimestamp,
                            modifiedAfterGeneration = previousState!!.mediaStoreGeneration,
                            modifiedThroughGeneration = generationSnapshot!!.generation
                        )
                    } catch (incrementalError: Exception) {
                        if (incrementalError is CancellationException) throw incrementalError
                        // A provider may advertise generation support but reject the delta
                        // query. Discard its partial stage and recover with a full snapshot.
                        snapshotDao.clearStagedForSource(sourceKey)
                        usedIncremental = false
                        queryAndStage(scanTimestamp)
                    }
                } else {
                    queryAndStage(scanTimestamp)
                }
                currentCoroutineContext().ensureActive()

                val completedAt = clock()
                val successfulState = ScanStateEntity(
                    sourceKey = sourceKey,
                    lastSuccessfulScanMillis = scanTimestamp,
                    permissionScope = permissionScope.name,
                    inProgressSinceMillis = null,
                    lastError = null,
                    mediaStoreVersion = generationSnapshot?.version,
                    // The token is captured before querying. Any media changes during the
                    // query therefore remain newer than this checkpoint and are picked up
                    // by the next incremental reconciliation.
                    mediaStoreGeneration = generationSnapshot?.generation
                )

                if (usedIncremental) {
                    if (count > 0) {
                        snapshotDao.publishMediaStoreDelta(
                            sourceKey,
                            scanTimestamp,
                            successfulState
                        )
                    } else {
                        snapshotDao.clearStagedForSource(sourceKey)
                        repository.saveScanState(successfulState)
                    }
                } else {
                    snapshotDao.publishMediaStoreFullSnapshot(
                        sourceKey,
                        scanTimestamp,
                        successfulState
                    )
                }

                // Once the media snapshot/delta commits, finish its folder projection even
                // if the Activity or WorkManager caller is cancelled during teardown.
                withContext(NonCancellable) {
                    if (!usedIncremental || count > 0) {
                        repository.refreshFolderIndex(sourceKey, permissionScope, completedAt)
                    }
                }
                Result.success(
                    MediaScanResult(count, permissionScope, completedAt, usedIncremental)
                )
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) {
                    runCatching { snapshotDao.clearStagedForSource(sourceKey) }
                    runCatching {
                        repository.saveScanState(
                            ScanStateEntity(
                                sourceKey, previousSuccess, permissionScope.name,
                                null, "Scan cancelled",
                                previousState?.mediaStoreVersion,
                                previousState?.mediaStoreGeneration
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
                                error.message ?: error.javaClass.simpleName,
                                previousState?.mediaStoreVersion,
                                previousState?.mediaStoreGeneration
                            )
                        )
                    }
                }
                Result.failure(error)
            }
        }
    }

    private fun readGenerationSnapshot(): MediaStoreGeneration? {
        if (Build.VERSION.SDK_INT < 30) return null
        return runCatching {
            MediaStoreGeneration(
                version = MediaStore.getVersion(context, MediaStore.VOLUME_EXTERNAL),
                generation = MediaStore.getGeneration(context, MediaStore.VOLUME_EXTERNAL)
            )
        }.getOrNull()
    }

    private suspend fun queryAndStage(
        generationMillis: Long,
        modifiedAfterGeneration: Long? = null,
        modifiedThroughGeneration: Long? = null
    ): Int {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val incremental = modifiedAfterGeneration != null && modifiedThroughGeneration != null
        val projection = buildList {
            add(MediaStore.Video.Media._ID)
            add(MediaStore.Video.Media.DISPLAY_NAME)
            add(MediaStore.Video.Media.MIME_TYPE)
            add(MediaStore.Video.Media.DURATION)
            add(MediaStore.Video.Media.SIZE)
            add(MediaStore.Video.Media.DATE_ADDED)
            add(MediaStore.Video.Media.DATE_MODIFIED)
            add(MediaStore.Video.Media.RELATIVE_PATH)
            if (incremental) add(MediaStore.MediaColumns.GENERATION_MODIFIED)
        }.toTypedArray()
        val selection = if (incremental) {
            "${MediaStore.MediaColumns.GENERATION_MODIFIED} > ? AND " +
                "${MediaStore.MediaColumns.GENERATION_MODIFIED} <= ?"
        } else null
        val selectionArgs = if (incremental) {
            arrayOf(modifiedAfterGeneration.toString(), modifiedThroughGeneration.toString())
        } else null

        var discovered = 0
        val batch = ArrayList<SafStagedMediaEntity>(BATCH_SIZE)

        resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
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

    private fun nextTimestamp(value: Long): Long =
        if (value == Long.MAX_VALUE) Long.MAX_VALUE else value + 1L

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
