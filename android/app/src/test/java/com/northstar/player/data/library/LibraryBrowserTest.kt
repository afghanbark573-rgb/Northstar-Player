package com.northstar.player.data.library

import com.northstar.player.core.database.FolderEntity
import com.northstar.player.core.database.MediaItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryBrowserTest {
    private fun video(key: String, name: String, path: String?, added: Long, modified: Long, bytes: Long) =
        MediaItemEntity(key, "content://media/$key", name, "video/mp4", 1000, bytes, added, modified,
            "MEDIA_STORE", "media", path, true, 1000)

    private fun folder(key: String, parent: String?, name: String, path: String, hasVideos: Boolean = true) =
        FolderEntity(key, parent, name, "media", path, hasVideos, 1, 0, 1000, null)

    private val videos = listOf(
        video("a", "zeta.mp4", "Movies/Trips/", 10, 30, 100),
        video("b", "alpha.mp4", "Movies/Trips/", 20, 10, 500),
        video("c", "root.mp4", "", 15, 20, 250),
        video("gone", "gone.mp4", "Movies/", 99, 99, 999).copy(isAvailable = false)
    )
    private val folders = listOf(
        folder("movies", null, "Movies", "Movies", false),
        folder("trips", "movies", "Trips", "Movies/Trips"),
        folder("empty", null, "Empty", "Empty", false)
    )

    @Test fun videoFoldersShowsOnlyTopLevelFoldersContainingVideos() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.NAME)
        assertEquals(listOf("Movies"), result.folders.map { it.displayName })
        assertEquals(emptyList<MediaItemEntity>(), result.videos)
    }

    @Test fun filesOnlyReturnsVideosWithoutFolderRowsAndSortsByRequestedField() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.FILES_ONLY, LibrarySort.LARGEST)
        assertEquals(emptyList<FolderEntity>(), result.folders)
        assertEquals(listOf("alpha.mp4", "root.mp4", "zeta.mp4"), result.videos.map { it.displayName })
    }

    @Test fun nestedViewShowsOnlyImmediateChildrenAndDirectVideos() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.NESTED_VIEW, LibrarySort.NAME, currentFolderKey = "movies")
        assertEquals(listOf("Trips"), result.folders.map { it.displayName })
        assertEquals(emptyList<MediaItemEntity>(), result.videos)

        val child = LibraryBrowser.build(videos, folders, LibraryViewMode.NESTED_VIEW, LibrarySort.NAME, currentFolderKey = "trips")
        assertEquals(emptyList<FolderEntity>(), child.folders)
        assertEquals(listOf("alpha.mp4", "zeta.mp4"), child.videos.map { it.displayName })
    }

    @Test fun searchMatchesVideoNamesAndFolderPathsAndExcludesUnavailableItems() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.FILES_ONLY, LibrarySort.DATE_ADDED, query = "ALPHA")
        assertEquals(listOf("alpha.mp4"), result.videos.map { it.displayName })
    }

    @Test fun dateModifiedSortIsIndependentFromDateAddedSort() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.FILES_ONLY, LibrarySort.DATE_MODIFIED)
        assertEquals(listOf("zeta.mp4", "root.mp4", "alpha.mp4"), result.videos.map { it.displayName })
    }
}
