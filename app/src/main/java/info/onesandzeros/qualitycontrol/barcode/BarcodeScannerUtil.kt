import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import info.onesandzeros.qualitycontrol.ui.activities.BarcodeScannerActivity

class BarcodeScannerUtil(
    private val activity: FragmentActivity,
    private val activityResultRegistry: ActivityResultRegistry
) {
    fun startBarcodeScanning(callback: (String?) -> Unit) {
        val intentLauncher: ActivityResultLauncher<Intent> =
            activityResultRegistry.register(
                "barcode_scanner_request",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val barcodeValue = intent?.getStringExtra("barcode_data")
                    Log.d("BarcodeScannerUtil", "Barcode data: $barcodeValue")
                    callback(barcodeValue)
                } else {
                    Log.d("BarcodeScannerUtil", "No barcode detected.")
                    callback(null)
                }
            }

        val intent = Intent(activity, BarcodeScannerActivity::class.java)
        intentLauncher.launch(intent)
    }
}

