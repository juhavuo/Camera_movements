/**
 * Handling the camera. Uses camera to take series of photos and save them to internal storage.
 * After this is done, the processing in ImageHandler is started.
 *
 * Now there is massive function which basically does everything. That may need to change.
 * Juha Vuokko
 */

package fi.julavu.cameramovements

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface

//https://www.freecodecamp.org/news/android-camera2-api-take-photos-and-videos/
//https://androidwave.com/video-recording-with-camera2-api-android/
class CameraHandler(private val context: Context) {
    private var dataStoreHandler: DataStoreHandler = DataStoreHandler(context)

    //private var settingsDataList = SettingsData.getDefaultSettingsDataList(context)
    private var amountOfCaptures = 10
    private var captureCounter = 0
    private var timeToStop = false
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var imageReader: ImageReader
    private lateinit var imageReaderSurface: Surface
    private lateinit var fileHandler: FileHandler
    private lateinit var imageManipulator: ImageManipulator
    private var outputSize = Size(720, 480)
    private lateinit var cameraDevice: CameraDevice

    //this used to define cameraDevice
    private var cameraStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                Log.i(MyApplication.tagForTesting, "camera opened")
            }

            override fun onDisconnected(camera: CameraDevice) {
                //cameraDevice.close()
                Log.i(MyApplication.tagForTesting, "camera disconnected")
                //stopBackgroundThread()
                //cameraService?.stopSelf()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                cameraDevice.close()
                Log.i(MyApplication.tagForTesting, "$error")
            }
        }//CameraDevice.StateCallback

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        //when one capture is completed one is added to counter
        //when captureCounter reaches amountOfCaptures request for repeated photos
        //is stopped and bool timeToStop is set to true so camera and imageRwader can be
        //closed
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            Log.i(
                MyApplication.tagForTesting,
                "capturecallback oncapturecompleted, number $captureCounter"
            )
            ++captureCounter
            if (captureCounter >= amountOfCaptures) {
                session.stopRepeating()
                timeToStop = true
            }
        }
    }

    private lateinit var cameraManager: CameraManager
    //private lateinit var cameraCaptureSession: CameraCaptureSession
    //private var cameraService: CameraService? = null
    private var sizeIndex = -1
    //private var videoLength = 0
    private var framerate = 0
    private var cameraId = ""


    suspend fun fetchSettingsData() {
        val videoLengthSettingsData =
            SettingsData.getSettingsData(context, R.string.for_amount_of_photos_seekbar)
        amountOfCaptures = dataStoreHandler.getSeekbarProgressValue(videoLengthSettingsData)
        sizeIndex = dataStoreHandler.getImageSizeIndex()

        Log.i("cameramovements_testing", "amountOfCaptures: $amountOfCaptures, framerate: $framerate")
    }

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

    @SuppressLint("MissingPermission")
    fun useCamera() {
        startBackgroundThread()
        fileHandler = FileHandler(context)
        fileHandler.createFolderForTemporaryPhotos() //creates folder if it doesn't exist
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camIds: Array<String> = cameraManager.cameraIdList
        for (camId in camIds) {
            val cameraCharasteristics = cameraManager.getCameraCharacteristics(camId)
            if (cameraCharasteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                cameraId = camId
                val streamMap =
                    cameraCharasteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val sizes = streamMap?.getOutputSizes(ImageFormat.JPEG)
                if (sizes != null) {
                    outputSize = if (sizeIndex >= 0) {
                        sizes[sizeIndex]
                    } else {
                        if (sizes.size > 1) {
                            sizes[sizes.size / 2]
                        } else {
                            sizes[0]
                        }
                    }
                }
                Log.i(MyApplication.tagForTesting, "$outputSize")
            }//if cameraCharasteristics
        }//for(camId in camIds)

        cameraManager.openCamera(cameraId, cameraStateCallback, handler)

        imageReader = ImageReader.newInstance(
            outputSize.width,
            outputSize.height,
            ImageFormat.JPEG,
            1
        )
        imageReaderSurface = imageReader.surface

        imageReader.setOnImageAvailableListener({
            val image = it.acquireLatestImage()
            handler.post {
                Log.i(MyApplication.tagForTesting, "Time to save image $image")
                fileHandler.saveImageToTemporaryStorage(image)
                image.close()
            }
        }, handler)


        val captureRequestBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(imageReaderSurface)

        val captureStateCallback = object : CameraCaptureSession.StateCallback() {

            //when camera is ready, request for taking multiple photos is set
            override fun onConfigured(session: CameraCaptureSession) {
                session.setRepeatingRequest(
                    captureRequestBuilder.build(),
                    captureCallback,
                    handler
                )
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.i(MyApplication.tagForTesting, "configuration failed")
            }

            //when required amount of photos is taken and timeToStop is set to true
            //camera is closed and handlerThread for camera processes is stopped
            //ImageManipulator is started
            override fun onReady(session: CameraCaptureSession) {
                if (timeToStop) {
                    closeCamera()
                    stopBackgroundThread()
                    timeToStop = false
                    startImageManipulator()
                    Log.i(MyApplication.tagForTesting, "on Ready finished")
                }
            }
        }

        cameraDevice.createCaptureSession(listOf(imageReaderSurface), captureStateCallback, null)

        val outputConfigurationList = ArrayList<OutputConfiguration>()
        outputConfigurationList.add(OutputConfiguration(imageReaderSurface))
        //val outputConfiguration = OutputConfiguration(imageReaderSurface)

        //val sessionConfiguration = SessionConfiguration(SessionConfiguration.SESSION_REGULAR,outputConfigurationList,)

        Log.i(MyApplication.tagForTesting, cameraId)
    }//useCamera ends

    /*
        Closes the camera and imageReader. This function is used in CameraCaptureSession.StateCallback
        onReady, after set amount of photos is taken.
     */
    private fun closeCamera() {
        cameraDevice.close()
        imageReader.close()
    }

    //starts handler processes for camera callbacks where handler is needed
    private fun startBackgroundThread() {
        handlerThread = HandlerThread("camerahandlerthread")
        handlerThread.start()
        handler = Handler(
            handlerThread.looper
        )
    }

    //stops handler processes
    private fun stopBackgroundThread() {
        handlerThread.quitSafely()
        handlerThread.join()
    }

    //starts imageManipulator, used when CameraCaptureSession.StateCallback
    //onReady, after set amount of photos is taken. (timeToStop is set to true)
    private fun startImageManipulator() {
        imageManipulator = ImageManipulator(context)
    }


}