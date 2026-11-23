package com.northstar.player.core.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "media_items",
    indices = [Index("sourceKey"), Index("relativePath"), Index("dateAddedSeconds"), Index("dateModifiedSeconds")]
)
data class MediaItemEntity(
    @PrimaryKey val mediaKey: String,
    val uri: String,
    val displayName: String,
    val mimeType: String?,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSeconds: Long?,
    val dateModifiedSeconds: Long?,
    val sourceType: String,
    val sourceKey: String,
    val relativePath: String?,
    val isAvailable: Boolean = true,
    val indexedAtMillis: Long
)

@Entity(tableName = "folders", indices = [Index("parentFolderKey"), Index("sourceKey")])
data class FolderEntity(
    @PrimaryKey val folderKey: String,
    val parentFolderKey: String?,
    val displayName: String,
    val sourceKey: String,
    val relativePath: String,
    val containsVideos: Boolean,
    val videoCount: Int,
    val childFolderCount: Int,
    val discoveredAtMillis: Long?,
    val acknowledgedAtMillis: Long?
)

@Entity(tableName = "favorites", indices = [Index("targetKey")])
data class FavoriteEntity(
    @PrimaryKey val targetKey: String,
    val targetType: String,
    val createdAtMillis: Long
)

@Entity(tableName = "playback_progress")
data class PlaybackProgressEntity(
    @PrimaryKey val mediaKey: String,
    val positionMs: Long,
    val durationMs: Long,
    val isCompleted: Boolean,
    val updatedAtMillis: Long
)

@Entity(tableName = "watch_history", indices = [Index("mediaKey"), Index("playedAtMillis")])
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaKey: String,
    val playedAtMillis: Long,
    val lastPositionMs: Long
)

@Entity(tableName = "scan_states")
data class ScanStateEntity(
    @PrimaryKey val sourceKey: String,
    val lastSuccessfulScanMillis: Long?,
    val permissionScope: String,
    val inProgressSinceMillis: Long?,
    val lastError: String?
)

@Entity(tableName = "new_content_states", indices = [Index("folderKey")])
data class NewContentStateEntity(
    @PrimaryKey val contentKey: String,
    val folderKey: String,
    val discoveredAtMillis: Long,
    val acknowledgedAtMillis: Long?
)

@Entity(tableName = "external_tree_grants")
data class ExternalTreeGrantEntity(
    @PrimaryKey val treeUri: String,
    val displayName: String,
    val persistedFlags: Int,
    val grantedAtMillis: Long,
    val isAvailable: Boolean
)

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items WHERE isAvailable = 1 ORDER BY dateAddedSeconds DESC")
    fun observeAvailable(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE mediaKey = :mediaKey LIMIT 1")
    suspend fun findByKey(mediaKey: String): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE sourceKey = :sourceKey AND isAvailable = 1")
    suspend fun availableForSource(sourceKey: String): List<MediaItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MediaItemEntity>)

    @Query("UPDATE media_items SET isAvailable = 0 WHERE sourceKey = :sourceKey")
    suspend fun markSourceUnavailable(sourceKey: String)

    @Query("UPDATE media_items SET isAvailable = 1 WHERE sourceKey = :sourceKey AND indexedAtMillis = :scanStartedAtMillis")
    suspend fun activateStagedItems(sourceKey: String, scanStartedAtMillis: Long)

    @Query("UPDATE media_items SET isAvailable = 0 WHERE sourceKey = :sourceKey AND indexedAtMillis < :scanStartedAtMillis")
    suspend fun markOlderItemsUnavailable(sourceKey: String, scanStartedAtMillis: Long)

    @Query("DELETE FROM media_items WHERE mediaKey = :mediaKey")
    suspend fun deleteByKey(mediaKey: String)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY relativePath COLLATE NOCASE")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE sourceKey = :sourceKey")
    suspend fun findForSource(sourceKey: String): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(folders: List<FolderEntity>)

    @Query("DELETE FROM folders WHERE sourceKey = :sourceKey")
    suspend fun deleteForSource(sourceKey: String)

    @Transaction
    suspend fun replaceForSource(sourceKey: String, folders: List<FolderEntity>) {
        deleteForSource(sourceKey)
        if (folders.isNotEmpty()) upsertAll(folders)
    }
}

@Dao
interface UserMediaDao {
    @Query("SELECT * FROM favorites ORDER BY createdAtMillis DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE targetKey = :targetKey")
    suspend fun removeFavorite(targetKey: String)

    @Query("SELECT * FROM playback_progress WHERE mediaKey = :mediaKey LIMIT 1")
    suspend fun progressFor(mediaKey: String): PlaybackProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: PlaybackProgressEntity)

    @Query("SELECT * FROM watch_history ORDER BY playedAtMillis DESC LIMIT :limit")
    fun observeHistory(limit: Int): Flow<List<WatchHistoryEntity>>

    @Insert
    suspend fun addHistoryEntry(entry: WatchHistoryEntity)
}

@Dao
interface ScanStateDao {
    @Query("SELECT * FROM scan_states WHERE sourceKey = :sourceKey LIMIT 1")
    suspend fun get(sourceKey: String): ScanStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: ScanStateEntity)
}

@Dao
interface ExternalTreeGrantDao {
    @Query("SELECT * FROM external_tree_grants ORDER BY displayName COLLATE NOCASE")
    fun observeAll(): Flow<List<ExternalTreeGrantEntity>>

    @Query("SELECT * FROM external_tree_grants ORDER BY displayName COLLATE NOCASE")
    suspend fun all(): List<ExternalTreeGrantEntity>

    @Query("SELECT * FROM external_tree_grants WHERE treeUri = :treeUri LIMIT 1")
    suspend fun find(treeUri: String): ExternalTreeGrantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(grant: ExternalTreeGrantEntity)

    @Query("UPDATE external_tree_grants SET isAvailable = :available WHERE treeUri = :treeUri")
    suspend fun setAvailability(treeUri: String, available: Boolean)

    @Query("DELETE FROM external_tree_grants WHERE treeUri = :treeUri")
    suspend fun delete(treeUri: String)
}

@Database(
    entities = [
        MediaItemEntity::class,
        FolderEntity::class,
        FavoriteEntity::class,
        PlaybackProgressEntity::class,
        WatchHistoryEntity::class,
        ScanStateEntity::class,
        NewContentStateEntity::class,
        ExternalTreeGrantEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NorthstarDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun folderDao(): FolderDao
    abstract fun userMediaDao(): UserMediaDao
    abstract fun scanStateDao(): ScanStateDao
    abstract fun externalTreeGrantDao(): ExternalTreeGrantDao

    companion object {
        @Volatile private var instance: NorthstarDatabase? = null

        fun getInstance(context: Context): NorthstarDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NorthstarDatabase::class.java,
                    "northstar.db"
                ).build().also { instance = it }
            }
    }
}
