package info.onesandzeros.qualitycontrol.ui.fragments.checks

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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChecksViewModel @Inject constructor(
    private val repository: ChecksRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ChecksState>()
    val uiState: MutableLiveData<ChecksState> get() = _uiState

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

    fun addComment(section: String) {

        val currentChecksMap = _uiState.value?.checksMap ?: emptyMap()
        val updatedChecksMap = currentChecksMap.toMutableMap()

        val currentSectionChecks = updatedChecksMap[section]?.toMutableList() ?: mutableListOf()
        val commentCheck = CheckItem(
            _id = UUID.randomUUID().toString(), // Generating a random UUID for the comment ID
            section = section, // You can replace this with the appropriate section name
            type = "comment",
            title = "Additional Comments",
            description = "Provide any additional comments about this section.",
            expectedValue = null,
            images = emptyList(),
            result = null
        )
        currentSectionChecks.add(commentCheck)

        updatedChecksMap[section] = currentSectionChecks

        val updatedState = _uiState.value?.copy(checksMap = updatedChecksMap)
        _uiState.postValue(updatedState)
    }

    fun isCommentAddedInSection(section: String): Boolean {
        return _uiState.value?.checksMap?.get(section)?.any { it.type == "comment" } == true
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
