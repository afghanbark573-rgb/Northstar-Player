package com.northstar.player.data.library

import com.northstar.player.core.database.FolderEntity
import com.northstar.player.core.database.MediaItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryBrowserTest {
    private fun video(
        key: String,
        name: String,
        path: String?,
        added: Long?,
        modified: Long?,
        bytes: Long,
        sourceKey: String = "media"
    ) = MediaItemEntity(
        key, "content://media/$key", name, "video/mp4", 1000, bytes, added, modified,
        "MEDIA_STORE", sourceKey, path, true, 1000
    )

    private fun folder(
        key: String,
        parent: String?,
        name: String,
        path: String,
        count: Int = 1,
        sourceKey: String = "media"
    ) = FolderEntity(
        key, parent, name, sourceKey, path, count > 0, count, 0, 1000, null
    )

    private val videos = listOf(
        video("a", "zeta.mp4", "Movies/Trips/", 10, 30, 100),
        video("b", "alpha.mp4", "Movies/Trips/", 20, 10, 500),
        video("c", "root.mp4", "", 15, 20, 250),
        video("gone", "gone.mp4", "Movies/", 99, 99, 999).copy(isAvailable = false)
    )
    private val folders = listOf(
        folder("movies", null, "Movies", "Movies", 2),
        folder("trips", "movies", "Trips", "Movies/Trips", 2),
        folder("empty", null, "Empty", "Empty", 0)
    )

    @Test fun videoFoldersShowsTopLevelFoldersWithDescendantVideos() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.NAME)
        assertEquals(listOf("Movies"), result.folders.map { it.displayName })
        assertEquals(emptyList<MediaItemEntity>(), result.videos)
    }

    @Test fun filesOnlyReturnsVideosWithoutFolderRowsAndSortsByRequestedField() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.FILES_ONLY, LibrarySort.LARGEST)
        assertEquals(emptyList<FolderEntity>(), result.folders)
        assertEquals(listOf("alpha.mp4", "root.mp4", "zeta.mp4"), result.videos.map { it.displayName })
    }

    @Test fun nestedViewShowsImmediateChildrenAndDirectVideos() {
        val result = LibraryBrowser.build(
            videos, folders, LibraryViewMode.NESTED_VIEW, LibrarySort.NAME, currentFolderKey = "movies"
        )
        assertEquals(listOf("Trips"), result.folders.map { it.displayName })
        assertEquals(emptyList<MediaItemEntity>(), result.videos)

        val child = LibraryBrowser.build(
            videos, folders, LibraryViewMode.NESTED_VIEW, LibrarySort.NAME, currentFolderKey = "trips"
        )
        assertEquals(emptyList<FolderEntity>(), child.folders)
        assertEquals(listOf("alpha.mp4", "zeta.mp4"), child.videos.map { it.displayName })
    }

    @Test fun searchMatchesVideoNamesAndExcludesUnavailableItems() {
        val result = LibraryBrowser.build(
            videos, folders, LibraryViewMode.FILES_ONLY, LibrarySort.DATE_ADDED, query = "ALPHA"
        )
        assertEquals(listOf("alpha.mp4"), result.videos.map { it.displayName })
    }

    @Test fun folderSortUsesNewestDescendantDateAddedAndDateModified() {
        val allVideos = videos + video("d", "camera.mp4", "Camera/", 30, 5, 50)
        val allFolders = folders + folder("camera", null, "Camera", "Camera", 1)

        val byAdded = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.DATE_ADDED
        )
        assertEquals(listOf("Camera", "Movies"), byAdded.folders.map { it.displayName })

        val byModified = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.DATE_MODIFIED
        )
        assertEquals(listOf("Movies", "Camera"), byModified.folders.map { it.displayName })
    }

    @Test fun largestFolderSortUsesSumOfAvailableDescendantBytes() {
        val allVideos = videos + video("d", "camera.mp4", "Camera/", 30, 5, 50)
        val allFolders = folders + folder("camera", null, "Camera", "Camera", 1)

        val result = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.LARGEST
        )
        assertEquals(listOf("Movies", "Camera"), result.folders.map { it.displayName })
    }

    @Test fun dateModifiedSortIsIndependentFromDateAddedSort() {
        val result = LibraryBrowser.build(videos, folders, LibraryViewMode.FILES_ONLY, LibrarySort.DATE_MODIFIED)
        assertEquals(listOf("zeta.mp4", "root.mp4", "alpha.mp4"), result.videos.map { it.displayName })
    }

    @Test fun unknownDatesSortAfterKnownDatesForFoldersAndVideos() {
        val allVideos = listOf(
            video("unknown", "Unknown.mp4", "Unknown/", null, null, 100),
            video("known", "Known.mp4", "Known/", 50, 60, 100)
        )
        val allFolders = listOf(
            folder("unknown-folder", null, "Unknown", "Unknown"),
            folder("known-folder", null, "Known", "Known")
        )

        val foldersByAdded = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.DATE_ADDED
        )
        assertEquals(listOf("Known", "Unknown"), foldersByAdded.folders.map { it.displayName })

        val videosByModified = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.FILES_ONLY, LibrarySort.DATE_MODIFIED
        )
        assertEquals(listOf("Known.mp4", "Unknown.mp4"), videosByModified.videos.map { it.displayName })
    }

    @Test fun equalSortValuesUseStableIdentityTieBreakers() {
        val allVideos = listOf(
            video("z-video", "same.mp4", "Shared/", 10, 20, 100, "z-source"),
            video("a-video", "same.mp4", "Shared/", 10, 20, 100, "a-source")
        )
        val allFolders = listOf(
            folder("z-folder", null, "Shared", "Shared", 1, "z-source"),
            folder("a-folder", null, "Shared", "Shared", 1, "a-source")
        )

        val foldersBySize = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.LARGEST
        )
        assertEquals(listOf("a-folder", "z-folder"), foldersBySize.folders.map { it.folderKey })

        val videosBySize = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.FILES_ONLY, LibrarySort.LARGEST
        )
        assertEquals(listOf("a-video", "z-video"), videosBySize.videos.map { it.mediaKey })
    }

    @Test fun staleMediaStoreFoldersAreHiddenButEmptySafFoldersRemainInNestedView() {
        val visibleVideo = video(
            "visible", "visible.mp4", "Movies/Current/", 10, 10, 100, "mediastore:external"
        )
        val folders = listOf(
            folder("movies", null, "Movies", "Movies", 1, "mediastore:external"),
            folder("current", "movies", "Current", "Movies/Current", 1, "mediastore:external"),
            folder("stale", "movies", "Old selection", "Movies/Old", 2, "mediastore:external"),
            folder("usb-empty", null, "Empty USB folder", "", 0, "saf:usb")
        )

        val root = LibraryBrowser.build(
            listOf(visibleVideo), folders, LibraryViewMode.NESTED_VIEW, LibrarySort.NAME
        )
        assertEquals(
            listOf("Empty USB folder", "Movies"),
            root.folders.map { it.displayName }
        )

        val movies = LibraryBrowser.build(
            listOf(visibleVideo), folders, LibraryViewMode.NESTED_VIEW, LibrarySort.NAME,
            currentFolderKey = "movies"
        )
        assertEquals(listOf("Current"), movies.folders.map { it.displayName })
    }

    @Test fun videoFoldersIgnoreStalePersistedCountsWhenNoCurrentMediaIsAccessible() {
        val stale = folder("stale", null, "Old selection", "Old selection", 4, "mediastore:external")
        val result = LibraryBrowser.build(
            emptyList(), listOf(stale), LibraryViewMode.VIDEO_FOLDERS, LibrarySort.NAME
        )
        assertEquals(emptyList<FolderEntity>(), result.folders)
    }

    @Test fun folderMetricsStayIsolatedBySourceAndNormalizePathSeparators() {
        val allVideos = listOf(
            video("usb", "clip.mp4", "Movies//Trips\\", 10, 10, 200, "usb-source"),
            video("internal", "clip.mp4", "Movies/Trips/", 10, 10, 100, "internal-source")
        )
        val allFolders = listOf(
            folder("usb-movies", null, "Movies", "Movies", 1, "usb-source"),
            folder("internal-movies", null, "Movies", "Movies", 1, "internal-source"),
            folder("usb-trips", "usb-movies", "Trips", "Movies/Trips/", 1, "usb-source")
        )

        val topLevel = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.VIDEO_FOLDERS, LibrarySort.LARGEST
        )
        assertEquals(listOf("usb-movies", "internal-movies"), topLevel.folders.map { it.folderKey })

        val nested = LibraryBrowser.build(
            allVideos, allFolders, LibraryViewMode.NESTED_VIEW, LibrarySort.NAME, currentFolderKey = "usb-trips"
        )
        assertEquals(listOf("usb"), nested.videos.map { it.mediaKey })
    }
}
