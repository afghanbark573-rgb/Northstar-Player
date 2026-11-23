package com.northstar.player.data.library

import com.northstar.player.core.database.MediaItemEntity
import org.junit.Assert.*
import org.junit.Test

class MediaStoreFolderIndexerTest {
    private fun video(key: String, path: String, source: String = "media") = MediaItemEntity(
        mediaKey = key, uri = "content://media/$key", displayName = "$key.mp4", mimeType = "video/mp4",
        durationMs = 1000, sizeBytes = 100, dateAddedSeconds = 10, dateModifiedSeconds = 20,
        sourceType = "MEDIA_STORE", sourceKey = source, relativePath = path, isAvailable = true, indexedAtMillis = 1000
    )

    @Test fun createsNestedFoldersWithStableParentKeysAndCounts() {
        val folders = MediaStoreFolderIndexer.build(listOf(video("1", "Movies/Trips/"), video("2", "Movies/Trips/"), video("3", "Movies/Clips/")), "media", 5000).associateBy { it.relativePath }
        val movies = folders.getValue("Movies")
        val trips = folders.getValue("Movies/Trips")
        val clips = folders.getValue("Movies/Clips")
        assertNull(movies.parentFolderKey)
        assertEquals("media|Movies", trips.parentFolderKey)
        assertEquals(3, movies.videoCount)
        assertEquals(2, movies.childFolderCount)
        assertFalse(movies.containsVideos)
        assertEquals(2, trips.videoCount)
        assertTrue(trips.containsVideos)
        assertEquals(0, trips.childFolderCount)
        assertEquals(1, clips.videoCount)
        assertEquals(5000L, trips.discoveredAtMillis)
    }

    @Test fun ignoresUnavailableAndOtherSourceItems() {
        val unavailable = video("2", "Old/").copy(isAvailable = false)
        val folders = MediaStoreFolderIndexer.build(listOf(video("1", "Movies/"), unavailable, video("3", "USB/", "usb")), "media")
        assertEquals(listOf("Movies"), folders.map { it.relativePath })
    }

    @Test fun normalizesSeparatorsAndIgnoresBlankPaths() {
        val folders = MediaStoreFolderIndexer.build(listOf(video("1", "DCIM\\Camera\\"), video("2", "")), "media")
        assertEquals(listOf("DCIM", "DCIM/Camera"), folders.map { it.relativePath })
    }
}