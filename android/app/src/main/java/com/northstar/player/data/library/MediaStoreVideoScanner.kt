package com.northstar.player.data.library

import android.content.ContentUris
import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.northstar.player.core.database.MediaItemEntity
import com.northstar.player.core.database.ScanStateEntity
import com.northstar.player.core.permissions.VideoPermissionScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

data class MediaScanResult(
    val discoveredCount: Int,
    val permissionScope: VideoPermissionScope,
    val completedAtMillis: Long
)

/**
 * Reads Android's indexed video collection. It never writes to or deletes source media.
 *
 * Partial-access scans are additive/upsert-only. A partial result is not proof that
 * unreturned media was deleted or became unavailable.
 */
class MediaStoreVideoScanner(
    private val resolver: ContentResolver,
    private val repository: MediaLibraryRepository,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun scan(permissionScope: VideoPermissionScope): Result<MediaScanResult> =
        withContext(Dispatchers.IO) {
            if (permissionScope == VideoPermissionScope.DENIED) {
                return@withContext Result.failure(SecurityException("Video permission is not granted"))
            }

            val sourceKey = SOURCE_KEY
            val previousSuccess = repository.getScanState(sourceKey)?.lastSuccessfulScanMillis
            repository.saveScanState(
                ScanStateEntity(
                    sourceKey = sourceKey,
                    lastSuccessfulScanMillis = previousSuccess,
                    permissionScope = permissionScope.name,
                    inProgressSinceMillis = clock(),
                    lastError = null
                )
            )

            try {
                val found = queryVideos()
                // Bounded batches avoid a single oversized SQLite operation for large libraries.
                found.chunked(400).forEach { batch -> repository.saveVideos(batch) }

                val completedAt = clock()
                repository.saveScanState(
                    ScanStateEntity(
                        sourceKey = sourceKey,
                        lastSuccessfulScanMillis = completedAt,
                        permissionScope = permissionScope.name,
                        inProgressSinceMillis = null,
                        lastError = null
                    )
                )
                Result.success(
                    MediaScanResult(found.size, permissionScope, completedAt)
                )
            } catch (cancelled: CancellationException) {
                repository.saveScanState(
                    ScanStateEntity(
                        sourceKey = sourceKey,
                        lastSuccessfulScanMillis = previousSuccess,
                        permissionScope = permissionScope.name,
                        inProgressSinceMillis = null,
                        lastError = "Scan cancelled"
                    )
                )
                throw cancelled
            } catch (error: Exception) {
                repository.saveScanState(
                    ScanStateEntity(
                        sourceKey = sourceKey,
                        lastSuccessfulScanMillis = previousSuccess,
                        permissionScope = permissionScope.name,
                        inProgressSinceMillis = null,
                        lastError = error.message ?: error.javaClass.simpleName
                    )
                )
                Result.failure(error)
            }
        }

    private fun queryVideos(): List<MediaItemEntity> {
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
        val result = ArrayList<MediaItemEntity>()

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
                val uri = ContentUris.withAppendedId(collection, cursor.getLong(idColumn))
                result += MediaItemEntity(
                    mediaKey = uri.toString(),
                    uri = uri.toString(),
                    displayName = cursor.stringOrNull(nameColumn) ?: "Untitled video",
                    mimeType = cursor.stringOrNull(mimeColumn),
                    durationMs = cursor.longOrZero(durationColumn),
                    sizeBytes = cursor.longOrZero(sizeColumn),
                    dateAddedSeconds = cursor.longOrNull(addedColumn),
                    dateModifiedSeconds = cursor.longOrNull(modifiedColumn),
                    sourceType = SOURCE_TYPE,
                    sourceKey = SOURCE_KEY,
                    relativePath = cursor.stringOrNull(pathColumn)?.trimEnd('/'),
                    isAvailable = true,
                    indexedAtMillis = clock()
                )
            }
        } ?: throw IOException("Android MediaStore did not return a video cursor")

        return result
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
    }
}
