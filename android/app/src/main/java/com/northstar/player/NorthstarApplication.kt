package com.northstar.player

import android.app.Application
import com.northstar.player.core.database.NorthstarDatabase
import com.northstar.player.core.permissions.VideoPermissionChecker
import com.northstar.player.data.library.MediaLibraryRepository
import com.northstar.player.data.library.MediaStoreVideoScanner
import com.northstar.player.data.library.MediaLibraryScanScheduler
import com.northstar.player.data.library.RoomMediaLibraryRepository

class NorthstarApplication : Application() {\n    override fun onCreate() {\n        super.onCreate()\n        mediaLibraryScanScheduler.ensurePeriodicScan()\n    }
    val database: NorthstarDatabase by lazy {
        NorthstarDatabase.getInstance(this)
    }

    val mediaLibraryRepository: MediaLibraryRepository by lazy {
        RoomMediaLibraryRepository(
            database.mediaItemDao(),
            database.scanStateDao()
        )
    }

    val videoPermissionChecker: VideoPermissionChecker by lazy {
        VideoPermissionChecker(this)
    }

    val mediaLibraryScanScheduler: MediaLibraryScanScheduler by lazy {\n        MediaLibraryScanScheduler(this)\n    }\n\n    val mediaStoreVideoScanner: MediaStoreVideoScanner by lazy {
        MediaStoreVideoScanner(
            contentResolver,
            mediaLibraryRepository
        )
    }
}
