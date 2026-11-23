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
 * Folder metadata is derived from available descendant videos: Largest is the sum of
 * their byte sizes, while date sorts use the newest provider-supplied child date.
 * Missing dates remain unknown and sort after folders with known dates.
 */
object LibraryBrowser {
    private data class FolderMetrics(
        val totalBytes: Long,
        val newestAdded: Long?,
        val newestModified: Long?
    )

    private fun compareNewest(left: Long?, right: Long?): Int = when {
        left == null && right == null -> 0
        left == null -> 1
        right == null -> -1
        else -> right.compareTo(left)
    }

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

        fun normalizedPath(path: String): String =
            path.trimEnd('/').replace('\\', '/')

        fun matchesFolder(folder: FolderEntity): Boolean =
            normalizedQuery.isEmpty() ||
                folder.displayName.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                folder.relativePath.lowercase(Locale.ROOT).contains(normalizedQuery)

        fun matchesVideo(video: MediaItemEntity): Boolean =
            normalizedQuery.isEmpty() ||
                video.displayName.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                video.relativePath.orEmpty().lowercase(Locale.ROOT).contains(normalizedQuery)

        val folderMetrics = allFolders.associate { folder ->
            val path = normalizedPath(folder.relativePath)
            val descendants = available.filter { video ->
                val videoPath = normalizedPath(video.relativePath.orEmpty())
                video.sourceKey == folder.sourceKey &&
                    (path.isEmpty() || videoPath == path || videoPath.startsWith("$path/"))
            }
            folder.folderKey to FolderMetrics(
                totalBytes = descendants.sumOf { it.sizeBytes },
                newestAdded = descendants.mapNotNull { it.dateAddedSeconds }.maxOrNull(),
                newestModified = descendants.mapNotNull { it.dateModifiedSeconds }.maxOrNull()
            )
        }

        val folders = when (mode) {
            LibraryViewMode.VIDEO_FOLDERS -> allFolders.filter {
                it.parentFolderKey == null && it.videoCount > 0 && matchesFolder(it)
            }
            LibraryViewMode.FILES_ONLY -> emptyList()
            LibraryViewMode.NESTED_VIEW -> allFolders.filter {
                it.parentFolderKey == currentFolderKey && matchesFolder(it)
            }
        }.sortedWith { left, right ->
            val leftMetrics = folderMetrics.getValue(left.folderKey)
            val rightMetrics = folderMetrics.getValue(right.folderKey)
            val primary = when (sort) {
                LibrarySort.DATE_ADDED -> compareNewest(leftMetrics.newestAdded, rightMetrics.newestAdded)
                LibrarySort.DATE_MODIFIED -> compareNewest(leftMetrics.newestModified, rightMetrics.newestModified)
                LibrarySort.NAME -> left.displayName.compareTo(right.displayName, ignoreCase = true)
                LibrarySort.LARGEST -> rightMetrics.totalBytes.compareTo(leftMetrics.totalBytes)
            }
            if (primary != 0) primary
            else left.displayName.compareTo(right.displayName, ignoreCase = true)
        }

        val videos = when (mode) {
            LibraryViewMode.VIDEO_FOLDERS -> emptyList()
            LibraryViewMode.FILES_ONLY -> available.filter(::matchesVideo)
            LibraryViewMode.NESTED_VIEW -> {
                val currentFolder = currentFolderKey?.let { key ->
                    allFolders.firstOrNull { it.folderKey == key }
                }
                val currentPath = currentFolder?.relativePath
                available.filter { video ->
                    val videoPath = normalizedPath(video.relativePath.orEmpty())
                    val parentPath = normalizedPath(currentPath.orEmpty())
                    (currentFolder == null || video.sourceKey == currentFolder.sourceKey) &&
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
