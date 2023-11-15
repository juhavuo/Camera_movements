/**
 * Images are loaded from internal storage. The bitmaps are combined to from end product. Algorithm
 * can be tuned to make some good effect. For now this happens automatically after photos have been
 * taken. The idea of separate the take of images and image processing is to make sure that image
 * handling process can not affect the performance of camera.
 *
 * NEEDS SOME KIND OF THREAD HANDLING, NOW EVERYTHING IS DONE IN COROUTINE!!!
 * IS THAT ENOUGH
 *
 * Juha Vuokko
 */
package fi.julavu.cameramovements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.set
import java.io.File

class ImageManipulator(private val context: Context) {

    //private var handler: Handler
    //private var handlerThread: HandlerThread = HandlerThread("imagemanipulatiopthread")
    private lateinit var fileHandler: FileHandler
    private var amountOfFiles = 0
    //private var fileNumber = 0
    private var bitmapBase: Bitmap? = null
    private var files: Array<File>? = null

    fun manipulate(){
        /*
        handlerThread.start()
        handler = Handler(
            handlerThread.looper
        )*/

        fileHandler = FileHandler(context)
            files = fileHandler.getTemporaryPhotoFiles()

            if (files != null) {
                amountOfFiles = files!!.size
                Log.i(MyApplication.tagForTesting,"amount of files: ${files!!.size}")
            }else{
                Log.i(MyApplication.tagForTesting,"no files in internal storage")
            }
        val directoryCreated = fileHandler.createFolderInExternalStorage(FileHandler.externalImageFolderName)
        Log.i(MyApplication.tagForTesting, "external directory created $directoryCreated")
            /*
        if(files != null) {
            for (file in files!!) {
                Log.i(MyApplication.tagForTesting,"filename: ${file.name} filesize: ${file.freeSpace}")
            }
        }*/
            var bitmap: Bitmap?
            if (files != null && files!!.isNotEmpty()) {
                Log.i(MyApplication.tagForTesting,"need to get bitmap")
                bitmap = fileHandler.getBitmap(files!![0])
                if(bitmap != null) {
                    createEmptyBitmap(bitmap)
                    addBitmapToBase(bitmap)
                    for (i in 1 until files!!.size) {
                        bitmap = fileHandler.getBitmap(files!![i])
                        if(bitmap != null) {
                            addBitmapToBase(bitmap)
                        }
                    }
                }
            }
            if (bitmapBase != null) {
                fileHandler.saveToExternalStorage(bitmapBase!!, FileHandler.externalImageFolderName)
            }else{
                Log.i(MyApplication.tagForTesting,"bitmapbase is null")
            }
            fileHandler.deleteImagesFromTemporaryStorage()
            Log.i(
                MyApplication.tagForTesting,
                " amount of files: ${fileHandler.getAmountOfFilesInTemporaryPhotos()}"
            )
            //stopBackgroundThread()
            CameraService.isBusy = false
            Log.i(MyApplication.tagForTesting,"is activity showing: ${CameraService.recordingActivityShowing}")
            if(!CameraService.recordingActivityShowing) {
                CameraService.stopService()
            }
    }

    //https://stackoverflow.com/questions/5663671/creating-an-empty-bitmap-and-drawing-though-canvas-in-android
    private fun createEmptyBitmap(bm: Bitmap){
        val width = bm.width
        val height = bm.height
        val conf = Bitmap.Config.ARGB_8888
        bitmapBase = Bitmap.createBitmap(width,height,conf)
    }

    private fun addBitmapToBase(bm: Bitmap){
        if(bitmapBase != null){
            var r: Int
            var g: Int
            var b: Int
            var rAdd: Int
            var gAdd: Int
            var bAdd: Int
            var pixel: Int
            for(x in 0 until bitmapBase!!.width){
                for(y in 0 until bitmapBase!!.height){
                    pixel = bitmapBase!!.getPixel(x,y)
                    r = Color.red(pixel)
                    g = Color.green(pixel)
                    b = Color.blue(pixel)
                    pixel = bm.getPixel(x,y)
                    rAdd = Color.red(pixel)/amountOfFiles
                    gAdd = Color.green(pixel)/amountOfFiles
                    bAdd = Color.blue(pixel)/amountOfFiles

                    bitmapBase!![x, y] = Color.rgb(r+rAdd,g+gAdd,b+bAdd)
                }
            }
        }
    }

    /*
    private fun stopBackgroundThread() {
        handlerThread.quitSafely()
        handlerThread.join()
    }*/
}