package info.onesandzeros.qualitycontrol.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.data.AppDatabase
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.viewmodels.CheckState
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.DataFetchHelpers
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.DatabaseException
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.NetworkException
import info.onesandzeros.qualitycontrol.ui.fragments.checks.ChecksRepository
import info.onesandzeros.qualitycontrol.utils.Event
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecksViewModel @Inject constructor(
    private val repository: ChecksRepository,
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val _uiState = MutableLiveData<CheckState>()
    val uiState: LiveData<CheckState> get() = _uiState

    val navigateTo: MutableLiveData<Event<Int>> = MutableLiveData()

    init {
        _uiState.value = CheckState()
    }

    fun getChecks(lineId: String, checkTypeId: String, idhNumberId: String) {
        viewModelScope.launch {
            when (val result = repository.getChecks(lineId, checkTypeId, idhNumberId)) {
                is DataFetchHelpers.DataResult.Success -> {
                    // Handle success
                    _uiState.value = _uiState.value?.copy(
                        checksMap = categorizeChecksByType(result.data),
                        isLoading = false
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
