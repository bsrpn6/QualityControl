package info.onesandzeros.qualitycontrol.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line

class CheckSetupViewModel : ViewModel() {
    val departmentListLiveData = MutableLiveData<List<Department>?>()
    val lineListLiveData = MutableLiveData<List<Line>?>()
    val checkTypeLiveData = MutableLiveData<List<CheckType>?>()
    val idhNumberLiveData = MutableLiveData<List<IDHNumbers>?>()
}