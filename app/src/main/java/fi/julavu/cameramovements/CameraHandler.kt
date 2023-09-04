package fi.julavu.cameramovements

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.media.MediaRecorder
import android.util.Log

//https://www.freecodecamp.org/news/android-camera2-api-take-photos-and-videos/
//->problem uses mediaRecorder,that requires api 31
//https://androidwave.com/video-recording-with-camera2-api-android/
class CameraHandler(private val context: Context) {
    private var dataStoreHandler: DataStoreHandler = DataStoreHandler(context)
    private var settingsDataList = SettingsData.getDefaultSettingsDataList(context)
    private lateinit var imageReader: ImageReader
    private var fileHandler = FileHandler(context)
    private lateinit var mediaRecorder: MediaRecorder
    private var videoLength = 0
    private var framerate = 0
    private var cameraId = ""

    suspend fun fetchSettingsData() {
        val videoLengthSettingsData = settingsDataList.first { it.tag == "duration" }
        videoLength = dataStoreHandler.getSeekbarProgressValue(videoLengthSettingsData)
        val framerateSettingsData = settingsDataList.first { it.tag == "frames" }
        framerate = dataStoreHandler.getSeekbarProgressValue(framerateSettingsData)
        Log.i("cameramovements_testing", "videolengt: $videoLength, framerate: $framerate")

    }

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

    }

    fun prepareCamera(){
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camIds : Array<String> = cameraManager.cameraIdList
        for(camId in camIds){
            val cameraCharasteristics = cameraManager.getCameraCharacteristics(camId)
            if(cameraCharasteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK){
               cameraId = camId
                val streamMap = cameraCharasteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val outputFormats = streamMap?.outputFormats
                if(outputFormats!=null){
                    for(outputFormat in outputFormats){
                        Log.i(MyApplication.tagForTesting,"outgput: $outputFormat")
                    }
                }
                val sizes = streamMap?.getOutputSizes(SurfaceTexture::class.java)
                if (sizes != null) {
                    Log.i(MyApplication.tagForTesting,"sizes class: ${sizes::class.java}")
                    for(size in sizes){
                        Log.i(MyApplication.tagForTesting,"size: $size")
                    }
                }
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