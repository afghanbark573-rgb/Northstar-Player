package com.northstar.player.data.library

import com.northstar.player.core.database.*
import com.northstar.player.core.permissions.VideoPermissionScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomMediaLibraryRepositoryTest {
    private class FakeMediaDao : MediaItemDao {
        val items = mutableListOf<MediaItemEntity>()
        override fun observeAvailable(): Flow<List<MediaItemEntity>> = MutableStateFlow(items.filter { it.isAvailable })
        override suspend fun findByKey(mediaKey: String) = items.find { it.mediaKey == mediaKey }
        override suspend fun availableForSource(sourceKey: String) = items.filter { it.sourceKey == sourceKey && it.isAvailable }
        override suspend fun upsertAll(items: List<MediaItemEntity>) {
            items.forEach { item ->
                this.items.removeAll { it.mediaKey == item.mediaKey }
                this.items.add(item)
            }
        }
        override suspend fun markSourceUnavailable(sourceKey: String) {
            for (index in items.indices) if (items[index].sourceKey == sourceKey) {
                items[index] = items[index].copy(isAvailable = false)
            }
        }
        override suspend fun deleteByKey(mediaKey: String) {
            items.removeAll { it.mediaKey == mediaKey }
        }
    }

    private class FakeFolderDao : FolderDao {
        val folders = mutableListOf<FolderEntity>()
        override fun observeAll(): Flow<List<FolderEntity>> = MutableStateFlow(folders.toList())
        override suspend fun findForSource(sourceKey: String) = folders.filter { it.sourceKey == sourceKey }
        override suspend fun upsertAll(folders: List<FolderEntity>) {
            folders.forEach { folder ->
                this.folders.removeAll { it.folderKey == folder.folderKey }
                this.folders.add(folder)
            }
        }
        override suspend fun deleteForSource(sourceKey: String) {
            folders.removeAll { it.sourceKey == sourceKey }
        }
    }

    private class FakeScanStateDao : ScanStateDao {
        override suspend fun get(sourceKey: String): ScanStateEntity? = null
        override suspend fun save(state: ScanStateEntity) = Unit
    }

    private fun video(key: String, path: String) = MediaItemEntity(
        mediaKey = key, uri = "content://media/$key", displayName = "$key.mp4", mimeType = "video/mp4",
        durationMs = 1000, sizeBytes = 100, dateAddedSeconds = 10, dateModifiedSeconds = 20,
        sourceType = "MEDIA_STORE", sourceKey = "media", relativePath = path,
        isAvailable = true, indexedAtMillis = 1000
    )

    private fun repository(media: FakeMediaDao, folders: FakeFolderDao) =
        RoomMediaLibraryRepository(media, FakeScanStateDao(), folders)

    @Test
    fun partialRefreshKeepsPreviouslyIndexedFoldersAndAcknowledgement() = kotlinx.coroutines.runBlocking {
        val media = FakeMediaDao().apply { items += video("new", "Movies/New/") }
        val folders = FakeFolderDao().apply {
            folders += FolderEntity("media|Movies/Old", "media|Movies", "Old", "media", "Movies/Old",
                true, 1, 0, 100L, 200L)
            folders += FolderEntity("media|Movies", null, "Movies", "media", "Movies",
                false, 1, 1, 100L, null)
        }

        repository(media, folders).refreshFolderIndex("media", VideoPermissionScope.PARTIAL, 500L)

        val byPath = folders.folders.associateBy { it.relativePath }
        assertTrue(byPath.containsKey("Movies/Old"))
        assertEquals(200L, byPath.getValue("Movies/Old").acknowledgedAtMillis)
        assertEquals(100L, byPath.getValue("Movies").discoveredAtMillis)
        assertTrue(byPath.containsKey("Movies/New"))
        assertEquals(500L, byPath.getValue("Movies/New").discoveredAtMillis)
    }

    @Test
    fun fullRefreshReplacesFoldersThatAreNoLongerPresent() = kotlinx.coroutines.runBlocking {
        val media = FakeMediaDao().apply { items += video("new", "Movies/New/") }
        val folders = FakeFolderDao().apply {
            folders += FolderEntity("media|Movies/Old", "media|Movies", "Old", "media", "Movies/Old",
                true, 1, 0, 100L, null)
        }

        repository(media, folders).refreshFolderIndex("media", VideoPermissionScope.FULL, 600L)

        assertFalse(folders.folders.any { it.relativePath == "Movies/Old" })
        assertTrue(folders.folders.any { it.relativePath == "Movies/New" })
    }

    @Test
    fun emptyPartialRefreshDoesNotEraseExistingFolders() = kotlinx.coroutines.runBlocking {
        val media = FakeMediaDao()
        val folders = FakeFolderDao().apply {
            folders += FolderEntity("media|Existing", null, "Existing", "media", "Existing",
                true, 1, 0, 100L, null)
        }

        repository(media, folders).refreshFolderIndex("media", VideoPermissionScope.PARTIAL, 700L)

        assertEquals(listOf("Existing"), folders.folders.map { it.relativePath })
    }
}
