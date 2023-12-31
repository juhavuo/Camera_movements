/**
 *Trying out CameraX
 *
 * Juha Vuokko
 */

package fi.julavu.cameramovements

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.impl.ImageCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CameraHandler(private val context: Context) {

    private lateinit var dataStoreHandler: DataStoreHandler
    private lateinit var imageManipulator: ImageManipulator
    private lateinit var imageCapture: ImageCapture
    private val fileHandler = FileHandler(context)
    private lateinit var imageCaptureConfig: ImageCaptureConfig
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraExecutor: Executor
    private lateinit var camera: Camera
    private var amountOfPhotos = 0
    private var outputSize = Size(720, 480)

    companion object {
        fun getSizes(context: Context): ArrayList<Size> {
            val sizes = ArrayList<Size>()
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val camIds = cameraManager.cameraIdList
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(camIds[0])
            val streamMap =
                cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (streamMap != null) {
                val sizeArray = streamMap.getOutputSizes(ImageFormat.JPEG)
                sizes.addAll(sizeArray)
            }
            return sizes
        }

    }

    suspend fun getSettings() {
        dataStoreHandler = DataStoreHandler.getInstance(context)
        val amountSettingsData =
            SettingsData.getSettingsData(context, R.string.for_amount_of_photos_seekbar)
        amountOfPhotos = dataStoreHandler.getSeekbarProgressValue(amountSettingsData)
        val sizes = getSizes(context)
        outputSize = sizes[dataStoreHandler.getImageSizeIndex()]
        Log.i(MyApplication.tagForTesting, "getsettings: $amountOfPhotos")
    }

    fun prepareCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProvider = cameraProviderFuture.get()

        cameraProviderFuture.addListener(Runnable {

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(outputSize)
                .build()
            cameraExecutor = Executors.newSingleThreadExecutor()

            camera =
                cameraProvider.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    imageCapture
                )

            Log.i(MyApplication.tagForTesting, "prepareCamera")

        },ContextCompat.getMainExecutor(context))

    }

    fun useCamera() {
        Log.i(MyApplication.tagForTesting, "useCamera")
        CameraService.isBusy = true
        var outputFileOptions: OutputFileOptions
        Log.i(MyApplication.tagForTesting, "amount of photos: $amountOfPhotos")
        for (i in 0 until amountOfPhotos) {
            Log.i(MyApplication.tagForTesting, "photo number: $i")
            outputFileOptions =
                OutputFileOptions.Builder(fileHandler.getFileFromInternalStorage(i)).build()
            imageCapture.takePicture(outputFileOptions, cameraExecutor!!,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val uri = outputFileResults.savedUri
                        Log.i(MyApplication.tagForTesting, "onImageSaved $uri")
                        if(i == amountOfPhotos-1){
                            imageManipulator = ImageManipulator(context)
                            imageManipulator.manipulate()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(MyApplication.tagForTesting, "error when taking picture: $exception")
                    }

                })
        }//for loop to take photos
    }//use camera

    fun stopUsingCamera(){
        cameraProvider.unbindAll() // stop using camera
    }
}