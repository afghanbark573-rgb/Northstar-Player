package com.northstar.player.data.storage

enum class SafGrantReconciliationAction {
    MARK_UNAVAILABLE,
    RESCAN,
    NONE
}

/** Pure state-transition policy shared by foreground/background SAF reconciliation. */
object SafGrantReconciliationPolicy {
    fun action(wasAvailable: Boolean, isAvailable: Boolean): SafGrantReconciliationAction =
        when {
            !isAvailable -> SafGrantReconciliationAction.MARK_UNAVAILABLE
            !wasAvailable -> SafGrantReconciliationAction.RESCAN
            else -> SafGrantReconciliationAction.NONE
        }
}
