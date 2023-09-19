package info.onesandzeros.qualitycontrol.utils

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import info.onesandzeros.qualitycontrol.ui.activities.BarcodeScannerActivity
import info.onesandzeros.qualitycontrol.ui.activities.EMDKBarcodeScannerActivity

class BarcodeScannerUtil(
    private val activity: FragmentActivity,
    private val activityResultRegistry: ActivityResultRegistry
) {
    // Register the ActivityResultLauncher only once at initialization
    private val intentLauncher: ActivityResultLauncher<Intent> =
        activityResultRegistry.register(
            "barcode_scanner_request",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val barcodeValue = intent?.getStringExtra("barcode_data")
                Log.d(
                    "info.onesandzeros.qualitycontrol.utils.BarcodeScannerUtil",
                    "Barcode data: $barcodeValue"
                )
                callback(barcodeValue)
            } else {
                Log.d(
                    "info.onesandzeros.qualitycontrol.utils.BarcodeScannerUtil",
                    "No barcode detected."
                )
                callback(null)
            }
        }

    // Here, store the callback to be used in the ActivityResultLauncher
    private var callback: (String?) -> Unit = {}

    fun startBarcodeScanning(callback: (String?) -> Unit) {
        // Store the callback
        this.callback = callback

        // Decide which activity to launch based on the device type
        val intent = if (isZebraDevice()) {
            Intent(activity, EMDKBarcodeScannerActivity::class.java)
        } else {
            Intent(activity, BarcodeScannerActivity::class.java)
        }

        intentLauncher.launch(intent)
    }

    // Helper function to check if the device is a Zebra device
    private fun isZebraDevice(): Boolean {
        val knownZebraManufacturers = listOf("Zebra Technologies", "Motorola Solutions")
        val knownZebraModels = listOf(
            "TC55", "TC70", "TC75", "MC40",
            "MC67", "MC92", "TC700H", "WT41N0" // add more models as necessary
        )

        return knownZebraManufacturers.contains(Build.MANUFACTURER) ||
                knownZebraModels.contains(Build.MODEL)
    }

}
