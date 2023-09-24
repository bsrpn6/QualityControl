package info.onesandzeros.qualitycontrol.ui.fragments.checks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.utils.DataFetchHelpers
import info.onesandzeros.qualitycontrol.utils.DatabaseException
import info.onesandzeros.qualitycontrol.utils.NetworkException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecksViewModel @Inject constructor(
    private val repository: ChecksRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ChecksState>()
    val uiState: LiveData<ChecksState> get() = _uiState

    init {
        _uiState.value = ChecksState()
    }

    fun getChecks(lineId: String, checkTypeId: String, idhNumberId: String) {
        _uiState.value = _uiState.value?.copy(isLoading = true)


        viewModelScope.launch {
            when (val result = repository.getChecks(lineId, checkTypeId, idhNumberId)) {
                is DataFetchHelpers.DataResult.Success -> {
                    // Handle success
                    _uiState.value = _uiState.value?.copy(
                        checksMap = categorizeChecksByType(result.data),
                        isLoading = false,
                        initialLoadComplete = true
                    )
                }

                is DataFetchHelpers.DataResult.Error -> {
                    // Here, you can translate the error
                    val errorMessage = when (result.exception) {
                        is NetworkException -> "Network error occurred!"
                        is DatabaseException -> "Database error occurred!"
                        else -> result.exception.localizedMessage
                    }
                    _uiState.value = _uiState.value?.copy(error = errorMessage, isLoading = false)
                }
            }
        }
    }

    private fun categorizeChecksByType(checks: List<CheckItem>?): Map<String, List<CheckItem>> {
        val checksMap = mutableMapOf<String, MutableList<CheckItem>>()
        checks?.forEach { checkItem ->
            val checkType = checkItem.section
            checksMap.getOrPut(checkType) { mutableListOf() }.add(checkItem)
        }
        return checksMap
    }

    fun saveSubmissionToLocalDatabase(submissionData: ChecksSubmissionRequest) {
        viewModelScope.launch {
            repository.saveSubmissionToLocalDatabase(submissionData)
        }
    }
}
