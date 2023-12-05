/**
 * Images are loaded from internal storage. The bitmaps are combined to from end product. Algorithm
 * can be tuned to make some good effect. For now this happens automatically after photos have been
 * taken. The idea of separate the take of images and image processing is to make sure that image
 * handling process can not affect the performance of camera.
 *
 * Image handling is done in background using workmanager.
 *
 * Juha Vuokko
 */
package fi.julavu.cameramovements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.set
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class ImageManipulatorWorker(c: Context, workerParameters: WorkerParameters): Worker(c, workerParameters) {

    private lateinit var fileHandler: FileHandler
    private val context = c
    private var amountOfFiles = 0
    private var bitmapBase: Bitmap? = null
    private var files: Array<File>? = null

    override fun doWork(): Result {

        var fileName = inputData.getString(MyApplication.fileNameTagForWorker)
        if (fileName == null){
            fileName = ""
        }

        var internalFolderName = inputData.getString(MyApplication.internalFolderNameTagForWorker)
        if(internalFolderName == null){
            internalFolderName = ""
        }

        var pixelSize = inputData.getInt(MyApplication.pixelSizeTagForWorker,1)
        Log.i(MyApplication.tagForTesting,"pixel size: $pixelSize")

        fileHandler = FileHandler(context)
            files = fileHandler.getTemporaryPhotoFiles(internalFolderName)

            if (files != null) {
                amountOfFiles = files!!.size
                Log.i(MyApplication.tagForTesting,"amount of files: ${files!!.size}")
            }else{
                Log.i(MyApplication.tagForTesting,"no files in internal storage")
            }
        val directoryCreated = fileHandler.createFolderInExternalStorage(FileHandler.externalImageFolderName)
        Log.i(MyApplication.tagForTesting, "external directory created $directoryCreated")

            var bitmap: Bitmap?
            if (files != null && files!!.isNotEmpty()) {
                Log.i(MyApplication.tagForTesting,"need to get bitmap")
                bitmap = fileHandler.getBitmap(files!![0])
                if(bitmap != null) {
                    createEmptyBitmap(bitmap)
                    if(pixelSize == 1) {
                        addBitmapToBase(bitmap)
                    }else{
                        addBitmapToBase(bitmap,pixelSize)
                    }
                    for (i in 1 until files!!.size) {
                        bitmap = fileHandler.getBitmap(files!![i])
                        if(bitmap != null) {
                            if(pixelSize == 1) {
                                addBitmapToBase(bitmap)
                            }else{
                                addBitmapToBase(bitmap,pixelSize)
                            }
                        }
                    }
                }
            }
            if (bitmapBase != null) {
                fileHandler.saveToExternalStorage(bitmapBase!!, FileHandler.externalImageFolderName, fileName)
            }else{
                Log.i(MyApplication.tagForTesting,"bitMapBase is null")
                return Result.failure()
            }
            fileHandler.deleteImagesFromTemporaryStorage(internalFolderName)
            Log.i(
                MyApplication.tagForTesting,
                " amount of files: ${fileHandler.getAmountOfFilesInTemporaryPhotos(internalFolderName)}"
            )

            Log.i(MyApplication.tagForTesting,"end of image handling")

        return Result.success()
    }

    //https://stackoverflow.com/questions/5663671/creating-an-empty-bitmap-and-drawing-though-canvas-in-android
    private fun createEmptyBitmap(bm: Bitmap){
        val width = bm.width
        val height = bm.height
        val conf = Bitmap.Config.ARGB_8888
        bitmapBase = Bitmap.createBitmap(width,height,conf)
    }

    /*
        Combine bitmap to base bitmap.
     */
    private fun addBitmapToBase(bm: Bitmap){
        if(bitmapBase != null){
            var r: Int
            var g: Int
            var b: Int
            var pixel: Int
            var pixelBM: Int

            for(x in 0 until bitmapBase!!.width){
                for(y in 0 until bitmapBase!!.height){
                    pixel = bitmapBase!!.getPixel(x, y)
                    pixelBM = bm.getPixel(x,y)
                    r = Color.red(pixel)+Color.red(pixelBM)/amountOfFiles
                    g = Color.green(pixel)+Color.green(pixelBM)/amountOfFiles
                    b = Color.blue(pixel)+Color.blue(pixelBM)/amountOfFiles

                    bitmapBase!![x, y] = Color.rgb(r,g,b)

                }//for(y...
            }//for(x...)
        }//if(bitmapBase != null)
    }//fun addBitmapToBase

    /*
     * add to bitmapbase when pixel size is set to other than 1. Now color is taken in
     * upper left corner of area and adding color to whole region with width and height
     * both pixelSize is unefficient. If there is a good way to set multiple of pixels
     * in bitmap at once, will move to that when found.
     */
    private fun addBitmapToBase(bm: Bitmap, pixelSize: Int){
        if(bitmapBase != null){
            var r: Int
            var g: Int
            var b: Int
            var pixel: Int
            var pixelBM: Int
            for(x in 0 until bitmapBase!!.width-pixelSize step pixelSize){
                for(y in 0 until bitmapBase!!.height-pixelSize step pixelSize){
                    pixel = bitmapBase!!.getPixel(x, y)
                    pixelBM = bm.getPixel(x,y)
                    r = Color.red(pixel)+Color.red(pixelBM)/amountOfFiles
                    g = Color.green(pixel)+Color.green(pixelBM)/amountOfFiles
                    b = Color.blue(pixel)+Color.blue(pixelBM)/amountOfFiles
                   //not very efficient solution
                    for(i in 0 until pixelSize) {
                        for(j in 0 until pixelSize) {
                            bitmapBase!![x+i, y+j] = Color.rgb(r, g, b)
                        }
                    }

                }//for(y...
            }//for(x...)
        }//if(bitmapBase != null)
    }//fun addBitmapToBase
}




/*

            var pixel2: Int
            var pixel3: Int
            var pixel4: Int

            var pixelBM2: Int
            var pixelBM3: Int
            var pixelBM4: Int

                   if(pixelSize < 30) {
                       pixel = bitmapBase!!.getPixel(x, y)
                       pixelBM = bm.getPixel(x,y)
                       r = Color.red(pixel)+Color.red(pixelBM)/amountOfFiles
                       g = Color.green(pixel)+Color.green(pixelBM)/amountOfFiles
                       b = Color.blue(pixel)+Color.blue(pixelBM)/amountOfFiles
                   }else{
                       pixel = bitmapBase!!.getPixel(x, y)
                       pixel2 = bitmapBase!!.getPixel(x+pixelSize-1, y)
                       pixel3 = bitmapBase!!.getPixel(x, y+pixelSize-1)
                       pixel4 = bitmapBase!!.getPixel(x+pixelSize-1, y+pixelSize-1)
                       pixelBM= bm.getPixel(x,y)
                       pixelBM2=bm.getPixel(x+pixelSize-1,y)
                       pixelBM3=bm.getPixel(x,y+pixelSize-1)
                       pixelBM4=bm.getPixel(x+pixelSize-1,y+pixelSize-1)
                       Log.i(MyApplication.tagForTesting,"p1: $pixel p2: $pixel2 p3 $pixel3 p4 $pixel4")
                       r = (Color.red(pixel)+Color.red(pixel2)+Color.red(pixel3)+Color.red(pixel4))/4
                       +(Color.red(pixelBM)+Color.red(pixelBM2)+Color.red(pixelBM3)+Color.red(pixelBM4))/(4*amountOfFiles)
                       g = (Color.green(pixel)+Color.green(pixel2)+Color.green(pixel3)+Color.green(pixel4))/4
                       +(Color.green(pixelBM)+Color.green(pixelBM2)+Color.green(pixelBM3)+Color.green(pixelBM4))/(4*amountOfFiles)
                       b = (Color.blue(pixel)+Color.blue(pixel2)+Color.blue(pixel3)+Color.blue(pixel4))/4
                       +(Color.blue(pixelBM)+Color.blue(pixelBM2)+Color.blue(pixelBM3)+Color.blue(pixelBM4))/(4*amountOfFiles)
                   }*/