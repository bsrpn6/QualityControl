package info.onesandzeros.qualitycontrol.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.api.models.ProductSpecsResponse
import info.onesandzeros.qualitycontrol.data.CheckSetupRepository
import info.onesandzeros.qualitycontrol.utils.ErrorAction
import info.onesandzeros.qualitycontrol.utils.ErrorEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckSetupViewModel @Inject constructor(
    private val repository: CheckSetupRepository
) : ViewModel() {

    val departmentListLiveData = MutableLiveData<List<Department>?>()
    val lineListLiveData = MutableLiveData<List<Line>?>()
    val checkTypeLiveData = MutableLiveData<List<CheckType>?>()
    val idhNumberLiveData = MutableLiveData<List<IDHNumbers>?>()
    val productSpecsLiveData = MutableLiveData<ProductSpecsResponse?>()
    val errorMessage: MutableLiveData<ErrorEvent> = MutableLiveData()

    fun getDepartments(siteId: String) {
        viewModelScope.launch {
            try {
                val departments = repository.getDepartmentsForSite(siteId)
                departmentListLiveData.value = departments
            } catch (e: Exception) {
                errorMessage.value = ErrorEvent(
                    e.localizedMessage ?: "Failed to fetch departments.",
                    ErrorAction.LOAD_IDH_FROM_DB
                )
            }
        }
    }

    fun getLinesForDepartment(departmentId: String) {
        viewModelScope.launch {
            try {
                val lines = repository.getLinesForDepartment(departmentId)
                lineListLiveData.value = lines
            } catch (e: Exception) {
                errorMessage.value =
                    ErrorEvent("Failed to fetch lines.", ErrorAction.LOAD_LINES_FROM_DATABASE)
            }
        }
    }

    fun getCheckTypesForLine(lineId: String) {
        viewModelScope.launch {
            try {
                val checkTypes = repository.getCheckTypesForLine(lineId)
                checkTypeLiveData.value = checkTypes
            } catch (e: Exception) {
                errorMessage.value =
                    ErrorEvent("Failed to fetch check types.", ErrorAction.LOAD_LINES_FROM_DATABASE)
            }
        }
    }

    fun getProductsForLine(lineId: String) {
        viewModelScope.launch {
            try {
                val products = repository.getProductsForLine(lineId)
                idhNumberLiveData.value = products
            } catch (e: Exception) {
                errorMessage.value =
                    ErrorEvent("Failed to fetch IDH numbers.", ErrorAction.LOAD_LINES_FROM_DATABASE)
            }
        }
    }

    fun getSpecs(id: String) {
        viewModelScope.launch {
            try {
                val specs = repository.getSpecsForProduct(id)
                productSpecsLiveData.value = specs
            } catch (e: Exception) {
                errorMessage.value = ErrorEvent(
                    "Failed to fetch product specs.",
                    ErrorAction.LOAD_LINES_FROM_DATABASE
                )
            }
        }
    }
}