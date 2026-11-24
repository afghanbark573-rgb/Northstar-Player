package com.northstar.player.data.library

import com.northstar.player.core.database.FolderEntity

/** Shared NEW-marker rules for MediaStore and SAF folder snapshots. */
object FolderNewState {
    fun isNew(folder: FolderEntity): Boolean {
        val discovered = folder.discoveredAtMillis ?: return false
        val acknowledged = folder.acknowledgedAtMillis
        return acknowledged == null || discovered > acknowledged
    }

    /**
     * A folder becomes new when first seen or when its descendant video/folder count grows.
     * Existing discovery history is retained when a scan finds no growth or fewer items.
     */
    fun nextDiscoveryTimestamp(
        previous: FolderEntity?,
        videoCount: Int,
        childFolderCount: Int,
        observedAtMillis: Long
    ): Long {
        if (previous == null) return observedAtMillis

        val grew = videoCount > previous.videoCount ||
            childFolderCount > previous.childFolderCount
        if (!grew) return previous.discoveredAtMillis ?: observedAtMillis

        val lastMarker = listOfNotNull(
            previous.discoveredAtMillis,
            previous.acknowledgedAtMillis
        ).maxOrNull() ?: 0L
        val nextMarker = if (lastMarker == Long.MAX_VALUE) Long.MAX_VALUE else lastMarker + 1L
        return maxOf(observedAtMillis, nextMarker)
    }
}
