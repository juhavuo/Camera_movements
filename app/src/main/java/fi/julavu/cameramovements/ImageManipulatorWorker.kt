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
    private val multipliers: Array<Float> = arrayOf(1f,1f,1f)

    override fun doWork(): Result {

        var fileName = inputData.getString(MyApplication.fileNameTagForWorker)
        if (fileName == null){
            fileName = ""
        }

        var internalFolderName = inputData.getString(MyApplication.internalFolderNameTagForWorker)
        if(internalFolderName == null){
            internalFolderName = ""
        }

        val pixelSize = inputData.getInt(MyApplication.pixelSizeTagForWorker,1)
        Log.i(MyApplication.tagForTesting,"pixel size: $pixelSize")

        val multipliersString = inputData.getString(MyApplication.multipliersTag)
        if(!multipliersString.isNullOrEmpty()){
            val multiplierParts = multipliersString.split(',')
            if(multiplierParts.size == multipliers.size) {
                for (i in multipliers.indices) {
                    multipliers[i] = multiplierParts[i].toFloat()/100f
                }
            }
        }

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
                bitmap = fileHandler.getBitmapAndRotate(files!![0])
                if(bitmap != null) {
                    createEmptyBitmap(bitmap)
                    if(pixelSize == 1) {
                        addBitmapToBase(bitmap)
                    }else{
                        addBitmapToBase(bitmap,pixelSize)
                    }
                    for (i in 1 until files!!.size) {
                        bitmap = fileHandler.getBitmapAndRotate(files!![i])
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
                    r = Color.red(pixel)+(multipliers[0]*Color.red(pixelBM)/amountOfFiles).toInt()
                    g = Color.green(pixel)+(multipliers[1]*Color.green(pixelBM)/amountOfFiles).toInt()
                    b = Color.blue(pixel)+(multipliers[2]*Color.blue(pixelBM)/amountOfFiles).toInt()

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
                    r = Color.red(pixel)+(multipliers[0]*Color.red(pixelBM)/amountOfFiles).toInt()
                    g = Color.green(pixel)+(multipliers[1]*Color.green(pixelBM)/amountOfFiles).toInt()
                    b = Color.blue(pixel)+(multipliers[2]*Color.blue(pixelBM)/amountOfFiles).toInt()
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




