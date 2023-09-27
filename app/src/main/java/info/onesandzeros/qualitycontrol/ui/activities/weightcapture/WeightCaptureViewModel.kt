package info.onesandzeros.qualitycontrol.ui.activities.weightcapture

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem
import info.onesandzeros.qualitycontrol.api.models.WeightCheckItem

class WeightCaptureViewModel : ViewModel() {

    private val _weightLiveData = MutableLiveData<Double>()
    val weightLiveData: LiveData<Double> get() = _weightLiveData

    private val _fillHeadListLiveData = MutableLiveData<List<FillHeadItem>>()
    val fillHeadListLiveData: LiveData<List<FillHeadItem>> get() = _fillHeadListLiveData

    var currentFillHeadIndex = 0
    lateinit var weightCheckItem: WeightCheckItem
    lateinit var fillHeadList: MutableList<FillHeadItem>

    fun updateWeightCheckItem(item: WeightCheckItem) {
        weightCheckItem = item
        fillHeadList = weightCheckItem.fillHeads.map { FillHeadItem(it, null) }.toMutableList()
        _fillHeadListLiveData.value = fillHeadList
    }

    fun handleWeight(weight: Double) {
        Log.d("WeightCaptureViewModel", "Received weight: $weight")
        _weightLiveData.postValue(weight)
        Handler(Looper.getMainLooper()).postDelayed({
            processWeight(weight)
        }, 2000) // 2 seconds delay
    }

    private fun processWeight(weight: Double) {
        fillHeadList[currentFillHeadIndex] =
            FillHeadItem(fillHeadList[currentFillHeadIndex].fillHead, weight)
        _fillHeadListLiveData.value = fillHeadList
    }
}
