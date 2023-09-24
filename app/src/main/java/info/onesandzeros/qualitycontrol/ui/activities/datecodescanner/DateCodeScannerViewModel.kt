package info.onesandzeros.qualitycontrol.ui.activities.datecodescanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DateCodeScannerViewModel : ViewModel() {

    private val _dateCodeData = MutableLiveData<String?>()
    val dateCodeData: LiveData<String?> get() = _dateCodeData

    fun extractDateCode(text: String) {
        val prefix = "PO"
        val length = 8
        val regex = Regex("$prefix\\d{$length}")
        val match = regex.find(text)
        _dateCodeData.value = match?.value
    }
}
