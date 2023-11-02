/**
 *Trying out CameraX
 *
 * Juha Vuokko
 */

package fi.julavu.cameramovements

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Size

class CameraHandler(private val context: Context) {

    private lateinit var dataStoreHandler: DataStoreHandler
    private lateinit var imageManipulator: ImageManipulator

    companion object {
        fun getSizes(context: Context): ArrayList<Size> {
            val sizes = ArrayList<Size>()
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val camIds = cameraManager.cameraIdList
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(camIds[0])
            val streamMap =
                cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (streamMap != null) {
                val sizeArray = streamMap.getOutputSizes(ImageFormat.JPEG)
                sizes.addAll(sizeArray)
            }
            return sizes
        }
    }

    fun prepareCamera(){
        Log.i(MyApplication.tagForTesting, "prepareCamera")
        dataStoreHandler = DataStoreHandler.getInstance(context)
    }

    fun useCamera(){
        Log.i(MyApplication.tagForTesting,"useCamera")
        imageManipulator = ImageManipulator(context)
    }

}