package fi.julavu.cameramovements

import android.content.Context
/*
    progress-parameter is to follow changes user makes in SettingsAdapter (seekbar progress) so changed values can easily stored
    to dataStore, if user chooses to save and quit. startValue is meant to keep original value during this prosess.
 */
class SettingsData(val title: String, val sliderMin: Int, val sliderMax: Int, val startValue: Int, val tag: String, var progress: Int){

    companion object{
        /*
            Get default values, no checking from dataStore to find actual values of parameters(= default startValue & progress)
            These values are stored in strings.xml in settings_seekbar_data. This way one can get needed tags and seekbar basic values
            and don't need to use coroutine and datastore unless it is really needed.
         */
        fun getDefaultSettingsDataList(context: Context): ArrayList<SettingsData>{
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