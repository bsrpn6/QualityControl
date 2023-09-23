package info.onesandzeros.qualitycontrol.utils

data class ErrorEvent(val message: String, val action: ErrorAction)

enum class ErrorAction {
    LOAD_IDH_FROM_DB,
    LOAD_LINES,
    LOAD_LINES_FROM_DATABASE,
    // ... other actions
}