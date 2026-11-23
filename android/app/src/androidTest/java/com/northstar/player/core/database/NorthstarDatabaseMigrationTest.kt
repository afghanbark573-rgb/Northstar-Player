package com.northstar.player.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NorthstarDatabaseMigrationTest {
    @Test
    fun migrationFromV1PreservesLibraryAndCreatesIsolatedStagingTable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "northstar-migration-${System.nanoTime()}.db"
        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()

        val legacy = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        try {
            legacy.beginTransaction()
            try {
                LEGACY_V1_SCHEMA.forEach { legacy.execSQL(it) }
                legacy.execSQL("""
                    INSERT INTO media_items (
                        mediaKey, uri, displayName, mimeType, durationMs, sizeBytes,
                        dateAddedSeconds, dateModifiedSeconds, sourceType, sourceKey,
                        relativePath, isAvailable, indexedAtMillis
                    ) VALUES (
                        'saf:test-source|document-1', 'content://test/video/1',
                        'preserved-video.mp4', 'video/mp4', 0, 1234,
                        NULL, 1730000000, 'SAF_TREE', 'saf:test-source',
                        'Movies', 1, 1730000000000
                    )
                """.trimIndent())
                legacy.version = 1
                legacy.setTransactionSuccessful()
            } finally {
                legacy.endTransaction()
            }
        } finally {
            legacy.close()
        }

        val migrated = Room.databaseBuilder(context, NorthstarDatabase::class.java, databaseName)
            .addMigrations(NorthstarDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        try {
            val row = migrated.mediaItemDao().findByKey("saf:test-source|document-1")
            assertNotNull("Existing media row must survive migration", row)
            assertEquals("preserved-video.mp4", row?.displayName)
            assertEquals("content://test/video/1", row?.uri)
            assertEquals(1234L, row?.sizeBytes)

            migrated.openHelper.writableDatabase.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'saf_staged_media'"
            ).use { assertTrue("Staging table must exist", it.moveToFirst()) }
            migrated.openHelper.writableDatabase.query(
                "SELECT COUNT(*) FROM saf_staged_media"
            ).use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
        } finally {
            migrated.close()
            context.deleteDatabase(databaseName)
        }
    }

    private companion object {
        // Exact version-1 table and index definitions from the previously exported Room schema.
        val LEGACY_V1_SCHEMA = listOf(
            "CREATE TABLE IF NOT EXISTS media_items (mediaKey TEXT NOT NULL, uri TEXT NOT NULL, displayName TEXT NOT NULL, mimeType TEXT, durationMs INTEGER NOT NULL, sizeBytes INTEGER NOT NULL, dateAddedSeconds INTEGER, dateModifiedSeconds INTEGER, sourceType TEXT NOT NULL, sourceKey TEXT NOT NULL, relativePath TEXT, isAvailable INTEGER NOT NULL, indexedAtMillis INTEGER NOT NULL, PRIMARY KEY(mediaKey))",
            "CREATE INDEX IF NOT EXISTS index_media_items_sourceKey ON media_items (sourceKey)",
            "CREATE INDEX IF NOT EXISTS index_media_items_relativePath ON media_items (relativePath)",
            "CREATE INDEX IF NOT EXISTS index_media_items_dateAddedSeconds ON media_items (dateAddedSeconds)",
            "CREATE INDEX IF NOT EXISTS index_media_items_dateModifiedSeconds ON media_items (dateModifiedSeconds)",
            "CREATE TABLE IF NOT EXISTS folders (folderKey TEXT NOT NULL, parentFolderKey TEXT, displayName TEXT NOT NULL, sourceKey TEXT NOT NULL, relativePath TEXT NOT NULL, containsVideos INTEGER NOT NULL, videoCount INTEGER NOT NULL, childFolderCount INTEGER NOT NULL, discoveredAtMillis INTEGER, acknowledgedAtMillis INTEGER, PRIMARY KEY(folderKey))",
            "CREATE INDEX IF NOT EXISTS index_folders_parentFolderKey ON folders (parentFolderKey)",
            "CREATE INDEX IF NOT EXISTS index_folders_sourceKey ON folders (sourceKey)",
            "CREATE TABLE IF NOT EXISTS favorites (targetKey TEXT NOT NULL, targetType TEXT NOT NULL, createdAtMillis INTEGER NOT NULL, PRIMARY KEY(targetKey))",
            "CREATE INDEX IF NOT EXISTS index_favorites_targetKey ON favorites (targetKey)",
            "CREATE TABLE IF NOT EXISTS playback_progress (mediaKey TEXT NOT NULL, positionMs INTEGER NOT NULL, durationMs INTEGER NOT NULL, isCompleted INTEGER NOT NULL, updatedAtMillis INTEGER NOT NULL, PRIMARY KEY(mediaKey))",
            "CREATE TABLE IF NOT EXISTS watch_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, mediaKey TEXT NOT NULL, playedAtMillis INTEGER NOT NULL, lastPositionMs INTEGER NOT NULL)",
            "CREATE INDEX IF NOT EXISTS index_watch_history_mediaKey ON watch_history (mediaKey)",
            "CREATE INDEX IF NOT EXISTS index_watch_history_playedAtMillis ON watch_history (playedAtMillis)",
            "CREATE TABLE IF NOT EXISTS scan_states (sourceKey TEXT NOT NULL, lastSuccessfulScanMillis INTEGER, permissionScope TEXT NOT NULL, inProgressSinceMillis INTEGER, lastError TEXT, PRIMARY KEY(sourceKey))",
            "CREATE TABLE IF NOT EXISTS new_content_states (contentKey TEXT NOT NULL, folderKey TEXT NOT NULL, discoveredAtMillis INTEGER NOT NULL, acknowledgedAtMillis INTEGER, PRIMARY KEY(contentKey))",
            "CREATE INDEX IF NOT EXISTS index_new_content_states_folderKey ON new_content_states (folderKey)",
            "CREATE TABLE IF NOT EXISTS external_tree_grants (treeUri TEXT NOT NULL, displayName TEXT NOT NULL, persistedFlags INTEGER NOT NULL, grantedAtMillis INTEGER NOT NULL, isAvailable INTEGER NOT NULL, PRIMARY KEY(treeUri))"
        )
    }
}
