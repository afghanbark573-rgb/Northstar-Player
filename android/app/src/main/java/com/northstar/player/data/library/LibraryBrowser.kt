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
 * Folder metadata is aggregated in one pass over available videos. Largest is the sum of
 * descendant byte sizes; date sorts use the newest provider-supplied descendant date.
 * Missing dates remain unknown and sort after known dates.
 */
object LibraryBrowser {
    private data class FolderIdentity(val sourceKey: String, val relativePath: String)

    private data class FolderMetrics(
        val totalBytes: Long,
        val newestAdded: Long?,
        val newestModified: Long?
    )

    private class MutableFolderMetrics {
        var totalBytes: Long = 0L
        var newestAdded: Long? = null
        var newestModified: Long? = null

        fun include(video: MediaItemEntity) {
            totalBytes += video.sizeBytes
            newestAdded = newest(newestAdded, video.dateAddedSeconds)
            newestModified = newest(newestModified, video.dateModifiedSeconds)
        }

        fun freeze() = FolderMetrics(totalBytes, newestAdded, newestModified)
    }

    private fun newest(current: Long?, candidate: Long?): Long? = when {
        candidate == null -> current
        current == null -> candidate
        else -> maxOf(current, candidate)
    }

    private fun compareNewest(left: Long?, right: Long?): Int = when {
        left == null && right == null -> 0
        left == null -> 1
        right == null -> -1
        else -> right.compareTo(left)
    }

    private fun normalizePath(path: String?): String =
        path.orEmpty()
            .replace('\\', '/')
            .split('/')
            .filter { it.isNotBlank() && it != "." }
            .joinToString("/")

    private fun compareNames(left: String, right: String): Int {
        val insensitive = left.compareTo(right, ignoreCase = true)
        return if (insensitive != 0) insensitive else left.compareTo(right)
    }

    private fun buildFolderMetrics(
        availableVideos: List<MediaItemEntity>,
        allFolders: List<FolderEntity>
    ): Map<String, FolderMetrics> {
        val byIdentity = HashMap<FolderIdentity, MutableFolderMetrics>()

        // Each video contributes once to its own directory and each ancestor, regardless
        // of how many folder rows exist. This avoids an O(folderCount × videoCount) scan.
        availableVideos.forEach { video ->
            val segments = normalizePath(video.relativePath).split('/').filter { it.isNotEmpty() }
            var ancestorPath = ""
            val ancestors = ArrayList<String>(segments.size + 1)
            ancestors += ancestorPath
            segments.forEach { segment ->
                ancestorPath = if (ancestorPath.isEmpty()) segment else "$ancestorPath/$segment"
                ancestors += ancestorPath
            }

            ancestors.forEach { path ->
                byIdentity.getOrPut(FolderIdentity(video.sourceKey, path)) {
                    MutableFolderMetrics()
                }.include(video)
            }
        }

        return allFolders.associate { folder ->
            val identity = FolderIdentity(folder.sourceKey, normalizePath(folder.relativePath))
            folder.folderKey to (byIdentity[identity]?.freeze() ?: FolderMetrics(0L, null, null))
        }
    }

    private fun compareFolders(
        left: FolderEntity,
        right: FolderEntity,
        sort: LibrarySort,
        metrics: Map<String, FolderMetrics>
    ): Int {
        val leftMetrics = metrics.getValue(left.folderKey)
        val rightMetrics = metrics.getValue(right.folderKey)
        val primary = when (sort) {
            LibrarySort.DATE_ADDED -> compareNewest(leftMetrics.newestAdded, rightMetrics.newestAdded)
            LibrarySort.DATE_MODIFIED -> compareNewest(leftMetrics.newestModified, rightMetrics.newestModified)
            LibrarySort.NAME -> compareNames(left.displayName, right.displayName)
            LibrarySort.LARGEST -> rightMetrics.totalBytes.compareTo(leftMetrics.totalBytes)
        }
        if (primary != 0) return primary

        val byName = compareNames(left.displayName, right.displayName)
        return if (byName != 0) byName else left.folderKey.compareTo(right.folderKey)
    }

    private fun compareVideos(
        left: MediaItemEntity,
        right: MediaItemEntity,
        sort: LibrarySort
    ): Int {
        val primary = when (sort) {
            LibrarySort.DATE_ADDED -> compareNewest(left.dateAddedSeconds, right.dateAddedSeconds)
            LibrarySort.DATE_MODIFIED -> compareNewest(left.dateModifiedSeconds, right.dateModifiedSeconds)
            LibrarySort.NAME -> compareNames(left.displayName, right.displayName)
            LibrarySort.LARGEST -> right.sizeBytes.compareTo(left.sizeBytes)
        }
        if (primary != 0) return primary

        val byName = compareNames(left.displayName, right.displayName)
        return if (byName != 0) byName else left.mediaKey.compareTo(right.mediaKey)
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

        fun matchesFolder(folder: FolderEntity): Boolean =
            normalizedQuery.isEmpty() ||
                folder.displayName.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                normalizePath(folder.relativePath).lowercase(Locale.ROOT).contains(normalizedQuery)

        fun matchesVideo(video: MediaItemEntity): Boolean =
            normalizedQuery.isEmpty() ||
                video.displayName.lowercase(Locale.ROOT).contains(normalizedQuery) ||
                normalizePath(video.relativePath).lowercase(Locale.ROOT).contains(normalizedQuery)

        val folderMetrics = buildFolderMetrics(available, allFolders)

        val folders = when (mode) {
            LibraryViewMode.VIDEO_FOLDERS -> allFolders.filter {
                it.parentFolderKey == null && it.videoCount > 0 && matchesFolder(it)
            }
            LibraryViewMode.FILES_ONLY -> emptyList()
            LibraryViewMode.NESTED_VIEW -> allFolders.filter {
                it.parentFolderKey == currentFolderKey && matchesFolder(it)
            }
        }.sortedWith { left, right -> compareFolders(left, right, sort, folderMetrics) }

        val currentFolder = currentFolderKey?.let { key ->
            allFolders.firstOrNull { it.folderKey == key }
        }
        val currentPath = normalizePath(currentFolder?.relativePath)

        val videos = when (mode) {
            LibraryViewMode.VIDEO_FOLDERS -> emptyList()
            LibraryViewMode.FILES_ONLY -> available.filter(::matchesVideo)
            LibraryViewMode.NESTED_VIEW -> available.filter { video ->
                val videoPath = normalizePath(video.relativePath)
                (currentFolder == null || video.sourceKey == currentFolder.sourceKey) &&
                    videoPath == currentPath && matchesVideo(video)
            }
        }.sortedWith { left, right -> compareVideos(left, right, sort) }

        return LibraryListing(folders, videos)
    }
}
