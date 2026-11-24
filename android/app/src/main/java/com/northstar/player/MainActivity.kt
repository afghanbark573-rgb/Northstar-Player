package com.northstar.player

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.lifecycleScope
import com.northstar.player.core.database.FolderEntity
import com.northstar.player.core.database.MediaItemEntity
import com.northstar.player.data.library.FolderNewState
import com.northstar.player.data.library.LibraryBrowser
import com.northstar.player.data.library.LibrarySort
import com.northstar.player.data.library.LibraryViewMode
import com.northstar.player.core.database.ExternalTreeGrantEntity
import com.northstar.player.data.storage.SafSourceIdentity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private data class LocalVideo(
    val key: String,
    val uri: Uri,
    val name: String,
    val duration: Long,
    val size: Long,
    val folder: String,
    val dateAdded: Long?,
    val dateModified: Long?,
    val entity: MediaItemEntity
)

class MainActivity : ComponentActivity() {
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var refreshLayout: SwipeRefreshLayout
    private var refreshJob: Job? = null
    private lateinit var status: TextView
    private lateinit var searchField: EditText
    private var videos = emptyList<LocalVideo>()
    private var folders = emptyList<FolderEntity>()
    private var query = ""
    private var searchVisible = false
    private var viewMode = LibraryViewMode.VIDEO_FOLDERS
    private var sortMode = LibrarySort.DATE_ADDED
    private var grid = false
    private var currentFolderKey: String? = null
    private var showingYou = false
    private var showingExternalStorage = false
    private var externalGrants = emptyList<ExternalTreeGrantEntity>()
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var libraryObservation: Job? = null
    private var folderObservation: Job? = null
    private var grantObservation: Job? = null

    private val treePicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            lifecycleScope.launch {
                try {
                    val name = queryTreeDisplayName(uri)
                    val app = application as NorthstarApplication
                    val grant = app.safTreeGrantManager.addTree(uri, name)
                    val result = app.safTreeScanner.scan(grant)
                    result.onSuccess {
                        Toast.makeText(this@MainActivity,
                            "Indexed ${it.discoveredVideos} videos in ${it.discoveredFolders} folders",
                            Toast.LENGTH_LONG).show()
                    }.onFailure {
                        Toast.makeText(this@MainActivity,
                            "Storage scan failed: ${it.message ?: "Access unavailable"}",
                            Toast.LENGTH_LONG).show()
                    }
                    showingExternalStorage = true
                    render()
                } catch (error: Exception) {
                    Toast.makeText(this@MainActivity,
                        "Could not access this folder: ${error.message ?: "Permission unavailable"}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val permission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        loadLibrary()
        if ((application as NorthstarApplication).videoPermissionChecker.currentScope() != com.northstar.player.core.permissions.VideoPermissionScope.DENIED) {
            (application as NorthstarApplication).mediaLibraryScanScheduler.enqueueRefresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLibrary()
        val wanted = when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (wanted.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) loadLibrary()
        else permission.launch(wanted)
    }

    private fun showLibrary() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 8)
            setBackgroundColor(0xFF000000.toInt())
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(TextView(this).apply {
            text = "Northstar"
            textSize = 23f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
        }, LinearLayout.LayoutParams(0, 52, 1f))
        top.addView(topAction("You", "You") { showingYou = true; render() })
        top.addView(topAction("☷", "Library controls") { showLibraryControls(it) })
        top.addView(topAction("⌕", "Search") {
            searchVisible = !searchVisible
            searchField.visibility = if (searchVisible) View.VISIBLE else View.GONE
            if (searchVisible) searchField.requestFocus() else {
                query = ""
                searchField.setText("")
            }
        })
        root.addView(top)

        searchField = EditText(this).apply {
            hint = "Search videos and folders"
            setSingleLine(true)
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
            setPadding(12, 0, 12, 0)
            setBackgroundColor(0xFF202020.toInt())
            visibility = if (searchVisible) View.VISIBLE else View.GONE
            setText(query)
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    query = s.toString()
                    render()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
        root.addView(searchField, LinearLayout.LayoutParams(-1, 46))

        status = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF999999.toInt())
            setPadding(2, 8, 2, 6)
        }
        root.addView(status)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 2, 0, 16)
        }
        scroll.addView(content)
        refreshLayout = SwipeRefreshLayout(this).apply {
            setColorSchemeColors(0xFFFFFFFF.toInt())
            setProgressBackgroundColorSchemeColor(0xFF151515.toInt())
            setOnRefreshListener { refreshCurrentPage() }
            addView(scroll, ViewGroup.LayoutParams(-1, -1))
        }
        root.addView(refreshLayout, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        render()
    }

    private fun refreshCurrentPage() {
        if (refreshJob?.isActive == true) {
            refreshLayout.isRefreshing = true
            return
        }
        val app = application as NorthstarApplication
        refreshJob = lifecycleScope.launch {
            refreshLayout.isRefreshing = true
            val failures = mutableListOf<String>()
            var refreshedSources = 0
            try {
                if (!showingExternalStorage) {
                    val scope = app.videoPermissionChecker.currentScope()
                    if (scope == com.northstar.player.core.permissions.VideoPermissionScope.DENIED) {
                        failures += "Internal video permission is not available"
                    } else {
                        app.mediaStoreVideoScanner.scan(scope)
                            .onSuccess { refreshedSources++ }
                            .onFailure { failures += "Internal library: ${it.message ?: "scan failed"}" }
                    }
                }

                val currentSourceKey = currentFolderKey?.let { key ->
                    folders.firstOrNull { it.folderKey == key }?.sourceKey
                }
                val latestGrants = app.safTreeGrantManager.refreshGrantAvailability()
                val targets = LibraryRefreshTargets.select(
                    currentFolderSourceKey = currentSourceKey,
                    showingExternalStorage = showingExternalStorage,
                    grants = latestGrants
                )
                targets.forEach { grant ->
                    app.safTreeScanner.scan(grant)
                        .onSuccess { refreshedSources++ }
                        .onFailure { failures += "${grant.displayName}: ${it.message ?: "scan failed"}" }
                }

                if (failures.isNotEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Refresh finished with issues: ${failures.joinToString("; ")}",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (refreshedSources == 0) {
                    Toast.makeText(this@MainActivity, "No accessible storage sources to refresh", Toast.LENGTH_SHORT).show()
                }
            } catch (error: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Refresh failed: ${error.message ?: "Storage unavailable"}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                refreshLayout.isRefreshing = false
            }
        }
    }

    private fun topAction(label: String, description: String, action: (View) -> Unit) =
        TextView(this).apply {
            text = label
            contentDescription = description
            textSize = if (label == "You") 13f else 25f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(10, 0, 10, 0)
            minWidth = 42
            setOnClickListener { action(it) }
        }

    private fun showLibraryControls(anchor: View) {
        val menu = PopupMenu(this, anchor)
        menu.menu.add("SORT").isEnabled = false
        menu.menu.add("◷  Date Added").setOnMenuItemClickListener { sortMode = LibrarySort.DATE_ADDED; render(); true }
        menu.menu.add("◷  Date Modified").setOnMenuItemClickListener { sortMode = LibrarySort.DATE_MODIFIED; render(); true }
        menu.menu.add("A–Z  Name").setOnMenuItemClickListener { sortMode = LibrarySort.NAME; render(); true }
        menu.menu.add("⇩  Largest").setOnMenuItemClickListener { sortMode = LibrarySort.LARGEST; render(); true }
        menu.menu.add("VIEW MODE").isEnabled = false
        menu.menu.add("▣  Video Folders").setOnMenuItemClickListener { setViewMode(LibraryViewMode.VIDEO_FOLDERS); true }
        menu.menu.add("▤  Files Only").setOnMenuItemClickListener { setViewMode(LibraryViewMode.FILES_ONLY); true }
        menu.menu.add("☷  Nested View").setOnMenuItemClickListener { setViewMode(LibraryViewMode.NESTED_VIEW); true }
        menu.menu.add("LAYOUT").isEnabled = false
        menu.menu.add(if (grid) "☰  List" else "▦  Grid").setOnMenuItemClickListener { grid = !grid; render(); true }
        menu.show()
    }

    private fun setViewMode(mode: LibraryViewMode) {
        viewMode = mode
        currentFolderKey = null
        showingYou = false
        render()
    }

    private fun loadLibrary() {
        if (libraryObservation?.isActive == true) return
        val app = application as NorthstarApplication
        libraryObservation = lifecycleScope.launch {
            app.mediaLibraryRepository.observeAvailableVideos().collect { indexed ->
                videos = indexed.map { item ->
                    LocalVideo(
                        key = item.mediaKey,
                        uri = Uri.parse(item.uri),
                        name = item.displayName,
                        duration = item.durationMs,
                        size = item.sizeBytes,
                        folder = item.relativePath.orEmpty().trimEnd('/'),
                        dateAdded = item.dateAddedSeconds,
                        dateModified = item.dateModifiedSeconds,
                        entity = item
                    )
                }
                render()
            }
        }
        folderObservation = lifecycleScope.launch {
            app.database.folderDao().observeAll().collect { indexedFolders ->
                folders = indexedFolders
                render()
            }
        }
        grantObservation = lifecycleScope.launch {
            app.database.externalTreeGrantDao().observeAll().collect { grants ->
                externalGrants = grants
                render()
            }
        }
    }

    private fun render() {
        if (!::content.isInitialized) return
        content.removeAllViews()

        if (showingExternalStorage) {
            renderExternalStorage()
            return
        }

        if (showingYou) {
            status.text = "YOUR LIBRARY"
            section("You")
            listDestination("♡", "Favorites", "Your saved videos")
            listDestination("◷", "Watch History", "Videos you have watched")
            listDestination("▣", "Private Library", "Protected personal videos")
            listDestination("⚙", "Settings", "Playback and app preferences")
            return
        }

        if (viewMode == LibraryViewMode.NESTED_VIEW && currentFolderKey != null) {
            val folder = folders.firstOrNull { it.folderKey == currentFolderKey }
            val path = folder?.relativePath.orEmpty()
            val crumbs = path.split('/').filter { it.isNotBlank() }
            val breadcrumb = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            breadcrumb.addView(TextView(this).apply {
                text = "‹"
                textSize = 26f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(4, 0, 12, 0)
                setOnClickListener { navigateUp() }
            })
            breadcrumb.addView(TextView(this).apply {
                text = crumbs.joinToString("  ›  ")
                textSize = 12f
                maxLines = 1
                setTextColor(0xFFBBBBBB.toInt())
            }, LinearLayout.LayoutParams(0, 42, 1f))
            val canCreate = folder?.let { current ->
                current.sourceKey.startsWith("saf:") && externalGrants.any { grant ->
                    SafSourceIdentity.key(grant.treeUri) == current.sourceKey &&
                        grant.isAvailable &&
                        (grant.persistedFlags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0
                }
            } == true
            breadcrumb.addView(TextView(this).apply {
                text = "+"
                textSize = 25f
                contentDescription = "Create folder"
                setTextColor(if (canCreate) 0xFFFFFFFF.toInt() else 0xFF666666.toInt())
                setPadding(12, 0, 8, 0)
                setOnClickListener {
                    if (canCreate && folder != null) showCreateFolderDialog(folder!!)
                    else Toast.makeText(this@MainActivity,
                        "Folder creation requires an available USB/SD folder with write permission.",
                        Toast.LENGTH_LONG).show()
                }
            })
            content.addView(breadcrumb)
        }

        if (viewMode == LibraryViewMode.NESTED_VIEW && currentFolderKey == null) {
            val manage = TextView(this).apply {
                text = "＋  Manage external storage"
                textSize = 13f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(12, 12, 12, 12)
                setOnClickListener { openExternalStorage() }
            }
            content.addView(manage)
        }

        val listing = LibraryBrowser.build(
            allVideos = videos.map { it.entity },
            allFolders = folders,
            mode = viewMode,
            sort = sortMode,
            query = query,
            currentFolderKey = currentFolderKey
        )
        val visibleCount = listing.videos.size
        status.text = when (viewMode) {
            LibraryViewMode.VIDEO_FOLDERS -> "VIDEO FOLDERS  •  ${listing.folders.size} folders"
            LibraryViewMode.FILES_ONLY -> "FILES ONLY  •  $visibleCount videos"
            LibraryViewMode.NESTED_VIEW -> "${listing.folders.size} folders  •  $visibleCount videos"
        }

        if (viewMode == LibraryViewMode.VIDEO_FOLDERS || viewMode == LibraryViewMode.NESTED_VIEW) {
            listing.folders.forEach { folder -> addFolder(folder) }
        }
        val videoByKey = videos.associateBy { it.key }
        val listedVideos = listing.videos.mapNotNull { videoByKey[it.mediaKey] }
        if (listedVideos.isNotEmpty()) {
            if (viewMode == LibraryViewMode.NESTED_VIEW) section("VIDEOS")
            if (grid) addGrid(listedVideos) else listedVideos.forEach { addRow(it) }
        }
        if (listing.folders.isEmpty() && listedVideos.isEmpty()) {
            section(when {
                videos.isEmpty() -> "No videos found yet"
                query.isNotBlank() -> "No matches"
                else -> "This folder is empty"
            })
        }
    }

    private fun acknowledgeFolder(folderKey: String) {
        lifecycleScope.launch {
            (application as NorthstarApplication).database.folderDao().acknowledge(folderKey)
        }
    }

    private fun navigateUp() {
        val current = folders.firstOrNull { it.folderKey == currentFolderKey }
        currentFolderKey = current?.parentFolderKey
        currentFolderKey?.let(::acknowledgeFolder)
        if (currentFolderKey == null && viewMode != LibraryViewMode.NESTED_VIEW) {
            viewMode = LibraryViewMode.VIDEO_FOLDERS
        }
        render()
    }

    override fun onBackPressed() {
        when {
            player != null -> { releasePlayer(); showLibrary() }
            showingExternalStorage -> { showingExternalStorage = false; render() }
            showingYou -> { showingYou = false; render() }
            viewMode == LibraryViewMode.NESTED_VIEW && currentFolderKey != null -> navigateUp()
            viewMode == LibraryViewMode.NESTED_VIEW -> setViewMode(LibraryViewMode.VIDEO_FOLDERS)
            else -> super.onBackPressed()
        }
    }

    private fun listDestination(icon: String, title: String, subtitle: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 13, 14, 13)
            setBackgroundColor(0xFF171717.toInt())
            isClickable = true
            isFocusable = true
            setOnClickListener {
                Toast.makeText(this@MainActivity, "$title is scheduled for the next implementation stage.", Toast.LENGTH_SHORT).show()
            }
        }
        val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        line.addView(TextView(this).apply { text = icon; textSize = 21f; setTextColor(0xFFFFFFFF.toInt()); setPadding(0, 0, 16, 0) })
        line.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply { text = title; textSize = 15f; setTextColor(0xFFFFFFFF.toInt()) })
            addView(TextView(this@MainActivity).apply { text = subtitle; textSize = 11f; setTextColor(0xFF999999.toInt()); setPadding(0, 3, 0, 0) })
        }, LinearLayout.LayoutParams(0, -2, 1f))
        line.addView(TextView(this).apply { text = "›"; textSize = 24f; setTextColor(0xFF888888.toInt()) })
        card.addView(line)
        content.addView(card)
        content.addView(View(this).apply { setBackgroundColor(0xFF252525.toInt()) }, LinearLayout.LayoutParams(-1, 1))
    }

    private fun openExternalStorage() {
        showingExternalStorage = true
        lifecycleScope.launch {
            (application as NorthstarApplication).safTreeGrantManager.refreshGrantAvailability()
        }
        render()
    }

    private fun renderExternalStorage() {
        status.text = "EXTERNAL STORAGE"
        section("Selected folders")
        val add = TextView(this).apply {
            text = "＋  Add USB / SD card or folder"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(14, 16, 14, 16)
            setOnClickListener { treePicker.launch(null) }
        }
        content.addView(add)
        if (externalGrants.isEmpty()) {
            section("No external folders selected")
        } else {
            externalGrants.forEach { grant ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(14, 13, 14, 13)
                    setBackgroundColor(0xFF151515.toInt())
                    isClickable = true
                    setOnClickListener {
                        if (grant.isAvailable) scanExternalTree(grant)
                        else treePicker.launch(null)
                    }
                }
                row.addView(TextView(this@MainActivity).apply {
                    text = grant.displayName
                    textSize = 14f
                    setTextColor(0xFFFFFFFF.toInt())
                })
                row.addView(TextView(this@MainActivity).apply {
                    text = if (grant.isAvailable) "Tap to refresh and index" else "Permission unavailable — select again"
                    textSize = 11f
                    setTextColor(0xFF999999.toInt())
                    setPadding(0, 4, 0, 0)
                })
                row.addView(TextView(this@MainActivity).apply {
                    text = "Browse folders  ›"
                    textSize = 12f
                    setTextColor(0xFFFFFFFF.toInt())
                    setPadding(0, 10, 0, 0)
                    setOnClickListener { browseExternalTree(grant) }
                })
                content.addView(row)
                content.addView(View(this).apply { setBackgroundColor(0xFF252525.toInt()) },
                    LinearLayout.LayoutParams(-1, 1))
            }
        }
        val back = TextView(this).apply {
            text = "‹  Back to library"
            textSize = 13f
            setTextColor(0xFFBBBBBB.toInt())
            setPadding(12, 18, 12, 18)
            setOnClickListener { showingExternalStorage = false; render() }
        }
        content.addView(back)
    }

    private fun scanExternalTree(grant: ExternalTreeGrantEntity) {
        lifecycleScope.launch {
            val result = (application as NorthstarApplication).safTreeScanner.scan(grant)
            result.onSuccess {
                Toast.makeText(this@MainActivity,
                    "Updated ${it.discoveredVideos} videos",
                    Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@MainActivity,
                    "Storage unavailable: ${it.message ?: "Permission revoked"}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun browseExternalTree(grant: ExternalTreeGrantEntity) {
        if (!grant.isAvailable) {
            Toast.makeText(this, "Reconnect or reselect this storage folder first.", Toast.LENGTH_LONG).show()
            return
        }
        val treeUri = Uri.parse(grant.treeUri)
        val rootDocumentId = try { DocumentsContract.getTreeDocumentId(treeUri) } catch (_: Exception) { null }
        if (rootDocumentId == null) {
            Toast.makeText(this, "This storage folder cannot be opened.", Toast.LENGTH_LONG).show()
            return
        }
        val sourceKey = SafSourceIdentity.key(grant.treeUri)
        val rootKey = "$sourceKey|$rootDocumentId"
        if (folders.none { it.folderKey == rootKey }) {
            scanExternalTree(grant)
            Toast.makeText(this, "Storage is being refreshed. Browse again when indexing finishes.", Toast.LENGTH_LONG).show()
            return
        }
        showingExternalStorage = false
        showingYou = false
        viewMode = LibraryViewMode.NESTED_VIEW
        currentFolderKey = rootKey
        acknowledgeFolder(rootKey)
        render()
    }

    private fun showCreateFolderDialog(parent: FolderEntity) {
        val nameInput = EditText(this).apply {
            hint = "Folder name"
            setSingleLine(true)
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
            setPadding(18, 12, 18, 12)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Create folder")
            .setView(nameInput)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text.toString()
                lifecycleScope.launch {
                    try {
                        val app = application as NorthstarApplication
                        val grant = app.safFolderManager.createFolder(parent, name)
                        val scan = app.safTreeScanner.scan(grant)
                        scan.onSuccess {
                            Toast.makeText(this@MainActivity, "Folder created", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }.onFailure { error ->
                            Toast.makeText(this@MainActivity,
                                "Folder created, but library refresh failed: ${error.message ?: "Storage unavailable"}",
                                Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    } catch (error: Exception) {
                        nameInput.error = error.message ?: "Could not create folder"
                    }
                }
            }
        }
        dialog.show()
    }

    private fun queryTreeDisplayName(uri: Uri): String {
        val documentId = try { DocumentsContract.getTreeDocumentId(uri) } catch (_: Exception) { null }
        if (documentId != null) {
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)
            contentResolver.query(
                documentUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    if (index >= 0 && !cursor.isNull(index)) return cursor.getString(index)
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast(':')?.ifBlank { null } ?: "External storage"
    }

    private fun section(label: String) {
        content.addView(TextView(this).apply {
            text = label
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFFAAAAAA.toInt())
            setPadding(3, 14, 3, 8)
        })
    }

    private fun addFolder(folder: FolderEntity) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(13, 13, 13, 13)
            setBackgroundColor(0xFF151515.toInt())
            isClickable = true
            isFocusable = true
            setOnClickListener {
                acknowledgeFolder(folder.folderKey)
                viewMode = LibraryViewMode.NESTED_VIEW
                currentFolderKey = folder.folderKey
                showingYou = false
                render()
            }
        }
        card.addView(TextView(this).apply {
            text = "▰"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 14, 0)
        })
        card.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = folder.displayName
                textSize = 15f
                setTextColor(0xFFFFFFFF.toInt())
                maxLines = 1
            })
            addView(TextView(this@MainActivity).apply {
                text = "${folder.videoCount} videos  •  ${folder.childFolderCount} folders"
                textSize = 11f
                setTextColor(0xFF999999.toInt())
                setPadding(0, 3, 0, 0)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))
        if (FolderNewState.isNew(folder)) {
            card.addView(TextView(this).apply {
                text = "NEW"
                textSize = 9f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(8, 4, 8, 4)
                contentDescription = "New content"
            })
        }
        card.addView(TextView(this).apply { text = "›"; textSize = 24f; setTextColor(0xFF888888.toInt()) })
        content.addView(card)
        content.addView(View(this).apply { setBackgroundColor(0xFF252525.toInt()) }, LinearLayout.LayoutParams(-1, 1))
    }

    private fun addRow(video: LocalVideo) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 11, 12, 11)
            setBackgroundColor(0xFF151515.toInt())
            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = 2
            layoutParams = params
            isClickable = true
            isFocusable = true
            setOnClickListener { play(video) }
        }
        card.addView(TextView(this).apply {
            text = video.name
            textSize = 14f
            maxLines = 2
            setTextColor(0xFFFFFFFF.toInt())
        })
        card.addView(TextView(this).apply {
            text = "${duration(video.duration)}  •  ${size(video.size)}"
            textSize = 11f
            setTextColor(0xFF999999.toInt())
            setPadding(0, 4, 0, 0)
        })
        content.addView(card)
    }

    private fun addGrid(items: List<LocalVideo>) {
        var row: LinearLayout? = null
        items.forEachIndexed { index, video ->
            if (index % 2 == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 5 })
            }
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(10, 12, 10, 12)
                setBackgroundColor(0xFF151515.toInt())
                layoutParams = LinearLayout.LayoutParams(0, 112, 1f).apply { marginEnd = 5 }
                isClickable = true
                isFocusable = true
                setOnClickListener { play(video) }
            }
            tile.addView(TextView(this).apply { text = "▶ VIDEO"; textSize = 10f; setTextColor(0xFFBBBBBB.toInt()) })
            tile.addView(TextView(this).apply { text = video.name; textSize = 12f; maxLines = 2; setTextColor(0xFFFFFFFF.toInt()); setPadding(0, 7, 0, 3) })
            tile.addView(TextView(this).apply { text = duration(video.duration); textSize = 10f; setTextColor(0xFF999999.toInt()) })
            row?.addView(tile)
        }
        if (items.size % 2 != 0) row?.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
    }

    private fun play(video: LocalVideo) {
        releasePlayer()
        root.removeAllViews()
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(Button(this).apply {
            text = "Library"
            setOnClickListener { releasePlayer(); showLibrary() }
        })
        val title = TextView(this).apply {
            text = video.name
            maxLines = 2
            textSize = 13f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(8, 0, 0, 0)
        }
        header.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(header)
        val view = PlayerView(this).apply {
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        playerView = view
        root.addView(view)
        player = ExoPlayer.Builder(this).build().also { exo ->
            view.player = exo
            exo.setMediaItem(MediaItem.fromUri(video.uri))
            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    title.text = "Playback error: ${error.errorCodeName}"
                }
            })
            exo.prepare()
            exo.playWhenReady = true
        }
    }

    private fun releasePlayer() {
        playerView?.player = null
        playerView = null
        player?.release()
        player = null
    }

    private fun duration(ms: Long): String {
        val seconds = ms / 1000
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }

    private fun size(bytes: Long): String = when {
        bytes >= 1073741824 -> "%.1f GB".format(bytes / 1073741824.0)
        bytes >= 1048576 -> "%.1f MB".format(bytes / 1048576.0)
        else -> "$bytes B"
    }

    override fun onStart() {
        super.onStart()
        if ((application as NorthstarApplication).videoPermissionChecker.currentScope() != com.northstar.player.core.permissions.VideoPermissionScope.DENIED) {
            (application as NorthstarApplication).mediaLibraryScanScheduler.enqueueRefresh()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        libraryObservation?.cancel()
        folderObservation?.cancel()
        grantObservation?.cancel()
        releasePlayer()
        super.onDestroy()
    }
}
