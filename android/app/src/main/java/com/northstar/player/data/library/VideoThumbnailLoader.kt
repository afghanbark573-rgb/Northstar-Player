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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * Loads small video preview frames away from the main thread.
 *
 * ContentResolver.loadThumbnail is preferred on Android 10+ because it lets the
 * provider produce a correctly sized thumbnail. The provider call is tied to the
 * caller's cancellation signal so a re-render can stop obsolete work. Older
 * Android versions use MediaMetadataRetriever, requesting scaled frames where
 * the platform supports it. A bounded decode semaphore and byte-sized LRU cache
 * prevent a fast scroll from spawning unbounded decode operations.
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

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                loadProviderThumbnail(uri)
            } else {
                withContext(Dispatchers.IO) { decodeLegacy(uri) }
            }

            currentCoroutineContext().ensureActive()
            bitmap?.let { cache.put(key, it) }
            bitmap
        }
    }

    /**
     * Runs the blocking provider call on Dispatchers.IO and connects coroutine
     * cancellation to Android's CancellationSignal. This prevents obsolete
     * requests from occupying one of the limited decode slots unnecessarily.
     */
    private suspend fun loadProviderThumbnail(uri: Uri): Bitmap? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }

            try {
                Dispatchers.IO.dispatch(continuation.context, Runnable {
                    if (!continuation.isActive) return@Runnable

                    val bitmap = try {
                        appContext.contentResolver.loadThumbnail(
                            uri,
                            Size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT),
                            signal
                        )
                    } catch (_: Exception) {
                        null
                    }

                    if (continuation.isActive) continuation.resume(bitmap)
                })
            } catch (_: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }
        }

    private fun decodeLegacy(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val sourceWidth = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toIntOrNull()
                    ?.coerceAtLeast(1)
                    ?: THUMBNAIL_WIDTH
                val sourceHeight = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toIntOrNull()
                    ?.coerceAtLeast(1)
                    ?: THUMBNAIL_HEIGHT
                val (width, height) = boundedDimensions(sourceWidth, sourceHeight)
                retriever.getScaledFrameAtTime(
                    0L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    width,
                    height
                )
            } else {
                // API 26 does not expose getScaledFrameAtTime. Scale immediately
                // after extraction so the cache never retains a full-size frame.
                retriever.getFrameAtTime(
                    0L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )?.let(::scaleFrameToBounds)
            }
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun scaleFrameToBounds(frame: Bitmap): Bitmap {
        val (width, height) = boundedDimensions(frame.width, frame.height)
        if (width == frame.width && height == frame.height) return frame

        return Bitmap.createScaledBitmap(frame, width, height, true).also {
            if (it !== frame) frame.recycle()
        }
    }

    private fun boundedDimensions(sourceWidth: Int, sourceHeight: Int): Pair<Int, Int> {
        val scale = minOf(
            1f,
            THUMBNAIL_WIDTH.toFloat() / sourceWidth.coerceAtLeast(1),
            THUMBNAIL_HEIGHT.toFloat() / sourceHeight.coerceAtLeast(1)
        )
        return Pair(
            (sourceWidth * scale).roundToInt().coerceAtLeast(1),
            (sourceHeight * scale).roundToInt().coerceAtLeast(1)
        )
    }

    companion object {
        private const val DEFAULT_CACHE_KB = 8 * 1024
        private const val MAX_CONCURRENT_DECODES = 2
        private const val THUMBNAIL_WIDTH = 480
        private const val THUMBNAIL_HEIGHT = 270
    }
}
