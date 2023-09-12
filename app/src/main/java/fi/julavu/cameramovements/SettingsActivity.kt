package fi.julavu.cameramovements
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsActivity : ComponentActivity() {

    private lateinit var dataStoreHandler: DataStoreHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        dataStoreHandler = DataStoreHandler(this)

        val sizes = CameraHandler.getSizes(this)
        val sizesListedForSpinner = ArrayList<String>()
        for(size in sizes){
            sizesListedForSpinner.add(size.toString())
        }

        val sizesSpinner = findViewById<Spinner>(R.id.settings_activity_size_spinner)
        val sizesSpinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,sizesListedForSpinner)
        sizesSpinner.adapter = sizesSpinnerAdapter

        val durationSettings = SettingsData.getSettingsData(this,R.string.for_duration_seekbar)
        val durationSeekBar = getAndSetupSeekbar(R.id.settings_activity_duration_view,durationSettings)
        CoroutineScope(Dispatchers.Main).launch {
            durationSeekBar.progress = dataStoreHandler.getSeekbarProgressValue(durationSettings)
        }

        //val settingsDataList = SettingsData.getDefaultSettingsDataList(this)


        /*
        val settingsRecyclerView = findViewById<RecyclerView>(R.id.settings_activity_recyclerview)
        val linearLayoutManager = LinearLayoutManager(this)
        val settingsAdapter = SettingsAdapter(settingsDataList, this)

        settingsRecyclerView.layoutManager = linearLayoutManager
        settingsRecyclerView.adapter = settingsAdapter

        var startValueForSeekbar = 0
        val dataStoreHandler = DataStoreHandler(this)

        Log.i("cameramovements_testing","$startValueForSeekbar")



        //Log.i("cameramovements_testing","${timespanSeekbar.min}")

        val saveButton = findViewById<Button>(R.id.settings_activity_save_and_quit_button)
        saveButton.setOnClickListener {
            settingsAdapter.saveSeekbarProgressesToDataStore()
            goBackToRecording()
        }

        val cancelButton = findViewById<Button>(R.id.settings_activity_cancel_button)
        cancelButton.setOnClickListener {
            goBackToRecording()
        }*/
    }

    private fun getAndSetupSeekbar(id: Int, settingsData: SettingsData): SeekBar{

        val seekBarLayout = findViewById<RelativeLayout>(id)
        val seekBar = seekBarLayout.findViewById<SeekBar>(R.id.template_seekbar)
        val seekBarMinTextView = seekBarLayout.findViewById<TextView>(R.id.seekbar_min_text_view)
        seekBarMinTextView.text = settingsData.sliderMin.toString()
        val seekBarMaxTextView = seekBarLayout.findViewById<TextView>(R.id.seekbar_max_text_view)
        seekBarMaxTextView.text = settingsData.sliderMax.toString()
        return seekBar
    }

    private fun goBackToRecording(){
        val intent = Intent(this,RecordingActivity::class.java)
        startActivity(intent)
    }
}

