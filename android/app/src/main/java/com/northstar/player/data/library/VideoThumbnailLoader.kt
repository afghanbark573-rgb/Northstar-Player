package com.northstar.player.data.library

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.util.LruCache
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Loads small video preview frames away from the main thread.
 *
 * ContentResolver.loadThumbnail is preferred on Android 10+ because it lets the
 * provider produce a correctly sized thumbnail. Older Android versions use
 * MediaMetadataRetriever as a compatibility fallback. A bounded decode semaphore
 * and byte-sized LRU cache prevent a fast scroll from spawning unbounded work.
 */
internal class VideoThumbnailLoader(
    context: Context,
    cacheSizeKilobytes: Int = DEFAULT_CACHE_KB,
    maxConcurrentDecodes: Int = MAX_CONCURRENT_DECODES
) {
    private val appContext = context.applicationContext
    private val decodeSlots = Semaphore(maxConcurrentDecodes.coerceAtLeast(1))
    private val cache = object : LruCache<String, Bitmap>(cacheSizeKilobytes.coerceAtLeast(1)) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    suspend fun load(uri: Uri): Bitmap? {
        val key = uri.toString()
        cache.get(key)?.let { return it }

        return decodeSlots.withPermit {
            cache.get(key)?.let { return@withPermit it }

            val bitmap = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    runCatching {
                        appContext.contentResolver.loadThumbnail(
                            uri,
                            Size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT),
                            CancellationSignal()
                        )
                    }.getOrNull()
                } else {
                    decodeLegacy(uri)
                }
            }

            currentCoroutineContext().ensureActive()
            bitmap?.let { cache.put(key, it) }
            bitmap
        }
    }

    private fun decodeLegacy(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)
            retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    companion object {
        private const val DEFAULT_CACHE_KB = 8 * 1024
        private const val MAX_CONCURRENT_DECODES = 2
        private const val THUMBNAIL_WIDTH = 480
        private const val THUMBNAIL_HEIGHT = 270
    }
}
