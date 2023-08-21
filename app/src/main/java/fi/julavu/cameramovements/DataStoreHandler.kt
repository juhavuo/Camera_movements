package fi.julavu.cameramovements

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

//https://stackoverflow.com/questions/66466345/proper-instance-creation-of-androids-jetpack-datastore-alpha07-version
private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreHandler(val context: Context) {

    private val timespan_tag = "TIMESPAN_VALUE"
    private val dataStore = context.dataStore

    suspend fun getTimespanValue() : Int{
        val defaultValue = context.resources.getInteger(R.integer.timespan_initial_value)
        val preferences_value = intPreferencesKey(timespan_tag)
        val timespanValueFlow: Flow<Int> = dataStore.data.map {
            preferences -> preferences[preferences_value] ?: defaultValue
        }
        val valueFromPreferences = timespanValueFlow.firstOrNull()
        if(valueFromPreferences == null){
            return defaultValue
        }else{
            return valueFromPreferences
        }
    }

    suspend fun writeTimespanValue(newTimespanValue: Int){
        val preferences_value = intPreferencesKey(timespan_tag)
        dataStore.edit {
            settings -> settings[preferences_value] = newTimespanValue
        }
    }

}