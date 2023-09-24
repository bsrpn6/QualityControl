package info.onesandzeros.qualitycontrol.utils

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import info.onesandzeros.qualitycontrol.ui.activities.datecodescanner.DateCodeScannerActivity

class DateCodeScannerUtil(
    private val activity: FragmentActivity,
    private val activityResultRegistry: ActivityResultRegistry
) {
    fun startDateCodeScanning(callback: (String?) -> Unit) {
        val intentLauncher: ActivityResultLauncher<Intent> =
            activityResultRegistry.register(
                "date_code_scanner_request",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val dateCodeValue = intent?.getStringExtra("date_code_data")
                    callback(dateCodeValue)
                } else {
                    callback(null)
                }
            }

        val intent = Intent(activity, DateCodeScannerActivity::class.java)
        intentLauncher.launch(intent)
    }
}
