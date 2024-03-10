package com.example.libralink

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.libralink.databinding.FragmentScanBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            setupCamera()
        }
    }

    private fun setupCamera() {
        barcodeScanner = BarcodeScanning.getClient()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            try {
                // Unbind any existing use cases before rebinding
                cameraProvider.unbindAll()

                // Set up Preview
                val preview = Preview.Builder().build()
                    .also {
                        it.setSurfaceProvider(binding.cameraView.surfaceProvider)
                    }

                // Set up ImageAnalysis
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            scanBarcode(imageProxy)
                        }
                    }

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Bind the use cases to the lifecycle
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun scanBarcode(imageProxy: ImageProxy) {
        @SuppressLint("UnsafeOptInUsageError")
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val barcodeValue = barcode.displayValue ?: "No Value"
                        showBarcodePopup(barcodeValue)
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun showBarcodePopup(barcodeValue: String) {
        // Retrieve studentId from arguments
        val studentId = arguments?.getString("studentId")

        // Ensure studentId is not null before proceeding
        if (studentId != null) {
            val bundle = Bundle().apply {
                putString("barcodeValue", barcodeValue)
                putString("studentId", studentId)
            }

            // Create an instance of ConfirmationFragment and set arguments
            val confirmationFragment = ConfirmationFragment()
            confirmationFragment.arguments = bundle

            // Navigate to ConfirmationFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, confirmationFragment)
                .addToBackStack(null)
                .commit()
        } else {
            // Handle the case where studentId is null
            // You might want to log an error, show a message, or take appropriate action
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                // Handle permission denial
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 123
    }
}
