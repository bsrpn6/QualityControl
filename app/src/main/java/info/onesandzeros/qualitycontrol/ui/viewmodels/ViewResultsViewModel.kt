package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.data.AppDatabase
import info.onesandzeros.qualitycontrol.data.models.LineEntity
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.data.models.CheckSubmissionEntity
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewResultsViewModel @Inject constructor(private val appDatabase: AppDatabase) : ViewModel() {

    private val _lines = MutableLiveData<List<Line>>() // Change the type to List<Line>
    val lines: LiveData<List<Line>> get() = _lines

    private val _checkSubmissions = MutableLiveData<List<CheckSubmissionEntity>>()
    val checkSubmissions: LiveData<List<CheckSubmissionEntity>> get() = _checkSubmissions

    private val _lineNames = MutableLiveData<List<String>>()

    fun loadLinesByDepartmentId(departmentId: Int) {
        viewModelScope.launch {
            // Perform database operation on a background thread using viewModelScope
            val lines = appDatabase.lineDao().getAllLines().toLineList()
            _lines.value = lines
            _lineNames.value = lines.toLineNameList()
        }
    }

    fun setLineId(line: Line) {
        val checkSubmissionsLiveData =
            appDatabase.checkSubmissionDao().getAllSubmissionsForLine(line)
        checkSubmissionsLiveData.observeForever {
            _checkSubmissions.value = it
            checkSubmissionsLiveData.removeObserver {} // Remove the observer after updating the value
        }
    }

    private fun List<LineEntity>.toLineList(): List<Line> {
        return map { lineEntity ->
            Line(lineEntity.lineId, lineEntity.abbreviation, lineEntity.name, lineEntity.checkTypes)
        }
    }

    private fun List<Line>.toLineNameList(): List<String> {
        return map { line ->
            line.name
        }
    }
}

