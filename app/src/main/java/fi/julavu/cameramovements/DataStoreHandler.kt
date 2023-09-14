package fi.julavu.cameramovements

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
//https://stackoverflow.com/questions/66466345/proper-instance-creation-of-androids-jetpack-datastore-alpha07-version
private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreHandler(val context: Context) {

    //private val timespan_tag = "TIMESPAN_VALUE"
    private val dataStore = context.dataStore
    private val sizeTag = "image_size"

    /*
    suspend fun getSeekbarProgressValues(settingsDataList: ArrayList<SettingsData>): ArrayList<Int> {
        val seekbarProgressValues = ArrayList<Int>()

        for (settingsData in settingsDataList) {
            val defaultValue = settingsData.startValue
            val preferencesValue = intPreferencesKey(settingsData.tag)
            val preferencesFlow: Flow<Int> = dataStore.data.map { preferences ->
                preferences[preferencesValue] ?: defaultValue
            }
            val valueFromPreferences = preferencesFlow.firstOrNull()
            if (valueFromPreferences == null) {
                seekbarProgressValues.add(defaultValue)
            } else {
                seekbarProgressValues.add(valueFromPreferences)
            }
        }

        return seekbarProgressValues
    }*/

    suspend fun getSeekbarProgressValue(settingsData: SettingsData): Int {

        val defaultValue = settingsData.startValue
        var seekbarProgressValue = defaultValue
        val preferencesValue = intPreferencesKey(settingsData.tag)
        val preferencesFlow: Flow<Int> = dataStore.data.map { preferences ->
            preferences[preferencesValue] ?: defaultValue
        }
        val valueFromPreferences = preferencesFlow.firstOrNull()
        if (valueFromPreferences != null) {
            seekbarProgressValue = valueFromPreferences
        }
        return seekbarProgressValue
    }

    suspend fun getImageSizeIndex(): Int {
        val defaultValue = -1
        val preferencesValue = intPreferencesKey(sizeTag)
        val preferencesFlow: Flow<Int> = dataStore.data.map { preferences ->
            preferences[preferencesValue] ?: defaultValue
        }
        return preferencesFlow.firstOrNull() ?: defaultValue
    }

    /*
    suspend fun writeTimespanValue(newTimespanValue: Int){
        val preferences_value = intPreferencesKey(timespan_tag)
        dataStore.edit {
            settings -> settings[preferences_value] = newTimespanValue
        }
    }*/

    suspend fun writeSeekbarProgressValues(settingsDataList: ArrayList<SettingsData>){
        for(settingsData in settingsDataList){
            val preferencesValue = intPreferencesKey(settingsData.tag)
            dataStore.edit {
                settings -> settings[preferencesValue] = settingsData.progress
            }
        }
    }

    suspend fun writeImageSize(sizeIndex: Int){
        val preferencesValue = intPreferencesKey(sizeTag)
        dataStore.edit {
            settings -> settings[preferencesValue] = sizeIndex
        }
    }

}