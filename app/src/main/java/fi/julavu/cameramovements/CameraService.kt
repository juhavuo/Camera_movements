package fi.julavu.cameramovements

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraService: Service() {

    companion object{
        lateinit var instance: CameraService
        var isServiceStarted = false

        fun stopService(){
            instance.stopSelf()
        }
    }

    init{
        instance = this
    }

    private lateinit var cameraHandler: CameraHandler

    override fun onCreate() {
        Log.i(MyApplication.tagForTesting, "service on create")
        startServiceForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        cameraHandler = CameraHandler(this)
        isServiceStarted = true
        CoroutineScope(Dispatchers.Main).launch {
            cameraHandler.prepareCamera()
            cameraHandler.useCamera()

        }
        return START_STICKY //IS THIS BEST OPTION, NEED TO REVISIT THIS
    }

    override fun onDestroy() {
        isServiceStarted = false
        Log.i(MyApplication.tagForTesting, "service on destroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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