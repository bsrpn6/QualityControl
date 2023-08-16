package info.onesandzeros.qualitycontrol.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.ActivityDateCodeScannerBinding
import info.onesandzeros.qualitycontrol.ui.views.BoundingBoxOverlayView

class DateCodeScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDateCodeScannerBinding
    private lateinit var textRecognizer: FirebaseVisionTextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the text recognizer
        textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        // Initialize the camera preview
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Set up Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            // Set up ImageAnalysis for date code detection
            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), DateCodeAnalyzer())
                }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any previous use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    inner class DateCodeAnalyzer : ImageAnalysis.Analyzer {
        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val imageRotationDegrees = imageProxy.imageInfo.rotationDegrees
                val rotationCompensation = degreesToFirebaseRotation(imageRotationDegrees)

                val firebaseVisionImage =
                    FirebaseVisionImage.fromMediaImage(mediaImage, rotationCompensation)

                textRecognizer.processImage(firebaseVisionImage)
                    .addOnSuccessListener { result ->
                        val dateCode = extractDateCode(result.text)
                        if (dateCode != null) {
                            val resultIntent = Intent()
                            resultIntent.putExtra("date_code_data", dateCode)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }

                        // Draw bounding boxes around detected text elements
                        val boundingBoxes = result.textBlocks.flatMap { it.lines }
                            .mapNotNull { it.boundingBox }

                        // Convert FirebaseVision's Rect objects to Android's Rect objects
                        val androidBoundingBoxes = boundingBoxes.map {
                            android.graphics.Rect(
                                it.left,
                                it.top,
                                it.right,
                                it.bottom
                            )
                        }

                        val boundingBoxOverlayView =
                            findViewById<BoundingBoxOverlayView>(R.id.boundingBoxOverlayView)
                        boundingBoxOverlayView.setCameraPreviewSize(
                            imageProxy.width,
                            imageProxy.height
                        )
                        boundingBoxOverlayView.setBoundingBoxes(androidBoundingBoxes)

                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Date code detection failed", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }


        private fun degreesToFirebaseRotation(degrees: Int): Int {
            return when (degrees) {
                0 -> FirebaseVisionImageMetadata.ROTATION_0
                90 -> FirebaseVisionImageMetadata.ROTATION_90
                180 -> FirebaseVisionImageMetadata.ROTATION_180
                270 -> FirebaseVisionImageMetadata.ROTATION_270
                else -> throw IllegalArgumentException("Rotation value $degrees is not supported.")
            }
        }
    }

    private fun extractDateCode(text: String): String? {
        val prefix = "PO"
        val length = 8 // Replace with the actual length of the date code
        val regex = Regex("$prefix\\d{$length}")
        val match = regex.find(text)
        return match?.value
    }

    companion object {
        private const val TAG = "DateCodeScannerActivity"
    }
}
