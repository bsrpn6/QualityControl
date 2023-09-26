package info.onesandzeros.qualitycontrol.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import info.onesandzeros.qualitycontrol.R
import info.onesandzeros.qualitycontrol.databinding.FragmentCameraPreviewBinding
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.ui.adapters.ImageReelAdapter
import info.onesandzeros.qualitycontrol.ui.viewmodels.SharedViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CameraPreviewFragment : Fragment() {
    private val REQUEST_CODE_PERMISSIONS = 10

    private lateinit var imageCapture: ImageCapture

    private var _binding: FragmentCameraPreviewBinding? = null
    private val binding get() = _binding!!

    private val imageReelAdapter = ImageReelAdapter { uriToRemove ->
        sharedViewModel.removeImageUri(sectionName, uriToRemove)
    }

    private lateinit var sectionName: String

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sectionName = CameraPreviewFragmentArgs.fromBundle(requireArguments()).sectionName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        // Use binding for setting up the reelRecyclerView
        binding.reelRecyclerView.adapter = imageReelAdapter

        sharedViewModel.photosLiveData.observe(viewLifecycleOwner) { photosMap ->
            val currentPhotos = photosMap?.get(sectionName) ?: emptyList()
            imageReelAdapter.setImages(currentPhotos)
        }

        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        // Add a click listener for the back button using the binding object
        binding.backButton.setOnClickListener {
            // Whatever you want to do when the back button is clicked (e.g., navigate back)
            findNavController().popBackStack()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    sharedViewModel.addImageUri(sectionName, savedUri)

                    // Optionally show a toast or some feedback to the user
                    simulateFlashEffect()

                    // Refresh the RecyclerView
                    val updatedUris = sharedViewModel.getPhotosForSection(sectionName)
                    imageReelAdapter.setImages(updatedUris)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle the error
                }
            })
    }

    fun simulateFlashEffect() {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 500 // Adjust as needed
        fadeOut.fillAfter = true
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding.flashOverlay.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.flashOverlay.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        binding.flashOverlay.startAnimation(fadeOut)
    }

    private val outputDirectory: File
        get() {
            val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists()) mediaDir else requireActivity().filesDir
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // If permissions are not granted, inform the user.
                // For simplicity, we just finish the current activity.
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
