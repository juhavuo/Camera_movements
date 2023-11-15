package fi.julavu.cameramovements

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import androidx.activity.ComponentActivity

class RecordingActivity : ComponentActivity() {

    private lateinit var cameraService: CameraService
    private var isBound = false
    private lateinit var serviceIntent: Intent

    private val serviceConnection = object: ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CameraService.CameraBinder
            cameraService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
           isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        createNotificationChannel()
        checkPermissions()

        val serviceClass = CameraService::class.java
        serviceIntent = Intent(applicationContext,serviceClass)

        val startButton = findViewById<Button>(R.id.recording_activity_start_button)
        startButton.setOnClickListener {
            if(CameraService.isServiceStarted){
                if(isBound){
                    cameraService.takePhotos()
                }
            }
        }
        val settingsButton = findViewById<Button>(R.id.recording_activity_settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        //for testing phase
        val stopServiceButton = findViewById<Button>(R.id.recording_activity_stop_service_button)
        stopServiceButton.setOnClickListener {
            if(CameraService.isServiceStarted){
                stopService(serviceIntent)
            }
        }


        val backButton = findViewById<Button>(R.id.recording_activity_back_button)
        backButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        if(!CameraService.isServiceStarted){
            startService(serviceIntent)
        }

        CameraService.recordingActivityShowing = true
    }

    override fun onStart() {
        super.onStart()
        Intent(this, CameraService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
        isBound = false
    }

    override fun onDestroy() {
        super.onDestroy()
        CameraService.recordingActivityShowing = false
        if(CameraService.isServiceStarted && !CameraService.isBusy){
            stopService(serviceIntent)
        }
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

