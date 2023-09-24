package info.onesandzeros.qualitycontrol.ui.fragments.submissionresult

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import javax.inject.Inject

@HiltViewModel
class SubmissionResultViewModel @Inject constructor() : ViewModel() {

    private val _resultState = MutableLiveData<ResultState>()
    val resultState: LiveData<ResultState> get() = _resultState

    fun processCheckResults(totalFailedChecks: Array<CheckItem>) {
        _resultState.value = if (totalFailedChecks.isNotEmpty()) {
            ResultState.Failure(totalFailedChecks.size)
        } else {
            ResultState.Success
        }
    }

    sealed class ResultState {
        data class Failure(val numberOfFailedChecks: Int) : ResultState()
        object Success : ResultState()
    }
}
