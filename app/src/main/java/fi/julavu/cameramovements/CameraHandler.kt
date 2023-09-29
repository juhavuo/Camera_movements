package fi.julavu.cameramovements

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface

//https://www.freecodecamp.org/news/android-camera2-api-take-photos-and-videos/
//->problem uses mediaRecorder,that requires api 31
//https://androidwave.com/video-recording-with-camera2-api-android/
class CameraHandler(private val context: Context) {
    private var dataStoreHandler: DataStoreHandler = DataStoreHandler(context)

    //private var settingsDataList = SettingsData.getDefaultSettingsDataList(context)
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var imageReader: ImageReader
    private lateinit var imageReaderSurface: Surface
    private var fileHandler = FileHandler(context)
    private var outputSize = Size(720, 480)
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var cameraDevice: CameraDevice
    private var cameraStateCallback: CameraDevice.StateCallback =  object: CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            Log.i(MyApplication.tagForTesting, "camera opened")
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice.close()
            Log.i(MyApplication.tagForTesting, "camera disconnected")
            stopBackgroundThread()
            //cameraService?.stopSelf()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice.close()
            Log.i(MyApplication.tagForTesting, "$error")
        }
    }//CameraDevice.StateCallback
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureStateCallback: CameraCaptureSession.StateCallback
    private var cameraService: CameraService? = null

    private var sizeIndex = -1
    private var videoLength = 0
    private var framerate = 0
    private var cameraId = ""



    suspend fun fetchSettingsData() {
        val videoLengthSettingsData =
            SettingsData.getSettingsData(context, R.string.for_duration_seekbar)
        videoLength = dataStoreHandler.getSeekbarProgressValue(videoLengthSettingsData)
        sizeIndex = dataStoreHandler.getImageSizeIndex()

        Log.i("cameramovements_testing", "videolengt: $videoLength, framerate: $framerate")

    }
    /*
    fun setupMediaRecorder(width: Int, height: Int) {

        mediaRecorder = if (android.os.Build.VERSION.SDK_INT < 31) {
            MediaRecorder()
        } else {
            MediaRecorder(context)
        }
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setVideoSize(width, height)
        mediaRecorder.setVideoFrameRate(framerate)
        mediaRecorder.setOutputFile(fileHandler.getFileForVideo())
        mediaRecorder.setVideoEncodingBitRate(10_000_000)
        mediaRecorder.prepare()

        Log.i("cameramovements_testing", "${mediaRecorder.metrics}")

    }*/

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
    fun prepareCamera() {
        startBackgroundThread()
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

        cameraManager.openCamera(cameraId,cameraStateCallback,handler)

        imageReader = ImageReader.newInstance(
            outputSize.width,
            outputSize.height,
            ImageFormat.JPEG,
            1
        )
        imageReaderSurface = imageReader.surface
        imageReader.setOnImageAvailableListener({
            val image = it.acquireLatestImage()
            Log.i(MyApplication.tagForTesting, "Time to save image $image")
            //stopUsingCamera()
            it.close()
        }, null)



        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(imageReaderSurface)

        val captureCallback = object: CameraCaptureSession.CaptureCallback(){
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                Log.i(MyApplication.tagForTesting,"capturecallback oncapturecompleted")
            }
        }
        captureStateCallback = object : CameraCaptureSession.StateCallback(){
            override fun onConfigured(session: CameraCaptureSession) {
                session.setRepeatingRequest(captureRequestBuilder.build(),captureCallback,handler)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.i(MyApplication.tagForTesting,"configuration failed")
            }

        }

        cameraDevice.createCaptureSession(listOf(imageReaderSurface),captureStateCallback,null)



        val outputConfigurationList = ArrayList<OutputConfiguration>()
        outputConfigurationList.add(OutputConfiguration(imageReaderSurface))
        //val outputConfiguration = OutputConfiguration(imageReaderSurface)

        //val sessionConfiguration = SessionConfiguration(SessionConfiguration.SESSION_REGULAR,outputConfigurationList,)

        Log.i(MyApplication.tagForTesting, cameraId)
    }

    fun startUsingCamera(){
    }

    fun stopUsingCamera() {
        //stop recording
        Log.i(MyApplication.tagForTesting,"stop using camera method")
        mediaRecorder.stop()
        mediaRecorder.reset()

    }

    fun getCameraService(service: CameraService) {
        cameraService = service
    }

    private fun startBackgroundThread(){
        handlerThread = HandlerThread("camerahandlerthread")
        handlerThread.start()
        handler  = Handler(
            handlerThread.looper
        )
    }

    private fun stopBackgroundThread(){
        handlerThread.quitSafely()
        handlerThread.join()
    }


}