package fi.julavu.cameramovements
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsDataList = SettingsData.getDefaultSettingsDataList(this)

        val settingsRecyclerView = findViewById<RecyclerView>(R.id.settings_activity_recyclerview)
        val linearLayoutManager = LinearLayoutManager(this)
        val settingsAdapter = SettingsAdapter(settingsDataList, this)

        settingsRecyclerView.layoutManager = linearLayoutManager
        settingsRecyclerView.adapter = settingsAdapter
        /*
        var startValueForSeekbar = 0
        val dataStoreHandler = DataStoreHandler(this)

        Log.i("cameramovements_testing","$startValueForSeekbar")

        val timespanMinValueTextview = findViewById<TextView>(R.id.settings_activity_timespan_seekbar_min_text_view)
        timespanMinValueTextview.text = resources.getInteger(R.integer.timespan_min).toString()

        val timespanMaxValueTextView = findViewById<TextView>(R.id.settings_activity_timespan_seekbar_max_text_view)
        timespanMaxValueTextView.text = resources.getInteger(R.integer.timespan_max).toString()

        val timespanSeekbar = findViewById<SeekBar>(R.id.settings_activity_timespan_seekbar)
        CoroutineScope(Dispatchers.Main).launch {
            startValueForSeekbar = dataStoreHandler.getTimespanValue()
            timespanSeekbar.progress = startValueForSeekbar
        }*/

        //Log.i("cameramovements_testing","${timespanSeekbar.min}")

        val saveButton = findViewById<Button>(R.id.settings_activity_save_and_quit_button)
        saveButton.setOnClickListener {
            settingsAdapter.saveSeekbarProgressesToDataStore()
            goBackToRecording()
        }

        val cancelButton = findViewById<Button>(R.id.settings_activity_cancel_button)
        cancelButton.setOnClickListener {
            goBackToRecording()
        }
    }

    private fun goBackToRecording(){
        val intent = Intent(this,RecordingActivity::class.java)
        startActivity(intent)
    }
}

