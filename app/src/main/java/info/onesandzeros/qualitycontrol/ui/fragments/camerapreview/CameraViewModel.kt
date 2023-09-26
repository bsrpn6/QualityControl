package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.fragments.camerapreview

import android.content.Context
import androidx.lifecycle.ViewModel
import info.onesandzeros.qualitycontrol.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraViewModel : ViewModel() {


    fun getPhotoFile(context: Context): File {
        val outputDirectory = getOutputDirectory(context)
        return File(
            outputDirectory,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )
    }

    private fun getOutputDirectory(context: Context): File {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }
}
