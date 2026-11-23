package com.northstar.player

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.util.Locale

data class LocalVideo(val uri: Uri, val name: String, val duration: Long, val size: Long, val folder: String, val date: Long)

class MainActivity : ComponentActivity() {
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var status: TextView
    private var videos = emptyList<LocalVideo>()
    private var query = ""
    private var folders = true
    private var grid = false
    private var sort = 0
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    private val permission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { loadVideos() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLibrary()
        val wanted = when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (wanted.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) loadVideos()
        else permission.launch(wanted)
    }

    private fun showLibrary() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 18, 16, 8); setBackgroundColor(0xFF171717.toInt()) }
        root.addView(TextView(this).apply { text = "Northstar Player"; textSize = 25f; setTextColor(0xFFF5F5F5.toInt()) })
        status = TextView(this).apply { textSize = 13f; setTextColor(0xFFA3A3A3.toInt()); setPadding(0, 5, 0, 10) }
        root.addView(status)
        val search = EditText(this).apply {
            hint = "Search videos or folders"; setSingleLine(true); textSize = 14f
            setTextColor(0xFFF5F5F5.toInt()); setHintTextColor(0xFF999999.toInt())
            setPadding(12, 0, 12, 0); setBackgroundColor(0xFF303030.toInt())
            setText(query)
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { query = s.toString(); render() }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        root.addView(search, LinearLayout.LayoutParams(-1, 46))
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 5, 0, 5) }
        bar.addView(control("Folders") { folders = true; render() }, weight())
        bar.addView(control("All videos") { folders = false; render() }, weight())
        bar.addView(control(if (grid) "List" else "Grid") { grid = !grid; render() }, weight())
        bar.addView(control(sortLabel()) { sort = (sort + 1) % 3; render() }, weight())
        root.addView(bar)
        val scroll = ScrollView(this).apply { isFillViewport = true; isVerticalScrollBarEnabled = true; overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 2, 0, 16) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun weight() = LinearLayout.LayoutParams(0, 44, 1f)
    private fun control(label: String, action: () -> Unit) = Button(this).apply {
        text = label; textSize = 10f; isAllCaps = false; minWidth = 0; minimumWidth = 0; setPadding(2, 0, 2, 0)
        setOnClickListener { action() }
    }
    private fun sortLabel() = when (sort) { 0 -> "Recent"; 1 -> "A–Z"; else -> "Largest" }

    private fun loadVideos() {
        val result = mutableListOf<LocalVideo>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val columns = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.RELATIVE_PATH, MediaStore.Video.Media.DATE_ADDED)
        try {
            contentResolver.query(collection, columns, null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC")?.use { c ->
                val id = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val name = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val duration = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val size = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val path = c.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                val date = c.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                while (c.moveToNext()) result.add(LocalVideo(
                    Uri.withAppendedPath(collection, c.getLong(id).toString()),
                    c.getString(name) ?: "Untitled video", c.getLong(duration), c.getLong(size),
                    if (path >= 0) c.getString(path).orEmpty().trimEnd('/') else "",
                    if (date >= 0) c.getLong(date) else 0L
                ))
            }
            videos = result
            render()
        } catch (e: Exception) { status.text = "Library error: ${e.localizedMessage ?: "unknown"}" }
    }

    private fun render() {
        if (!::content.isInitialized) return
        content.removeAllViews()
        var shown = videos.filter { it.name.contains(query, true) || it.folder.contains(query, true) }
        shown = when (sort) {
            1 -> shown.sortedBy { it.name.lowercase(Locale.ROOT) }
            2 -> shown.sortedByDescending { it.size }
            else -> shown.sortedByDescending { it.date }
        }
        status.text = "${shown.size} of ${videos.size} videos"
        if (shown.isEmpty()) { section(if (videos.isEmpty()) "No videos found" else "No matches"); return }
        if (folders) {
            shown.groupBy { it.folder.ifBlank { "Other / Unknown folder" } }.toSortedMap().forEach { (path, items) ->
                section("📁  ${path.substringAfterLast('/')}  •  ${items.size} videos")
                if (grid) addGrid(items) else items.forEach { addRow(it) }
            }
        } else if (grid) addGrid(shown) else shown.forEach { addRow(it) }
    }

    private fun section(label: String) {
        content.addView(TextView(this).apply { text = label; textSize = 14f; setTextColor(0xFFF59E45.toInt()); setPadding(3, 15, 3, 7) })
    }
    private fun addRow(v: LocalVideo) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(12, 10, 12, 10); setBackgroundColor(0xFF242424.toInt())
            val p = LinearLayout.LayoutParams(-1, -2); p.bottomMargin = 5; layoutParams = p
            isClickable = true; isFocusable = true; setOnClickListener { play(v) }
        }
        card.addView(TextView(this).apply { text = v.name; textSize = 14f; maxLines = 2; setTextColor(0xFFF5F5F5.toInt()) })
        card.addView(TextView(this).apply { text = "${duration(v.duration)}  •  ${size(v.size)}"; textSize = 11f; setTextColor(0xFFA3A3A3.toInt()); setPadding(0, 4, 0, 0) })
        content.addView(card)
    }
    private fun addGrid(items: List<LocalVideo>) {
        var row: LinearLayout? = null
        items.forEachIndexed { i, v ->
            if (i % 2 == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 5 })
            }
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(9, 10, 9, 10); setBackgroundColor(0xFF242424.toInt())
                layoutParams = LinearLayout.LayoutParams(0, 112, 1f).apply { marginEnd = 4 }
                setOnClickListener { play(v) }
            }
            tile.addView(TextView(this).apply { text = "▶ VIDEO"; textSize = 10f; setTextColor(0xFFF59E45.toInt()) })
            tile.addView(TextView(this).apply { text = v.name; textSize = 11f; maxLines = 2; setTextColor(0xFFF5F5F5.toInt()); setPadding(0, 6, 0, 3) })
            tile.addView(TextView(this).apply { text = duration(v.duration); textSize = 10f; setTextColor(0xFFA3A3A3.toInt()) })
            row?.addView(tile)
        }
        if (items.size % 2 != 0) row?.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
    }

    private fun play(v: LocalVideo) {
        releasePlayer()
        root.removeAllViews()
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(Button(this).apply { text = "Library"; setOnClickListener { releasePlayer(); showLibrary(); render() } })
        val title = TextView(this).apply { text = v.name; maxLines = 2; textSize = 13f; setTextColor(0xFFF5F5F5.toInt()); setPadding(8, 0, 0, 0) }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f)); root.addView(header)
        val view = PlayerView(this).apply { useController = true; setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING); layoutParams = LinearLayout.LayoutParams(-1, 0, 1f) }
        playerView = view; root.addView(view)
        player = ExoPlayer.Builder(this).build().also { exo ->
            view.player = exo; exo.setMediaItem(MediaItem.fromUri(v.uri))
            exo.addListener(object : Player.Listener { override fun onPlayerError(error: androidx.media3.common.PlaybackException) { title.text = "Playback error: ${error.errorCodeName}" } })
            exo.prepare(); exo.playWhenReady = true
        }
    }
    private fun releasePlayer() { playerView?.player = null; playerView = null; player?.release(); player = null }
    private fun duration(ms: Long): String { val s = ms / 1000; return "%d:%02d".format(s / 60, s % 60) }
    private fun size(bytes: Long) = when { bytes >= 1073741824 -> "%.1f GB".format(bytes / 1073741824.0); bytes >= 1048576 -> "%.1f MB".format(bytes / 1048576.0); else -> "${bytes} B" }
    override fun onStart() {\n        super.onStart()\n        if ((application as NorthstarApplication).videoPermissionChecker.currentScope() != com.northstar.player.core.permissions.VideoPermissionScope.DENIED) {\n            (application as NorthstarApplication).mediaLibraryScanScheduler.enqueueRefresh()\n        }\n    }\n\n    override fun onStop() { super.onStop(); player?.pause() }
    override fun onDestroy() { releasePlayer(); super.onDestroy() }
}