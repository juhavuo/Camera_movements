package fi.julavu.cameramovements

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class RecordingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        createNotificationChannel()
        checkPermissions()

        val serviceClass = CameraService::class.java
        val serviceIntent = Intent(applicationContext,serviceClass)

        val startButton = findViewById<Button>(R.id.recording_activity_start_button)
        startButton.setOnClickListener {
            startService(serviceIntent)
        }
        val settingsButton = findViewById<Button>(R.id.recording_activity_settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        //to enable testing
        val stopButton = findViewById<Button>(R.id.recording_activity_stop_button_for_testing)
        stopButton.setOnClickListener {
            stopService(serviceIntent)
        }

        val backButton = findViewById<Button>(R.id.recording_activity_back_button)
        backButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun createNotificationChannel(){
        val channelId = MyApplication.channelId
        val notificationChannelName = getString(R.string.notification_channel_name)
        val notificationDescription = getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId,notificationChannelName,importance)
        notificationChannel.description = notificationDescription
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun checkPermissions(){
        val permissionHelper = PermissionHelper()
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionHelper.checkAndRequestPermissions(this,permissions)
    }
}

