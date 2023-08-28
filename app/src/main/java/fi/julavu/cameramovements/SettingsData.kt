package fi.julavu.cameramovements

import android.content.Context

class SettingsData(val title: String, val sliderMin: Int, val sliderMax: Int, val startValue: Int, val tag: String, var progress: Int){

    companion object{
        fun getSettingsDataList(context: Context): ArrayList<SettingsData>{
            val settingsDataList = ArrayList<SettingsData>()
            val rawSettingsArray = context.resources.getStringArray(R.array.settings_seekbar_data)
            for(rawSettings in rawSettingsArray){
                val parts = rawSettings.split(",")
                settingsDataList.add(SettingsData(parts[0],parts[1].toInt(),parts[3].toInt(),parts[2].toInt(),parts[4],parts[2].toInt()))
            }
            return settingsDataList
        }
    }
}