package com.northstar.player.data.library

import com.northstar.player.core.database.MediaItemDao
import com.northstar.player.core.database.MediaItemEntity
import com.northstar.player.core.database.ScanStateDao
import com.northstar.player.core.database.ScanStateEntity
import kotlinx.coroutines.flow.Flow

interface MediaLibraryRepository {
    fun observeAvailableVideos(): Flow<List<MediaItemEntity>>
    suspend fun findVideo(mediaKey: String): MediaItemEntity?
    suspend fun saveVideos(items: List<MediaItemEntity>)
    suspend fun saveScanState(state: ScanStateEntity)
    suspend fun getScanState(sourceKey: String): ScanStateEntity?
    suspend fun markSourceUnavailable(sourceKey: String)
}

class RoomMediaLibraryRepository(
    private val mediaDao: MediaItemDao,
    private val scanStateDao: ScanStateDao
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
}
