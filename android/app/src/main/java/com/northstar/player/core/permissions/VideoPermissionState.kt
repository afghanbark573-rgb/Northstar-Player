package com.northstar.player.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

enum class VideoPermissionScope {
    FULL,
    PARTIAL,
    DENIED
}

/** Pure permission mapping so Android-version rules can be unit tested. */
object VideoPermissionStateResolver {
    fun resolve(apiLevel: Int, grantedPermissions: Set<String>): VideoPermissionScope =
        when {
            apiLevel >= 34 && Manifest.permission.READ_MEDIA_VIDEO in grantedPermissions ->
                VideoPermissionScope.FULL
            apiLevel >= 34 &&
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED in grantedPermissions ->
                VideoPermissionScope.PARTIAL
            apiLevel == 33 && Manifest.permission.READ_MEDIA_VIDEO in grantedPermissions ->
                VideoPermissionScope.FULL
            apiLevel <= 32 && Manifest.permission.READ_EXTERNAL_STORAGE in grantedPermissions ->
                VideoPermissionScope.FULL
            else -> VideoPermissionScope.DENIED
        }
}

class VideoPermissionChecker(private val context: Context) {
    fun currentScope(): VideoPermissionScope {
        val granted = buildSet {
            val permissions = when {
                Build.VERSION.SDK_INT >= 34 -> listOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
                Build.VERSION.SDK_INT == 33 -> listOf(Manifest.permission.READ_MEDIA_VIDEO)
                else -> listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissions.forEach { permission ->
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    add(permission)
                }
            }
        }
        return VideoPermissionStateResolver.resolve(Build.VERSION.SDK_INT, granted)
    }
}
