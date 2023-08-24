package info.onesandzeros.qualitycontrol.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line

class SharedViewModel : ViewModel() {
    fun clearDataSaveUserAndID() {
        checksLiveData.value = null
    }

    fun clearDataSaveUser() {
        departmentLiveData.value = null
        lineLiveData.value = null
        idhNumberLiveData.value = null
        checksLiveData.value = null
    }

    fun clearUserName() {
        usernameLiveData.value = null
    }

    val checkStartTimestamp = MutableLiveData<Long>()
    val usernameLiveData = MutableLiveData<String?>()
    val departmentLiveData = MutableLiveData<Department?>()
    val checkTypeLiveData = MutableLiveData<CheckType?>()
    val lineLiveData = MutableLiveData<Line?>()
    val idhNumberLiveData = MutableLiveData<IDHNumbers?>()
    val checksLiveData = MutableLiveData<Map<String, List<CheckItem>>?>()
}

