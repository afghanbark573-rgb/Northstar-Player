package com.northstar.player.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NorthstarDatabaseMigrationTest {
    @Test
    fun migrationFromV1ToV3PreservesLibraryAndAddsIncrementalCheckpointColumns() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "northstar-migration-${System.nanoTime()}.db"
        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()

        val legacy = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
        try {
            legacy.beginTransaction()
            try {
                LEGACY_V1_SCHEMA.forEach { legacy.execSQL(it) }
                legacy.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
                legacy.execSQL("INSERT INTO room_master_table (id, identity_hash) VALUES (42, 'dac46e644923f0530ef1f30a00e91ca1')")
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
                legacy.execSQL("INSERT INTO favorites (targetKey, targetType, createdAtMillis) VALUES ('favorite-1', 'VIDEO', 1000)")
                legacy.execSQL("INSERT INTO playback_progress (mediaKey, positionMs, durationMs, isCompleted, updatedAtMillis) VALUES ('saf:test-source|document-1', 5000, 60000, 0, 2000)")
                legacy.execSQL("INSERT INTO watch_history (mediaKey, playedAtMillis, lastPositionMs) VALUES ('saf:test-source|document-1', 3000, 5000)")
                legacy.execSQL("INSERT INTO folders (folderKey, parentFolderKey, displayName, sourceKey, relativePath, containsVideos, videoCount, childFolderCount, discoveredAtMillis, acknowledgedAtMillis) VALUES ('folder-1', NULL, 'Movies', 'saf:test-source', 'Movies', 1, 1, 0, 1000, NULL)")
                legacy.execSQL("INSERT INTO scan_states (sourceKey, lastSuccessfulScanMillis, permissionScope, inProgressSinceMillis, lastError) VALUES ('saf:test-source', 900, 'SAF_TREE', NULL, NULL)")
                legacy.execSQL("INSERT INTO new_content_states (contentKey, folderKey, discoveredAtMillis, acknowledgedAtMillis) VALUES ('new-1', 'folder-1', 1100, NULL)")
                legacy.execSQL("INSERT INTO external_tree_grants (treeUri, displayName, persistedFlags, grantedAtMillis, isAvailable) VALUES ('content://tree/test', 'USB', 1, 1000, 1)")
                legacy.version = 1
                legacy.setTransactionSuccessful()
            } finally {
                legacy.endTransaction()
            }
        } finally {
            legacy.close()
        }

        val migrated = Room.databaseBuilder(context, NorthstarDatabase::class.java, databaseName)
            .addMigrations(NorthstarDatabase.MIGRATION_1_2, NorthstarDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
        try {
            val row = migrated.mediaItemDao().findByKey("saf:test-source|document-1")
            assertNotNull("Existing media row must survive migration", row)
            assertEquals("preserved-video.mp4", row?.displayName)
            assertEquals("content://test/video/1", row?.uri)
            assertEquals(1234L, row?.sizeBytes)

            migrated.folderDao().acknowledge("folder-1")
            val acknowledgedFolder = migrated.folderDao().findForSource("saf:test-source").single()
            assertEquals(1000L, acknowledgedFolder.acknowledgedAtMillis)

            val db = migrated.openHelper.writableDatabase
            assertCount(db, "favorites", 1)
            assertCount(db, "playback_progress", 1)
            assertCount(db, "watch_history", 1)
            assertCount(db, "folders", 1)
            assertCount(db, "scan_states", 1)
            assertCount(db, "new_content_states", 1)
            assertCount(db, "external_tree_grants", 1)
            db.query("PRAGMA table_info(scan_states)").use { cursor ->
                val names = buildSet {
                    val nameColumn = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) add(cursor.getString(nameColumn))
                }
                assertTrue("MediaStore version checkpoint column must exist", "mediaStoreVersion" in names)
                assertTrue("MediaStore generation checkpoint column must exist", "mediaStoreGeneration" in names)
            }
            val migratedScanState = migrated.scanStateDao().get("saf:test-source")
            assertNotNull("Existing scan state must survive migration", migratedScanState)
            assertEquals(null, migratedScanState?.mediaStoreVersion)
            assertEquals(null, migratedScanState?.mediaStoreGeneration)

            db.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'saf_staged_media'"
            ).use { assertTrue("Staging table must exist", it.moveToFirst()) }
            db.query(
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

    @Test
    fun mediaStoreSnapshotPublicationHidesMissingRowsWithoutDeletingTheirMetadata() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context, NorthstarDatabase::class.java
        ).allowMainThreadQueries().build()
        try {
            val sourceKey = "mediastore:external"
            fun media(key: String, name: String) = MediaItemEntity(
                mediaKey = key,
                uri = "content://media/$key",
                displayName = name,
                mimeType = "video/mp4",
                durationMs = 1000L,
                sizeBytes = 100L,
                dateAddedSeconds = 10L,
                dateModifiedSeconds = 20L,
                sourceType = "MEDIA_STORE",
                sourceKey = sourceKey,
                relativePath = "Movies",
                isAvailable = true,
                indexedAtMillis = 100L
            )

            database.mediaItemDao().upsertAll(
                listOf(media("kept", "kept.mp4"), media("removed", "removed.mp4"))
            )
            database.safSnapshotDao().stageMedia(
                listOf(
                    SafStagedMediaEntity(
                        sourceKey, 200L, "kept", "content://media/kept", "kept.mp4",
                        "video/mp4", 1000L, 100L, 10L, 20L, "MEDIA_STORE", "Movies"
                    ),
                    SafStagedMediaEntity(
                        sourceKey, 200L, "added", "content://media/added", "added.mp4",
                        "video/mp4", 1000L, 100L, 10L, 20L, "MEDIA_STORE", "Movies"
                    )
                )
            )

            database.safSnapshotDao().publishMediaStoreFullSnapshot(
                sourceKey,
                200L,
                ScanStateEntity(sourceKey, 200L, "FULL", null, null, "volume-v1", 22L)
            )

            val committedState = database.scanStateDao().get(sourceKey)
            assertEquals("volume-v1", committedState?.mediaStoreVersion)
            assertEquals(22L, committedState?.mediaStoreGeneration)
            val visible = database.mediaItemDao().availableForSource(sourceKey).map { it.mediaKey }.toSet()
            assertEquals(setOf("kept", "added"), visible)
            val removed = database.mediaItemDao().findByKey("removed")
            assertNotNull("Inaccessible media metadata must be retained", removed)
            assertEquals(false, removed?.isAvailable == true)
            database.openHelper.writableDatabase.query(
                "SELECT COUNT(*) FROM saf_staged_media WHERE sourceKey = '$sourceKey'"
            ).use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
        } finally {
            database.close()
        }
    }

    @Test
    fun abandonedMediaStoreStageCanBeDiscardedWithoutChangingVisibleSnapshot() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context, NorthstarDatabase::class.java
        ).allowMainThreadQueries().build()
        try {
            val sourceKey = "mediastore:external"
            val original = MediaItemEntity(
                "original", "content://media/original", "original.mp4", "video/mp4",
                1000L, 100L, 10L, 20L, "MEDIA_STORE", sourceKey, "Movies", true, 100L
            )
            database.mediaItemDao().upsertAll(listOf(original))
            database.safSnapshotDao().stageMedia(
                listOf(
                    SafStagedMediaEntity(
                        sourceKey, 300L, "incomplete", "content://media/incomplete",
                        "incomplete.mp4", "video/mp4", 1000L, 100L, 10L, 20L,
                        "MEDIA_STORE", "Movies"
                    )
                )
            )

            database.safSnapshotDao().clearStagedForSource(sourceKey)

            assertEquals(true, database.mediaItemDao().findByKey("original")?.isAvailable)
            assertTrue(database.mediaItemDao().findByKey("incomplete") == null)
        } finally {
            database.close()
        }
    }

    
    @Test
    fun mediaStoreDeltaUpdatesChangedRowsWithoutHidingUnchangedRowsAndCommitsCheckpoint() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(
            context, NorthstarDatabase::class.java
        ).allowMainThreadQueries().build()
        try {
            val sourceKey = "mediastore:external"
            fun media(key: String, name: String) = MediaItemEntity(
                key, "content://media/$key", name, "video/mp4", 1000L, 100L,
                10L, 20L, "MEDIA_STORE", sourceKey, "Movies", true, 100L
            )
            database.mediaItemDao().upsertAll(
                listOf(media("unchanged", "unchanged.mp4"), media("changed", "old-name.mp4"))
            )
            database.safSnapshotDao().stageMedia(
                listOf(
                    SafStagedMediaEntity(
                        sourceKey, 400L, "changed", "content://media/changed",
                        "new-name.mp4", "video/mp4", 1000L, 100L, 10L, 30L,
                        "MEDIA_STORE", "Movies"
                    ),
                    SafStagedMediaEntity(
                        sourceKey, 400L, "added", "content://media/added",
                        "added.mp4", "video/mp4", 1000L, 100L, 10L, 30L,
                        "MEDIA_STORE", "Movies"
                    )
                )
            )
            val checkpoint = ScanStateEntity(
                sourceKey, 500L, "FULL", null, null, "volume-v1", 55L
            )

            database.safSnapshotDao().publishMediaStoreDelta(sourceKey, 400L, checkpoint)

            val available = database.mediaItemDao().availableForSource(sourceKey)
                .associateBy { it.mediaKey }
            assertEquals(setOf("unchanged", "changed", "added"), available.keys)
            assertEquals("new-name.mp4", available["changed"]?.displayName)
            assertEquals("unchanged.mp4", available["unchanged"]?.displayName)
            val saved = database.scanStateDao().get(sourceKey)
            assertEquals("volume-v1", saved?.mediaStoreVersion)
            assertEquals(55L, saved?.mediaStoreGeneration)
            database.openHelper.writableDatabase.query(
                "SELECT COUNT(*) FROM saf_staged_media WHERE sourceKey = '$sourceKey'"
            ).use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
        } finally {
            database.close()
        }
    }

    private fun assertCount(db: androidx.sqlite.db.SupportSQLiteDatabase, table: String, expected: Int) {
        db.query("SELECT COUNT(*) FROM `$table`").use {
            assertTrue(it.moveToFirst())
            assertEquals("Unexpected row count in $table", expected, it.getInt(0))
        }
    }

    private companion object {
        // Exact version-1 table and index definitions from the exported Room schema.
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
