package fi.julavu.cameramovements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.core.graphics.set
import java.io.File

class ImageManipulator(val context: Context) {

    private var handler: Handler
    private var handlerThread: HandlerThread = HandlerThread("imagemanipulatiopthread")
    private var fileHandler: FileHandler
    private var amountOfFiles = 0
    private var fileNumber = 0
    private var bitmapBase: Bitmap? = null
    private var files: Array<File>?

    init {
        handlerThread.start()
        handler = Handler(
            handlerThread.looper
        )
        fileHandler = FileHandler(context, handler)
        files = fileHandler.getTemporaryPhotoFiles()
        if(files!=null) {
            amountOfFiles = files!!.size
        }
        /*
        if(files != null) {
            for (file in files!!) {
                Log.i(MyApplication.tagForTesting,"filename: ${file.name} filesize: ${file.freeSpace}")
            }
        }*/
        var bitmap: Bitmap? = null
        if(files != null && files!!.isNotEmpty()){
           bitmap = getBitmap(files!![0])
            createEmptyBitmap(bitmap)
            addBitmapToBase(bitmap)
            bitmap = null
            for (i in 1 until files!!.size){
                bitmap = getBitmap(files!![i])
                addBitmapToBase(bitmap)
                bitmap = null
            }
        }
        if(bitmapBase!=null) {
            fileHandler.saveEndProductToExternalStorage(bitmapBase!!)
        }
        fileHandler.deleteImagesFromTemporaryStorage()
        Log.i(MyApplication.tagForTesting, " amount of files: ${fileHandler.getAmountOfFilesInTemporaryPhotos()}")
        stopBackgroundThread()
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
            var r = 0
            var g = 0
            var b = 0
            var rAdd = 0
            var gAdd = 0
            var bAdd = 0
            var pixel = 0
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

                    bitmapBase!!.set(x,y,Color.rgb(r+rAdd,g+gAdd,b+bAdd))
                }
            }
        }
    }

    private fun getBitmap(sourceFile: File): Bitmap = BitmapFactory.decodeFile(sourceFile.path)

    private fun stopBackgroundThread() {
        handlerThread.quitSafely()
        handlerThread.join()
    }


}