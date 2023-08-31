package fi.julavu.cameramovements

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//https://stackoverflow.com/a/46907385
//For permission handling
class PermissionHelper {

    private val ID_FOR_PERMISSIONS = 101

    fun checkAndRequestPermissions(activity: Activity, permissions: Array<String>){
        val permissionsNeeded: ArrayList<String> = ArrayList()
        for (permission in permissions){
            if(ContextCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED){
                permissionsNeeded.add(permission)
            }
        }

        if(permissionsNeeded.isNotEmpty()){
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray()
                ,ID_FOR_PERMISSIONS)
        }
    }

}