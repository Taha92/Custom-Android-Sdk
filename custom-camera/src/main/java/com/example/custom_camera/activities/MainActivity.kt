package com.example.custom_camera.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.custom_camera.R
import com.example.customcamerasdkapp.models.DetectionResult
import com.example.customcamerasdkapp.utils.AppConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var controller: LifecycleCameraController? = null
    private var recording: Recording? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var imageView: AppCompatImageView
    private lateinit var btnRecordVideo: AppCompatButton
    private lateinit var btnTakePicture: AppCompatButton
    private lateinit var currentPhotoPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // hide the action bar
        supportActionBar?.hide()

        // initialize views
        viewFinder = findViewById(R.id.viewFinder)
        imageView = findViewById(R.id.imageView)
        btnRecordVideo = findViewById(R.id.btnRecordVideo)
        btnTakePicture = findViewById(R.id.btnTakePicture)

        // Check camera permissions if all permission granted
        // start camera else ask for the permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this,
                AppConstant.REQUIRED_PERMISSIONS,
                AppConstant.REQUEST_CODE_PERMISSIONS
            )
        }

        btnTakePicture.setOnClickListener(this)
        btnRecordVideo.setOnClickListener(this)

        outputDirectory = getOutputDirectory()
        currentPhotoPath = outputDirectory.absolutePath
        //outputDirectory = createImageFile()
        cameraExecutor = Executors.newSingleThreadExecutor()

        controller =
            LifecycleCameraController(applicationContext).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or
                            CameraController.VIDEO_CAPTURE
                )
            }
    }

    private fun takeVideo() {
        controller?.let { recordVideo(it) }
    }

    private fun takePicture() {
        controller?.let { takePhoto() }
    }

    private fun takePhoto() {
        // Get a stable reference of the
        // modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(AppConstant.FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )
        Log.e(AppConstant.TAG, "takePhoto: "+photoFile)

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener,
        // which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(AppConstant.TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    imageView.visibility = View.VISIBLE
                    imageView.setImageResource(R.drawable.img_meal_two)

                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(AppConstant.TAG, msg)

                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, Uri.parse(
                        savedUri.toString()
                    ))

                    //setViewAndDetect(bitmap)
                    setViewAndDetect(getSampleImage(R.drawable.img_meal_two))
                }
            })
    }

    /**
     * getSampleImage():
     *      Get image form drawable and convert to bitmap.
     */
    private fun getSampleImage(drawable: Int): Bitmap {
        return BitmapFactory.decodeResource(resources, drawable, BitmapFactory.Options().apply {
            inMutable = true
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(AppConstant.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = AppConstant.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    // checks the camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstant.REQUEST_CODE_PERMISSIONS) {
            // If all permissions granted , then start Camera
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // If permissions are not granted,
                // present a toast to notify the user that
                // the permissions were not granted.
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * setViewAndDetect(bitmap: Bitmap)
     *      Set image to view and call object detection
     */
    private fun setViewAndDetect(bitmap: Bitmap) {
        // Display capture image
        //inputImageView.setImageBitmap(bitmap)
        //tvPlaceholder.visibility = View.INVISIBLE

        // Run ODT and display result
        // Note that we run this in the background thread to avoid blocking the app UI because
        // TFLite object detection is a synchronised process.
        lifecycleScope.launch(Dispatchers.Default) { runObjectDetection(bitmap) }
    }

    /**
     * runObjectDetection(bitmap: Bitmap)
     *      TFLite Object Detection function
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        // Step 1: Create TFLite's TensorImage object
        val image = TensorImage.fromBitmap(bitmap)

        // Step 2: Initialize the detector object
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.3f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            this,
            "salad.tflite",
            options
        )

        // Step 3: Feed given image to the detector
        val results = detector.detect(image)

        // Step 4: Parse the detection result and show it
        val resultToDisplay = results.map {
            // Get the top-1 category and craft the display text
            val category = it.categories.first()
            val text = "${category.label}, ${category.score.times(100).toInt()}%"

            // Create a data object to display the detection result
            DetectionResult(it.boundingBox, text)
        }
        // Draw the detection result on the bitmap and show it.
        val imgWithResult = drawDetectionResult(bitmap, resultToDisplay)
        runOnUiThread {
            imageView.setImageBitmap(imgWithResult)
            viewFinder.visibility = View.GONE
        }
    }

    /**
     * drawDetectionResult(bitmap: Bitmap, detectionResults: List<DetectionResult>
     *      Draw a box around each objects and show the object's name.
     */
    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResults: List<DetectionResult>
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val pen = Paint()
        pen.textAlign = Paint.Align.LEFT

        detectionResults.forEach {
            // draw bounding box
            pen.color = Color.RED
            pen.strokeWidth = 8F
            pen.style = Paint.Style.STROKE
            val box = it.boundingBox
            canvas.drawRect(box, pen)


            val tagSize = Rect(0, 0, 0, 0)

            // calculate the right font size
            pen.style = Paint.Style.FILL_AND_STROKE
            pen.color = Color.YELLOW
            pen.strokeWidth = 2F

            pen.textSize = AppConstant.MAX_FONT_SIZE
            pen.getTextBounds(it.text, 0, it.text.length, tagSize)
            val fontSize: Float = pen.textSize * box.width() / tagSize.width()

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.textSize) pen.textSize = fontSize

            var margin = (box.width() - tagSize.width()) / 2.0F
            if (margin < 0F) margin = 0F
            canvas.drawText(
                it.text, box.left + margin,
                box.top + tagSize.height().times(1F), pen
            )
        }
        return outputBitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun recordVideo(controller: LifecycleCameraController) {
        if(recording != null) {
            recording?.stop()
            recording = null
            btnRecordVideo.setBackgroundResource(R.drawable.ic_play)
            return
        }

        if(!allPermissionsGranted()) {
            return
        }

        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp")

        if (!folder.exists()) {
            folder.mkdir()
        }

        val outputFile = File.createTempFile("my-recording", SimpleDateFormat(AppConstant.FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".mp4", folder)

        //val outputFile = File(filesDir, "my-recording.mp4")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        btnRecordVideo.setBackgroundResource(R.drawable.ic_stop)
        recording = controller.startRecording(
            FileOutputOptions.Builder(outputFile).build(),
            AudioConfig.create(false),
            ContextCompat.getMainExecutor(applicationContext),
        ) { event ->
            when(event) {
                is VideoRecordEvent.Finalize -> {
                    if(event.hasError()) {
                        recording?.close()
                        recording = null

                        Toast.makeText(
                            applicationContext,
                            "Video capture failed",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.e(AppConstant.TAG, "recordVideo: "+event.error.toString())
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Video capture succeeded",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        val id = v?.id

        when(id) {
            R.id.btnTakePicture -> {
                takePicture()
            }

            R.id.btnRecordVideo -> {
                takeVideo()
            }
        }
    }


}
