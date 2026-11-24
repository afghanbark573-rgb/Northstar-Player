package com.northstar.player.data.library

import com.northstar.player.core.database.FolderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderNewStateTest {
    private fun folder(
        videoCount: Int = 2,
        childFolderCount: Int = 1,
        discoveredAt: Long? = 100L,
        acknowledgedAt: Long? = 100L
    ) = FolderEntity(
        folderKey = "source|folder",
        parentFolderKey = null,
        displayName = "Folder",
        sourceKey = "source",
        relativePath = "Folder",
        containsVideos = videoCount > 0,
        videoCount = videoCount,
        childFolderCount = childFolderCount,
        discoveredAtMillis = discoveredAt,
        acknowledgedAtMillis = acknowledgedAt
    )

    @Test
    fun firstDiscoveryCreatesNewMarker() {
        val discovered = FolderNewState.nextDiscoveryTimestamp(null, 1, 0, 500L)
        assertEquals(500L, discovered)
        assertTrue(FolderNewState.isNew(folder(discoveredAt = discovered, acknowledgedAt = null)))
    }

    @Test
    fun additionalVideosMarkExistingFolderNew() {
        assertEquals(500L, FolderNewState.nextDiscoveryTimestamp(folder(), 3, 1, 500L))
    }

    @Test
    fun additionalChildFoldersMarkParentNew() {
        assertEquals(500L, FolderNewState.nextDiscoveryTimestamp(folder(), 2, 2, 500L))
    }

    @Test
    fun unchangedOrReducedCountsDoNotResurrectNewMarker() {
        assertEquals(100L, FolderNewState.nextDiscoveryTimestamp(folder(), 2, 1, 500L))
        assertEquals(100L, FolderNewState.nextDiscoveryTimestamp(folder(), 1, 0, 500L))
        assertFalse(FolderNewState.isNew(folder()))
    }

    @Test
    fun newContentAfterAcknowledgementRemainsNewEvenWithinSameMillisecond() {
        val acknowledged = folder(discoveredAt = 100L, acknowledgedAt = 100L)
        val next = FolderNewState.nextDiscoveryTimestamp(acknowledged, 3, 1, 100L)
        assertEquals(101L, next)
        assertTrue(FolderNewState.isNew(acknowledged.copy(discoveredAtMillis = next)))
    }

    @Test
    fun unknownDiscoveryTimestampDoesNotShowNewMarker() {
        assertFalse(FolderNewState.isNew(folder(discoveredAt = null, acknowledgedAt = null)))
    }
}
