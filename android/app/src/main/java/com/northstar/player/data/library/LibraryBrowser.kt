package com.northstar.player.data.library

import com.northstar.player.core.database.FolderEntity
import com.northstar.player.core.database.MediaItemEntity
import java.util.Locale

/** The three user-approved public library modes. */
enum class LibraryViewMode { VIDEO_FOLDERS, FILES_ONLY, NESTED_VIEW }

enum class LibrarySort { DATE_ADDED, DATE_MODIFIED, NAME, LARGEST }

data class LibraryListing(
    val folders: List<FolderEntity>,
    val videos: List<MediaItemEntity>
)

/**
 * Pure presentation projection over persisted Room records.
 *
 * Folder ordering is alphabetical for now: FolderEntity does not yet retain aggregate
 * byte size or reliable folder-level dates, so inventing Largest/Date ordering for
 * folders would be misleading. Video ordering supports all four approved sort choices.
 */
object LibraryBrowser {
    fun build(
        allVideos: List<MediaItemEntity>,
        allFolders: List<FolderEntity>,
        mode: LibraryViewMode,
        sort: LibrarySort,
        query: String = "",
        currentFolderKey: String? = null
    ): LibraryListing {
        val available = allVideos.filter { it.isAvailable }
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)

        fun matchesFolder(folder: FolderEntity): Boolean =
            normalizedQuery.isEmpty() ||
                folder.displayName.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                folder.relativePath.lowercase(Locale.ROOT).contains(normalizedQuery)

        fun matchesVideo(video: MediaItemEntity): Boolean =
            normalizedQuery.isEmpty() ||
                video.displayName.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                video.relativePath.orEmpty().lowercase(Locale.ROOT).contains(normalizedQuery)

        val folders = when (mode) {
            LibraryViewMode.VIDEO_FOLDERS -> allFolders.filter {
                it.parentFolderKey == null && it.videoCount > 0 && matchesFolder(it)
            }
            LibraryViewMode.FILES_ONLY -> emptyList()
            LibraryViewMode.NESTED_VIEW -> allFolders.filter {
                it.parentFolderKey == currentFolderKey && matchesFolder(it)
            }
        }.sortedBy { it.displayName.lowercase(Locale.ROOT) }

        val videos = when (mode) {
            LibraryViewMode.VIDEO_FOLDERS -> emptyList()
            LibraryViewMode.FILES_ONLY -> available.filter(::matchesVideo)
            LibraryViewMode.NESTED_VIEW -> {
                val currentPath = currentFolderKey?.let { key ->
                    allFolders.firstOrNull { it.folderKey == key }?.relativePath
                }
                available.filter { video ->
                    val videoPath = video.relativePath.orEmpty().trimEnd('/').replace('\\', '/')
                    val parentPath = currentPath.orEmpty().trimEnd('/').replace('\\', '/')
                    videoPath == parentPath && matchesVideo(video)
                }
            }
        }.let { list ->
            when (sort) {
                LibrarySort.DATE_ADDED -> list.sortedByDescending { it.dateAddedSeconds ?: Long.MIN_VALUE }
                LibrarySort.DATE_MODIFIED -> list.sortedByDescending { it.dateModifiedSeconds ?: Long.MIN_VALUE }
                LibrarySort.NAME -> list.sortedBy { it.displayName.lowercase(Locale.ROOT) }
                LibrarySort.LARGEST -> list.sortedByDescending { it.sizeBytes }
            }
        }

        return LibraryListing(folders, videos)
    }
}
