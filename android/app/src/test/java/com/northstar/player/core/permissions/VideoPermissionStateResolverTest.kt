package com.northstar.player.core.permissions

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoPermissionStateResolverTest {
    @Test
    fun android12AndBelowRequiresLegacyPermission() {
        assertEquals(
            VideoPermissionScope.FULL,
            VideoPermissionStateResolver.resolve(
                32,
                setOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        )
    }

    @Test
    fun android13VideoPermissionMeansFullAccess() {
        assertEquals(
            VideoPermissionScope.FULL,
            VideoPermissionStateResolver.resolve(
                33,
                setOf(Manifest.permission.READ_MEDIA_VIDEO)
            )
        )
    }

    @Test
    fun android14SelectedMediaPermissionMeansPartialAccess() {
        assertEquals(
            VideoPermissionScope.PARTIAL,
            VideoPermissionStateResolver.resolve(
                34,
                setOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            )
        )
    }

    @Test
    fun android14VideoPermissionMeansFullAccess() {
        assertEquals(
            VideoPermissionScope.FULL,
            VideoPermissionStateResolver.resolve(
                34,
                setOf(Manifest.permission.READ_MEDIA_VIDEO)
            )
        )
    }

    @Test
    fun missingPermissionMeansDenied() {
        assertEquals(
            VideoPermissionScope.DENIED,
            VideoPermissionStateResolver.resolve(35, emptySet())
        )
    }
}
