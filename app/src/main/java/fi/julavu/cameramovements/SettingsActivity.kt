/**
 * Activity for selecting settings for camera and image manipulation.
 * SettingsActivity is reached from RecordingActivity. The reason these
 * are separated is to keep RecordingActivity as simple as possible so
 * when using camera one does not change settings accidentally.
 *
 * Juha Vuokko
 */

package fi.julavu.cameramovements

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var dataStoreHandler: DataStoreHandler
    //private lateinit var durationSeekBar: SeekBar
    private lateinit var sizesSpinner: Spinner
    private val settingsDataList = ArrayList<SettingsData>()
    private val seekBars = ArrayList<SeekBar>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        dataStoreHandler = DataStoreHandler.getInstance(this)

        //val sizes = CameraHandler.getSizes(this)
        val sizes = CameraHandler.getSizes(this)
        val sizesListedForSpinner = ArrayList<String>()
        for(size in sizes){
            sizesListedForSpinner.add(size.toString())
        }
        var defaultSize = sizes.size/2
        if(sizes.size == 1){
            defaultSize = 0
        }

        sizesSpinner = findViewById(R.id.settings_activity_size_spinner)
        val sizesSpinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,sizesListedForSpinner)
        sizesSpinner.adapter = sizesSpinnerAdapter

        val amountOfPhotosSettings = SettingsData.getSettingsData(this,R.string.for_amount_of_photos_seekbar)
        setupSeekbar(R.id.settings_activity_amount_of_images_view,amountOfPhotosSettings)
        CoroutineScope(Dispatchers.Main).launch {

            //when saved data is changed
            //dataStoreHandler.clearDatastore()

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

    /*
     *  Setting up seekbar. Get all data what is needed for seekbar.
     * Using include-tag in actitity_settings.xml forces to do more
     * setting up in activity. Seekbars are stored in arraylist.
     */
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

    /*
        If save settings is pressed, all the current values are saved to dataStore.
     */
    private fun saveAllSettings(){
        CoroutineScope(Dispatchers.Main).launch {
            dataStoreHandler.writeSeekbarProgressValues(settingsDataList)
            dataStoreHandler.writeImageSize(sizesSpinner.selectedItemPosition)
        }
    }


}

