package com.example.android.mlkitimagerecognition

import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException


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

    var classifier: ImageClassifier? = null


    private var isLessThanLongClick = false
    private var isPointerOn = false
    private var initTimeInMillis = 0L
    private var isDraggingBlocked = true

    private var currentSavedVideoUri: Uri? = null


    private var
            initialIconX: Float = 0f

    private lateinit var onImageCapturedCallback: (Uri) -> Unit
    private lateinit var onVideoCapturedCallback: (Uri) -> Unit

    override fun onStart() {
        super.onStart()
        try {
            classifier =  ImageClassifier(this)

        } catch (e: IOException) {
        }
    }


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


           /*   val imageAnalysis2 = ImageAnalysis.Builder()
                        .setTargetResolution(Size(preview_view.width, preview_view.height))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor, YourImageAnalyzer())
                        }*/




                val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                imageAnalysis.setAnalyzer(executor,
                        { image ->
                            classifyFrame(image)
                            image.close()
                        })

                imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()


                cameraProvider!!.unbindAll()
                cam = cameraProvider!!.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis, imageCapture
                )
            }
        }, executor)
    }

    private class YourImageAnalyzer : ImageAnalysis.Analyzer {

        val localModel = LocalModel.Builder()
                .setAssetFilePath("model.tflite")
                // or .setAbsoluteFilePath(absolute file path to model file)
                // or .setUri(URI to model file)
                .build()

        val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .setMaxResultCount(5)
                .build()


        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // Pass image to an ML Kit Vision API
                // ...
//            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

                val labeler = ImageLabeling.getClient(customImageLabelerOptions)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    // ...
                    if(labels.isNotEmpty()) {
                        for (label in labels) {
                            val text = label.text
                            val confidence = label.confidence
                            val index = label.index
                            Log.v("Zhoppa", "$text")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
                mediaImage.close()
            }
            imageProxy?.close()
        }
    }

    private fun classifyFrame(imageProxy: ImageProxy) {
        val image = imageProxy.image
        val bitmap: Bitmap? = image?.toBitmap()
        bitmap?.let{
            val scaled = Bitmap.createScaledBitmap(it, ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y, false)
            val textToShow: String = classifier?.classifyFrame(scaled)?:""
            scan_result.setText(textToShow)
            bitmap.recycle()
        }

    }


    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

}