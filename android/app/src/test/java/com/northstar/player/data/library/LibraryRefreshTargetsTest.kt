package com.northstar.player.data.library

import com.northstar.player.core.database.ExternalTreeGrantEntity
import com.northstar.player.data.storage.SafSourceIdentity
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryRefreshTargetsTest {
    private fun grant(uri: String, available: Boolean = true) = ExternalTreeGrantEntity(
        treeUri = uri,
        displayName = uri,
        persistedFlags = 1,
        grantedAtMillis = 1L,
        isAvailable = available
    )

    @Test
    fun mainLibraryRefreshSelectsAllAvailableSafTrees() {
        val first = grant("content://provider/tree/first")
        val second = grant("content://provider/tree/second")
        val unavailable = grant("content://provider/tree/offline", available = false)

        assertEquals(
            listOf(first, second),
            LibraryRefreshTargets.select(null, false, listOf(first, second, unavailable))
        )
    }

    @Test
    fun safFolderRefreshSelectsOnlyItsOwningTree() {
        val first = grant("content://provider/tree/first")
        val second = grant("content://provider/tree/second")
        val sourceKey = SafSourceIdentity.key(first.treeUri)

        assertEquals(
            listOf(first),
            LibraryRefreshTargets.select(sourceKey, false, listOf(first, second))
        )
    }

    @Test
    fun mediaStoreFolderRefreshDoesNotRescanSafTrees() {
        val first = grant("content://provider/tree/first")

        assertEquals(
            emptyList<ExternalTreeGrantEntity>(),
            LibraryRefreshTargets.select("mediastore:external", false, listOf(first))
        )
    }

    @Test
    fun externalStoragePageRefreshesAllAvailableTrees() {
        val first = grant("content://provider/tree/first")
        val second = grant("content://provider/tree/second")
        val unavailable = grant("content://provider/tree/offline", available = false)

        assertEquals(
            listOf(first, second),
            LibraryRefreshTargets.select("mediastore:external", true, listOf(first, second, unavailable))
        )
    }
}
