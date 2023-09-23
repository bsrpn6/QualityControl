package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.viewmodels

import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.api.models.ProductSpecsResponse
import info.onesandzeros.qualitycontrol.utils.ErrorEvent
import info.onesandzeros.qualitycontrol.utils.Event

data class CheckSetupState(
    val departments: List<Department> = emptyList(),
    val lines: List<Line> = emptyList(),
    val checkTypes: List<CheckType> = emptyList(),
    val idhNumbers: List<IDHNumbers> = emptyList(),
    val productSpecs: Event<ProductSpecsResponse?>? = null,
    val isLoading: Boolean = false,
    val error: ErrorEvent? = null
)
