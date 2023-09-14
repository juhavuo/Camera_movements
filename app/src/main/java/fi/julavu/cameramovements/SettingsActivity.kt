package fi.julavu.cameramovements
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
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
    private lateinit var durationSeekBar: SeekBar
    private lateinit var sizesSpinner: Spinner
    private val settingsDataList = ArrayList<SettingsData>()
    private val seekBars = ArrayList<SeekBar>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        dataStoreHandler = DataStoreHandler(this)

        val sizes = CameraHandler.getSizes(this)
        val sizesListedForSpinner = ArrayList<String>()
        for(size in sizes){
            sizesListedForSpinner.add(size.toString())
        }
        var defaultSize = sizes.size/2
        if(sizes.size == 1){
            defaultSize = 0
        }

        sizesSpinner = findViewById<Spinner>(R.id.settings_activity_size_spinner)
        val sizesSpinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,sizesListedForSpinner)
        sizesSpinner.adapter = sizesSpinnerAdapter

        val durationSettings = SettingsData.getSettingsData(this,R.string.for_duration_seekbar)
        setupSeekbar(R.id.settings_activity_duration_view,durationSettings)
        CoroutineScope(Dispatchers.Main).launch {
          for(i in 0 until seekBars.size){
              settingsDataList[i].progress = dataStoreHandler.getSeekbarProgressValue(settingsDataList[i])
              seekBars[i].progress = settingsDataList[i].progress
              val sizeIndex = dataStoreHandler.getImageSizeIndex()
              if(sizeIndex>=0) {
                  sizesSpinner.setSelection(sizeIndex)
              }else{
                  sizesSpinner.setSelection(defaultSize)
              }
          }
        }
        for(i in 0 until seekBars.size){
            seekBars[i].setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    settingsDataList[i].progress = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            })
        }

        /*

        val durationSettings = SettingsData.getSettingsData(this,R.string.for_duration_seekbar)
        durationSeekBar = getAndSetupSeekbar(R.id.settings_activity_duration_view,durationSettings)
        CoroutineScope(Dispatchers.Main).launch {
            durationSettings.progress = dataStoreHandler.getSeekbarProgressValue(durationSettings)
            durationSeekBar.progress = durationSettings.progress
            settingsDataList.add(durationSettings)
        }

        durationSeekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                settingsDataList[settingsDataList.indexOfFirst { it.tag == "duration" }].progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        */
        //val settingsDataList = SettingsData.getDefaultSettingsDataList(this)
        /*
        val settingsRecyclerView = findViewById<RecyclerView>(R.id.settings_activity_recyclerview)
        val linearLayoutManager = LinearLayoutManager(this)
        val settingsAdapter = SettingsAdapter(settingsDataList, this)

        settingsRecyclerView.layoutManager = linearLayoutManager
        settingsRecyclerView.adapter = settingsAdapter

        var startValueForSeekbar = 0
        val dataStoreHandler = DataStoreHandler(this)

*/

        val saveButton = findViewById<Button>(R.id.settings_activity_save_and_quit_button)
        saveButton.setOnClickListener {
            saveAllSettings()
            goBackToRecording()
        }

        val cancelButton = findViewById<Button>(R.id.settings_activity_cancel_button)
        cancelButton.setOnClickListener {
            goBackToRecording()
        }
    }

    private fun setupSeekbar(id: Int, settingsData: SettingsData){

        val seekBarLayout = findViewById<RelativeLayout>(id)
        val seekBar = seekBarLayout.findViewById<SeekBar>(R.id.template_seekbar)
        val seekBarTitle = seekBarLayout.findViewById<TextView>(R.id.seekbar_title)
        seekBarTitle.text = settingsData.title
        val seekBarMinTextView = seekBarLayout.findViewById<TextView>(R.id.seekbar_min_text_view)
        seekBarMinTextView.text = settingsData.sliderMin.toString()
        val seekBarMaxTextView = seekBarLayout.findViewById<TextView>(R.id.seekbar_max_text_view)
        seekBarMaxTextView.text = settingsData.sliderMax.toString()
        seekBars.add(seekBar)
        settingsDataList.add(settingsData)
    }

    private fun goBackToRecording(){
        val intent = Intent(this,RecordingActivity::class.java)
        startActivity(intent)
    }

    private fun saveAllSettings(){
        CoroutineScope(Dispatchers.Main).launch {
            dataStoreHandler.writeSeekbarProgressValues(settingsDataList)
            dataStoreHandler.writeImageSize(sizesSpinner.selectedItemPosition)
        }
    }


}

