package fi.julavu.cameramovements

import android.content.Context
/*
    progress-parameter is to follow changes user makes in SettingsAdapter (seekbar progress) so changed values can easily stored
    to dataStore, if user chooses to save and quit. startValue is meant to keep original value during this prosess.
 */
class SettingsData(val title: String, val sliderMin: Int, val sliderMax: Int, val startValue: Int, val tag: String, var progress: Int){

    companion object{

        fun getSettingsData(context: Context, stringId: Int): SettingsData{
            val settingsString = context.resources.getString(stringId)
            val parts = settingsString.split(",")
            return SettingsData(parts[0],parts[1].toInt(),parts[3].toInt(),parts[2].toInt(),parts[4],parts[2].toInt())
        }
    }//companion object
}