package fi.julavu.cameramovements

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraHandler(context: Context) {
    private var dataStoreHandler: DataStoreHandler = DataStoreHandler(context)
    private var settingsDataList = SettingsData.getDefaultSettingsDataList(context)
    private var videoLength = 0
    private var framerate = 0

    fun doPreparation() {
        CoroutineScope(Dispatchers.Main).launch {
            val videoLengthSettingsData = settingsDataList.first{it.tag == "duration"}
            videoLength = dataStoreHandler.getSeekbarProgressValue(videoLengthSettingsData)
            val framerateSettingsData = settingsDataList.first{it.tag == "frames"}
            framerate = dataStoreHandler.getSeekbarProgressValue(framerateSettingsData)
            Log.i("cameramovements_testing", "videolengt: $videoLength, framerate: $framerate")
        }
    }

}