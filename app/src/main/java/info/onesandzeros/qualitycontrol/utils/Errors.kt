package info.onesandzeros.qualitycontrol.utils

data class ErrorEvent(val message: String, val action: ErrorAction)

enum class ErrorAction {
    LOAD_IDH_FROM_DB,
    LOAD_CHECKS_FROM_DB,
    LOAD_LINES,
    LOAD_LINES_FROM_DATABASE,
    INVALID_SELECTION,
    // ... other actions
}