package info.onesandzeros.qualitycontrol.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import info.onesandzeros.qualitycontrol.api.models.FillHeadItem
import info.onesandzeros.qualitycontrol.api.models.WeightCheckItem
import info.onesandzeros.qualitycontrol.ui.activities.WeightCaptureActivity

class WeightCaptureUtil(
    private val activity: FragmentActivity,
    private val activityResultRegistry: ActivityResultRegistry
) {
    fun startWeightCapture(
        weightCheckItem: WeightCheckItem,
        callback: (List<FillHeadItem>?) -> Unit
    ) {
        val intentLauncher: ActivityResultLauncher<Intent> =
            activityResultRegistry.register(
                "weight_capture_request",
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val weightCaptureValue =
                        intent?.getParcelableArrayListExtra<FillHeadItem>("weight_capture_data")
                    Log.d(
                        "info.onesandzeros.qualitycontrol.utils.WeightCaptureUtil",
                        "Weight capture data: $weightCaptureValue"
                    )
                    callback(weightCaptureValue)
                } else {
                    Log.d(
                        "info.onesandzeros.qualitycontrol.utils.WeightCaptureUtil",
                        "Not able to capture weight data."
                    )
                    callback(null)
                }
            }

        val intent = Intent(activity, WeightCaptureActivity::class.java)
        intent.putExtra("weight_check_item_data", weightCheckItem)
        intentLauncher.launch(intent)
    }
}

