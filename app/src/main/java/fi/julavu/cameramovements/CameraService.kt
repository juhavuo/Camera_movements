package fi.julavu.cameramovements

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraService: LifecycleService() {

    companion object{
        lateinit var instance: CameraService
        var isServiceStarted = false
        var recordingActivityShowing = true
        var isBusy = false

        fun stopService(){
            instance.stopSelf()
        }
    }

    init{
        instance = this
    }

    private lateinit var cameraHandler: CameraHandler
    private val binder = CameraBinder()

    inner class CameraBinder : Binder(){
        fun getService(): CameraService = this@CameraService
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(MyApplication.tagForTesting, "service on create")
        startServiceForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        cameraHandler = CameraHandler(this)
        isServiceStarted = true
        CoroutineScope(Dispatchers.Main).launch {
            cameraHandler.getSettings()
            cameraHandler.prepareCamera()

        }
        return START_STICKY //IS THIS BEST OPTION, NEED TO REVISIT THIS
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHandler.stopUsingCamera()
        isServiceStarted = false
        Log.i(MyApplication.tagForTesting, "service on destroy")
    }

    fun takePhotos(){
        cameraHandler.useCamera()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private fun startServiceForeground(){
        val pendingIntent: PendingIntent = Intent(this, CameraService::class.java)
            .let { notificationIntent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            }
        val notificationBuilder: Notification.Builder = Notification.Builder(this,MyApplication.channelId)
            .setContentTitle(getText(R.string.notification_channel_title))
            .setContentText(getText(R.string.notification_channel_description))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)

        val notification = notificationBuilder.build()

        startForeground(MyApplication.notificationId,notification)
    }
}