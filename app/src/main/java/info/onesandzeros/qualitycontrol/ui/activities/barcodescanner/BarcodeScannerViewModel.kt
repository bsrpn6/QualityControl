package info.onesandzeros.qualitycontrol.ui.activities.barcodescanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

class BarcodeScannerViewModel : ViewModel() {

    private val _barcodeData = MutableLiveData<String?>()
    val barcodeData: LiveData<String?> get() = _barcodeData

    fun processBarcode(barcodes: List<FirebaseVisionBarcode>) {
        if (barcodes.isNotEmpty()) {
            _barcodeData.value = barcodes[0].rawValue
        }
    }
}
