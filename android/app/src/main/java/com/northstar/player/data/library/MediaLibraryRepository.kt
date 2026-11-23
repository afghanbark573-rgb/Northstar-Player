package com.northstar.player.data.library

import com.northstar.player.core.database.FolderDao
import com.northstar.player.core.database.FolderEntity
import com.northstar.player.core.database.MediaItemDao
import com.northstar.player.core.database.MediaItemEntity
import com.northstar.player.core.database.ScanStateDao
import com.northstar.player.core.database.ScanStateEntity
import com.northstar.player.core.permissions.VideoPermissionScope
import kotlinx.coroutines.flow.Flow

interface MediaLibraryRepository {
    fun observeAvailableVideos(): Flow<List<MediaItemEntity>>
    suspend fun findVideo(mediaKey: String): MediaItemEntity?
    suspend fun saveVideos(items: List<MediaItemEntity>)
    suspend fun saveScanState(state: ScanStateEntity)
    suspend fun getScanState(sourceKey: String): ScanStateEntity?
    suspend fun markSourceUnavailable(sourceKey: String)
    suspend fun refreshFolderIndex(sourceKey: String, permissionScope: VideoPermissionScope, completedAtMillis: Long)
}

class RoomMediaLibraryRepository(
    private val mediaDao: MediaItemDao,
    private val scanStateDao: ScanStateDao,
    private val folderDao: FolderDao
) : MediaLibraryRepository {
    override fun observeAvailableVideos(): Flow<List<MediaItemEntity>> =
        mediaDao.observeAvailable()

    override suspend fun findVideo(mediaKey: String): MediaItemEntity? =
        mediaDao.findByKey(mediaKey)

    override suspend fun saveVideos(items: List<MediaItemEntity>) {
        if (items.isNotEmpty()) mediaDao.upsertAll(items)
    }

    override suspend fun saveScanState(state: ScanStateEntity) =
        scanStateDao.save(state)

    override suspend fun getScanState(sourceKey: String): ScanStateEntity? =
        scanStateDao.get(sourceKey)

    override suspend fun markSourceUnavailable(sourceKey: String) =
        mediaDao.markSourceUnavailable(sourceKey)

    override suspend fun refreshFolderIndex(
        sourceKey: String,
        permissionScope: VideoPermissionScope,
        completedAtMillis: Long
    ) {
        val indexed = MediaStoreFolderIndexer.build(
            mediaDao.availableForSource(sourceKey),
            sourceKey
        )
        val previous = folderDao.findForSource(sourceKey).associateBy { it.folderKey }
        val refreshed = indexed.map { folder ->
            val old = previous[folder.folderKey]
            folder.copy(
                discoveredAtMillis = old?.discoveredAtMillis ?: completedAtMillis,
                acknowledgedAtMillis = old?.acknowledgedAtMillis
            )
        }

        if (permissionScope == VideoPermissionScope.FULL) {
            // A complete library scan is the only evidence that a folder disappeared.
            folderDao.replaceForSource(sourceKey, refreshed)
        } else {
            // Partial visibility cannot prove that previously indexed folders vanished.
            if (refreshed.isNotEmpty()) folderDao.upsertAll(refreshed)
        }
    }
}
