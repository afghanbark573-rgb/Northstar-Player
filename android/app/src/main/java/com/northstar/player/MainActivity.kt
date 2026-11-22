package com.northstar.player

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

data class LocalVideo(
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val relativePath: String
)

class MainActivity : ComponentActivity() {
    private lateinit var root: LinearLayout
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var libraryVideos: List<LocalVideo> = emptyList()
    private var statusView: TextView? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            loadLibrary()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLibraryShell()
        requestVideoAccessIfNeeded()
    }

    private fun showLibraryShell() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 20)
            setBackgroundColor(0xFF171717.toInt())
        }
        val title = TextView(this).apply {
            text = "Northstar Player"
            textSize = 25f
            setTextColor(0xFFF5F5F5.toInt())
        }
        statusView = TextView(this).apply {
            text = "Preparing your video library…"
            textSize = 14f
            setTextColor(0xFFA3A3A3.toInt())
            setPadding(0, 12, 0, 18)
        }
        root.addView(title)
        root.addView(statusView)
        setContentView(root)
    }

    private fun requestVideoAccessIfNeeded() {
        val permissions = when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val granted = permissions.any {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) {
            loadLibrary()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun loadLibrary() {
        val videos = mutableListOf<LocalVideo>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.RELATIVE_PATH
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val pathColumn = cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = Uri.withAppendedPath(collection, id.toString())
                    videos += LocalVideo(
                        uri = uri,
                        name = cursor.getString(nameColumn) ?: "Untitled video",
                        durationMs = cursor.getLong(durationColumn),
                        sizeBytes = cursor.getLong(sizeColumn),
                        relativePath = if (pathColumn >= 0) cursor.getString(pathColumn).orEmpty() else ""
                    )
                }
            }
            libraryVideos = videos
            renderVideoList()
        } catch (error: Exception) {
            statusView?.text = "Couldn't read the video library: ${error.localizedMessage ?: "unknown error"}"
        }
    }

    private fun renderVideoList() {
        root.removeViews(2, (root.childCount - 2).coerceAtLeast(0))
        statusView?.text = when (libraryVideos.size) {
            0 -> "No videos found. Check media access or add videos to your device."
            1 -> "1 video found"
            else -> "${libraryVideos.size} videos found"
        }

        libraryVideos.forEach { video ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 13, 14, 13)
                setBackgroundColor(0xFF242424.toInt())
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 8
                layoutParams = params
            }
            val name = TextView(this).apply {
                text = video.name
                textSize = 16f
                setTextColor(0xFFF5F5F5.toInt())
            }
            val details = TextView(this).apply {
                text = "${video.relativePath}  •  ${formatDuration(video.durationMs)}  •  ${formatSize(video.sizeBytes)}"
                textSize = 12f
                setTextColor(0xFFA3A3A3.toInt())
                setPadding(0, 5, 0, 0)
            }
            row.addView(name)
            row.addView(details)
            row.setOnClickListener { openPlayer(video) }
            root.addView(row)
        }
    }

    private fun openPlayer(video: LocalVideo) {
        releasePlayer()
        root.removeAllViews()

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val back = Button(this).apply {
            text = "Library"
            setOnClickListener {
                releasePlayer()
                showLibraryShell()
                renderVideoList()
            }
        }
        val label = TextView(this).apply {
            text = video.name
            textSize = 14f
            setTextColor(0xFFF5F5F5.toInt())
            setPadding(12, 0, 0, 0)
        }
        top.addView(back)
        top.addView(label, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val view = PlayerView(this).apply {
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        playerView = view
        root.addView(view)

        val exo = ExoPlayer.Builder(this).build()
        player = exo
        view.player = exo
        exo.setMediaItem(MediaItem.fromUri(video.uri))
        exo.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                label.text = "Playback error: ${error.errorCodeName}"
            }
        })
        exo.prepare()
        exo.playWhenReady = true
    }

    private fun releasePlayer() {
        playerView?.player = null
        playerView = null
        player?.release()
        player = null
    }

    private fun formatDuration(ms: Long): String {
        val total = ms / 1000
        return "%d:%02d".format(total / 60, total % 60)
    }

    private fun formatSize(bytes: Long): String = when {
        bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        else -> "$bytes B"
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }
}
