package com.northstar.player

import android.app.Application
import com.northstar.player.core.database.NorthstarDatabase
import com.northstar.player.core.permissions.VideoPermissionChecker
import com.northstar.player.data.library.MediaLibraryRepository
import com.northstar.player.data.library.MediaStoreVideoScanner
import com.northstar.player.data.library.MediaLibraryScanScheduler
import com.northstar.player.data.library.RoomMediaLibraryRepository
import com.northstar.player.data.storage.SafTreeGrantManager
import com.northstar.player.data.storage.SafTreeScanner
import com.northstar.player.data.storage.SafFolderManager

class NorthstarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        mediaLibraryScanScheduler.ensurePeriodicScan()
    }
    val database: NorthstarDatabase by lazy {
        NorthstarDatabase.getInstance(this)
    }

    val mediaLibraryRepository: MediaLibraryRepository by lazy {
        RoomMediaLibraryRepository(
            database.mediaItemDao(),
            database.scanStateDao(),
            database.folderDao()
        )
    }

    val videoPermissionChecker: VideoPermissionChecker by lazy {
        VideoPermissionChecker(this)
    }

    val mediaLibraryScanScheduler: MediaLibraryScanScheduler by lazy {
        MediaLibraryScanScheduler(this)
    }

    val mediaStoreVideoScanner: MediaStoreVideoScanner by lazy {
        MediaStoreVideoScanner(
            contentResolver,
            mediaLibraryRepository
        )
    }

    val safTreeGrantManager: SafTreeGrantManager by lazy {
        SafTreeGrantManager(contentResolver, database.externalTreeGrantDao())
    }

    val safTreeScanner: SafTreeScanner by lazy {
        SafTreeScanner(
            contentResolver,
            database.mediaItemDao(),
            database.folderDao(),
            database.scanStateDao(),
            database.safSnapshotDao(),
            database.externalTreeGrantDao()
        )
    }

    val safFolderManager: SafFolderManager by lazy {
        SafFolderManager(contentResolver, database.externalTreeGrantDao())
    }
}
