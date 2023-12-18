/**
 *Trying out CameraX
 *
 * Juha Vuokko
 */

package fi.julavu.cameramovements

import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CameraHandler(private val context: Context) {

    private lateinit var dataStoreHandler: DataStoreHandler
    private lateinit var imageCapture: ImageCapture
    private val fileHandler = FileHandler(context)
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraExecutor: Executor
    private lateinit var camera: Camera
    private var multipliers = ""
    private var amountOfPhotos = 0
    private var pixelSize = 1
    private var outputSize = Size(720, 480)
    private lateinit var workManager: WorkManager

    companion object {
        /**
         * Returns all image sizes as arraylist.
         */
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

    /**
     * Get setting data from DataStore.
     */
    suspend fun getSettings() {
        dataStoreHandler = DataStoreHandler.getInstance(context)
        val amountSettingsData =
            SettingsData.getSettingsData(context, R.string.for_amount_of_photos_seekbar)
        amountOfPhotos = dataStoreHandler.getSeekbarProgressValue(amountSettingsData)
        val pixelSizeSettingsData =
            SettingsData.getSettingsData(context, R.string.for_pixel_size_seekbar)
        pixelSize = dataStoreHandler.getSeekbarProgressValue(pixelSizeSettingsData)
        Log.i(MyApplication.tagForTesting, "camera handler pixel size: $pixelSize")
        val sizes = getSizes(context)
        outputSize = sizes[dataStoreHandler.getImageSizeIndex()]
        Log.i(MyApplication.tagForTesting, "get settings: $amountOfPhotos")
        multipliers = dataStoreHandler.getMultipliers()
    }

    /**
     * Prepare camera. This function is good to run at onCreate when using. Don't know
     * how much time is needed after this before camera is ready for taking images.
     */
    fun prepareCamera(rootView: View) {
        workManager = WorkManager.getInstance(context)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()

        cameraProviderFuture.addListener({

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(outputSize)
                .build()
            Log.i(MyApplication.tagForTesting,"rotation: ${rootView.display.rotation}")
            cameraExecutor = Executors.newSingleThreadExecutor()

            camera =
                cameraProvider.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    imageCapture
                )
            Log.i(MyApplication.tagForTesting, "prepareCamera")

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * This is for taking images. All taken images are first saved to temporary storage and after
     * this is done ImageManipulatorWorker is started in background.
     */
    fun useCamera(fileName: String, activity: ComponentActivity) {
        Log.i(MyApplication.tagForTesting, "useCamera")

        val internalFolderName = "photos${System.currentTimeMillis()}"
        fileHandler.createFolderForTemporaryPhotos(internalFolderName)

        val dataForWorker = Data.Builder()
            .putString(MyApplication.fileNameTagForWorker, fileName)
            .putString(MyApplication.internalFolderNameTagForWorker, internalFolderName)
            .putInt(MyApplication.pixelSizeTagForWorker, pixelSize)
            .putString(MyApplication.multipliersTag,multipliers)
            .build()

        var outputFileOptions: OutputFileOptions
        Log.i(MyApplication.tagForTesting, "amount of photos: $amountOfPhotos")
        for (i in 0 until amountOfPhotos) {
            Log.i(MyApplication.tagForTesting, "photo number: $i")
            outputFileOptions =
                OutputFileOptions.Builder(
                    fileHandler.getFileFromInternalStorage(
                        i,
                        internalFolderName
                    )
                ).build()
            imageCapture.takePicture(outputFileOptions, cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val uri = outputFileResults.savedUri
                        Log.i(MyApplication.tagForTesting, "onImageSaved $uri")
                        if (i == amountOfPhotos - 1) {
                            val imageManipulationRequest: WorkRequest =
                                OneTimeWorkRequestBuilder<ImageManipulatorWorker>().setInputData(
                                    dataForWorker
                                ).build()
                            workManager.enqueue(imageManipulationRequest)
                            GlobalScope.launch(Dispatchers.Main) {
                                workManager.getWorkInfoByIdLiveData(imageManipulationRequest.id)
                                    .observe(activity) { workInfo ->
                                        if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.recording_activity_toast_message),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(MyApplication.tagForTesting, "error when taking picture: $exception")
                    }

                })
        }//for loop to take photos
    }//use camera

    fun stopUsingCamera() {
        cameraProvider.unbindAll() // stop using camera
    }
}