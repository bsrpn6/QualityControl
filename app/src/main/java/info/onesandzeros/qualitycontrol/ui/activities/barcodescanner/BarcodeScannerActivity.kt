package info.onesandzeros.qualitycontrol.ui.activities.barcodescanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.ui.activities.baseactivity.BaseActivity

class BarcodeScannerActivity : BaseActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var barcodeDetector: FirebaseVisionBarcodeDetector

    private val viewModel: BarcodeScannerViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        viewModel.barcodeData.observe(this) { barcodeValue ->
            if (barcodeValue != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("barcode_data", barcodeValue)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        // Initialize the barcode detector
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()
        barcodeDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        // Initialize the camera preview
        previewView = findViewById(R.id.previewView)
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Set up Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Set up ImageAnalysis for barcode detection
            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), BarcodeAnalyzer())
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

    inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val imageRotationDegrees = imageProxy.imageInfo.rotationDegrees
                val rotationCompensation = degreesToFirebaseRotation(imageRotationDegrees)

                val firebaseVisionImage =
                    FirebaseVisionImage.fromMediaImage(mediaImage, rotationCompensation)

                barcodeDetector.detectInImage(firebaseVisionImage)
                    .addOnSuccessListener { barcodes ->
                        viewModel.processBarcode(barcodes)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Barcode detection failed", e)
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


    companion object {
        private const val TAG = "BarcodeScannerActivity"
    }
}
