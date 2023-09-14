package fi.julavu.cameramovements

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.media.MediaRecorder
import android.util.Log
import android.util.Size

//https://www.freecodecamp.org/news/android-camera2-api-take-photos-and-videos/
//->problem uses mediaRecorder,that requires api 31
//https://androidwave.com/video-recording-with-camera2-api-android/
class CameraHandler(private val context: Context) {
    private var dataStoreHandler: DataStoreHandler = DataStoreHandler(context)
    //private var settingsDataList = SettingsData.getDefaultSettingsDataList(context)
    private lateinit var imageReader: ImageReader
    private var fileHandler = FileHandler(context)
    private var outputSize = Size(720,480)
    private lateinit var mediaRecorder: MediaRecorder
    private var sizeIndex = -1
    private var videoLength = 0
    private var framerate = 0
    private var cameraId = ""

    suspend fun fetchSettingsData() {
        val videoLengthSettingsData = SettingsData.getSettingsData(context,R.string.for_duration_seekbar)
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
        fun getSizes(context: Context): ArrayList<Size>{
            val sizes = ArrayList<Size>()
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val camIds = cameraManager.cameraIdList
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(camIds[0])
            val streamMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if(streamMap != null) {
                val sizeArray = streamMap.getOutputSizes(ImageFormat.JPEG)
                sizes.addAll(sizeArray)
            }
            return sizes
        }
    }

    fun prepareCamera(){
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camIds : Array<String> = cameraManager.cameraIdList
        for(camId in camIds){
            val cameraCharasteristics = cameraManager.getCameraCharacteristics(camId)
            if(cameraCharasteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
               cameraId = camId
                val streamMap = cameraCharasteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val sizes = streamMap?.getOutputSizes(ImageFormat.JPEG)
                if(sizes != null) {
                    if (sizeIndex >= 0) {
                        outputSize = sizes[sizeIndex]
                    }else{
                        if(sizes.size > 1){
                            outputSize = sizes[sizes.size/2]
                        }else{
                            outputSize = sizes[0]
                        }
                    }
                }
                Log.i(MyApplication.tagForTesting,"$outputSize")
                /*
                if (sizes != null) {
                    Log.i(MyApplication.tagForTesting,"sizes class: ${sizes::class.java}")
                    for(size in sizes){
                        Log.i(MyApplication.tagForTesting,"size: $size, ratio: ${size.width.toDouble()/size.height}")

                    }
                }*/
            }
        }
        Log.i(MyApplication.tagForTesting,cameraId)
    }

    fun stopUsingCamera(){
        //stop recording
        mediaRecorder.stop()
        mediaRecorder.reset()

    }


}