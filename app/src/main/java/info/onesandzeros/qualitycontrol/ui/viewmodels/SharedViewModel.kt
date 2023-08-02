package info.onesandzeros.qualitycontrol.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.CheckItem

class SharedViewModel : ViewModel() {
    fun clearDataSaveUser() {
        departmentLiveData.value = null
        lineLiveData.value = null
        idhNumberLiveData.value = null
        checksLiveData.value = null
    }

    fun clearUserName() {
        usernameLiveData.value = null
    }

    val usernameLiveData = MutableLiveData<String?>()
    val departmentLiveData = MutableLiveData<Department?>()
    val lineLiveData = MutableLiveData<String?>()
    val idhNumberLiveData = MutableLiveData<Int?>()
    val checksLiveData = MutableLiveData<Map<String, List<CheckItem>>?>()
}

