package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.viewmodels

import info.onesandzeros.qualitycontrol.api.models.CheckItem

data class CheckState(
    val checksMap: Map<String, List<CheckItem>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)