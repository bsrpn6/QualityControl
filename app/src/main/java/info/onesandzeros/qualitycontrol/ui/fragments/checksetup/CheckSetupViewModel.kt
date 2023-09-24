package info.onesandzeros.qualitycontrol.ui.fragments.checksetup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.data.CheckSetupRepository
import info.onesandzeros.qualitycontrol.utils.ErrorAction
import info.onesandzeros.qualitycontrol.utils.ErrorEvent
import info.onesandzeros.qualitycontrol.utils.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckSetupViewModel @Inject constructor(
    private val repository: CheckSetupRepository
) : ViewModel() {

    val uiState = MutableLiveData(CheckSetupState(isLoading = true))

    val navigateTo: MutableLiveData<Event<Int>> = MutableLiveData()

    private val errorEvent: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()

    fun selectDepartment(department: Department) {
        // Here, you can update any internal state if needed and then fetch the lines
        getLinesForDepartment(department.id)
    }


    fun getDepartments(siteId: String) {
        viewModelScope.launch {
            val departments = safeCall(
                action = { repository.getDepartmentsForSite(siteId) },
                errorMessage = "Failed to fetch departments.",
                errorAction = ErrorAction.LOAD_IDH_FROM_DB
            )
            departments?.let {
                uiState.value = uiState.value?.copy(departments = it, isLoading = false)
            }
        }
    }

    private fun getLinesForDepartment(departmentId: String) {
        viewModelScope.launch {
            val lines = safeCall(
                action = { repository.getLinesForDepartment(departmentId) },
                errorMessage = "Failed to fetch lines for department.",
                errorAction = ErrorAction.LOAD_IDH_FROM_DB
            )
            lines?.let {
                uiState.value = uiState.value?.copy(lines = it, isLoading = false)
            }
        }
    }

    fun getCheckTypesForLine(lineId: String) {
        viewModelScope.launch {
            val checkTypes = safeCall(
                action = { repository.getCheckTypesForLine(lineId) },
                errorMessage = "Failed to fetch check types for line.",
                errorAction = ErrorAction.LOAD_IDH_FROM_DB
            )
            checkTypes?.let {
                uiState.value = uiState.value?.copy(checkTypes = it, isLoading = false)
            }
        }
    }

    fun getProductsForLine(lineId: String) {
        viewModelScope.launch {
            val products = safeCall(
                action = { repository.getProductsForLine(lineId) },
                errorMessage = "Failed to fetch products for line.",
                errorAction = ErrorAction.LOAD_IDH_FROM_DB
            )
            products?.let {
                uiState.value = uiState.value?.copy(idhNumbers = it, isLoading = false)
            }
        }
    }

    fun getSpecs(id: String) {
        viewModelScope.launch {
            val specs = safeCall(
                action = { repository.getSpecsForProduct(id) },
                errorMessage = "Failed to fetch departments.",
                errorAction = ErrorAction.LOAD_IDH_FROM_DB
            )
            specs?.let {
                uiState.value = uiState.value?.copy(productSpecs = Event(it), isLoading = false)
            }
        }
    }

    private suspend inline fun <T> safeCall(
        crossinline action: suspend () -> T, errorMessage: String, errorAction: ErrorAction
    ): T? {
        return try {
            action.invoke()
        } catch (e: Exception) {
            uiState.value = uiState.value?.copy(
                error = ErrorEvent(
                    e.localizedMessage ?: errorMessage, errorAction
                ), isLoading = false
            )
            null
        }
    }

    // Function for starting checks
    fun startChecks() {
        val currentState = uiState.value
        if (currentState != null && currentState.departments.isNotEmpty() && currentState.lines.isNotEmpty() && currentState.checkTypes.isNotEmpty() && currentState.idhNumbers.isNotEmpty()) {
            navigateTo.value = Event(R.id.action_checkSetupFragment_to_checksFragment)
        } else {
            errorEvent.value = Event(
                ErrorEvent(
                    "Please select valid values for all fields.", ErrorAction.INVALID_SELECTION
                )
            )
        }
    }

    // Function to handle logout
    fun logoutUser() {
        // This can be extended in the future if you have any logout logic in your repository
        navigateTo.value = Event(R.id.action_checkSetupFragment_to_loginFragment)
    }

}