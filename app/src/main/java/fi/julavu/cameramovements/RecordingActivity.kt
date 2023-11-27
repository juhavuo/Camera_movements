/**
 * RecordingActivity. Camera is used directly in this activity. When all photos has been taken
 * ImageManipulationWorker is started. It does image manipulation in the background.
 *
 * Juha Vuokko
 */

package fi.julavu.cameramovements

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordingActivity : ComponentActivity() {

    private val cameraHandler: CameraHandler = CameraHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        Log.i(MyApplication.tagForTesting,"recording activity on create")

        checkPermissions()

        val fileHandler = FileHandler(this)

        CoroutineScope(Dispatchers.Main).launch {
            cameraHandler.getSettings()
            cameraHandler.prepareCamera()
        }

        val warningsTextview = findViewById<TextView>(R.id.recording_activity_warnings_textview)

        val fileNameEditText = findViewById<EditText>(R.id.recording_activity_file_name_edittext)
        fileNameEditText.doAfterTextChanged {
            warningsTextview.text = ""
            //This can be changed later
            //if there is performance issues
            val name = it.toString()
            Thread{
                val doesExist = fileHandler.checkIfFileExists(name)
                if(doesExist){
                    runOnUiThread{
                        warningsTextview.text = resources.getString(R.string.recording_activity_file_exists)
                    }
                }
            }.start()
        }

        val startButton = findViewById<Button>(R.id.recording_activity_start_button)
        startButton.setOnClickListener {
            cameraHandler.useCamera(fileNameEditText.text.toString()+".jpg")
        }
        val settingsButton = findViewById<Button>(R.id.recording_activity_settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val backButton = findViewById<Button>(R.id.recording_activity_back_button)
        backButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHandler.stopUsingCamera()
        Log.i(MyApplication.tagForTesting,"recording activity, on destroy")
    }

    private fun checkPermissions(){
        val permissionHelper = PermissionHelper()
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionHelper.checkAndRequestPermissions(this,permissions)
    }
}

