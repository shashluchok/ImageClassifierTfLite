package com.example.android.mlkitimagerecognition

import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraActivity : AppCompatActivity() {



    private var backCamera = CameraSelector.DEFAULT_BACK_CAMERA
    private var frontCamera = CameraSelector.DEFAULT_FRONT_CAMERA
    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture? = null

    private var cam: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isFlashLightOn = false
    private var isBackCameraOn = false
    private var lastCameraImageUri: String? = null


    private var isLessThanLongClick = false
    private var isPointerOn = false
    private var initTimeInMillis = 0L
    private var isDraggingBlocked = true

    private var currentSavedVideoUri: Uri? = null


    private var
            initialIconX: Float = 0f

    private lateinit var onImageCapturedCallback: (Uri) -> Unit
    private lateinit var onVideoCapturedCallback: (Uri) -> Unit



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)


        lifecycleScope.launch(Dispatchers.IO){
            delay(500)
            withContext(Dispatchers.Main){
                        startCamera(backCamera)
            }
        }

    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val executor = ContextCompat.getMainExecutor(this)
        cameraProviderFuture.addListener(Runnable {

            cameraProvider = cameraProviderFuture.get()
            if (cameraProvider != null) {
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(preview_view.surfaceProvider)
                    }


                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(preview_view.width, preview_view.height))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, YourImageAnalyzer())
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()


                    cameraProvider!!.unbindAll()
                    cam = cameraProvider!!.bindToLifecycle(
                        this, cameraSelector, preview,imageAnalysis, imageCapture
                    )
            }
        }, executor)
    }

    private class YourImageAnalyzer : ImageAnalysis.Analyzer {

        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // Pass image to an ML Kit Vision API
                // ...
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    // ...
                    if(labels.isNotEmpty()) {
                        for (label in labels) {
                            val text = label.text
                            val confidence = label.confidence
                            val index = label.index
                            Log.v("Zhoppa","$text")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
            }

        }
    }

}