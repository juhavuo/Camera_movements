package fi.julavu.cameramovements

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaRecorder
import android.util.Log
import android.view.TextureView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

//https://www.freecodecamp.org/news/android-camera2-api-take-photos-and-videos/
//->problem uses mediaRecorder,that requires api 31
//https://androidwave.com/video-recording-with-camera2-api-android/
class CameraHandler(private val context: Context) {
    private var dataStoreHandler: DataStoreHandler = DataStoreHandler(context)
    private var settingsDataList = SettingsData.getDefaultSettingsDataList(context)
    private var fileHandler = FileHandler(context)
    private var videoLength = 0
    private var framerate = 0

    suspend fun doPreparation() {
        val videoLengthSettingsData = settingsDataList.first { it.tag == "duration" }
        videoLength = dataStoreHandler.getSeekbarProgressValue(videoLengthSettingsData)
        val framerateSettingsData = settingsDataList.first { it.tag == "frames" }
        framerate = dataStoreHandler.getSeekbarProgressValue(framerateSettingsData)
        Log.i("cameramovements_testing", "videolengt: $videoLength, framerate: $framerate")

    }

    fun setupMediaRecorder(width: Int, height: Int) {

        val mediaRecorder: MediaRecorder = if (android.os.Build.VERSION.SDK_INT < 31) {
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

    fun record() {}


}