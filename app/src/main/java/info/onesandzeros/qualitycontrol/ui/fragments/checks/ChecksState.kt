package info.onesandzeros.qualitycontrol.ui.fragments.checks

import info.onesandzeros.qualitycontrol.api.models.CheckItem

data class ChecksState(
    val checksMap: Map<String, List<CheckItem>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val initialLoadComplete: Boolean = false,
    var currentTabPosition: Int = 0
)