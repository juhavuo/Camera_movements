package fi.julavu.cameramovements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileHandler(val context: Context, val handler: Handler) {

    private val usedDirectory = Environment.DIRECTORY_PICTURES
    private val albumNameForVideos = "cameramovementsrawvideos"
    private val nameOfInternalFolder = "temporaryphotos"
    private var fileNumber = 0
    private val fileNameStart = "temp"
    private val fileType = ".jpg"

    fun createFolderForTemporaryPhotos(){
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
        folderForTemporaryPhotos.mkdir()
    }

    fun getTemporaryPhotoFiles():Array<File>?{
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
        return if(folderForTemporaryPhotos.exists()){
            folderForTemporaryPhotos.listFiles()
        }else{
            null
        }
    }

    /*
    fun getTemporaryPhotoFile(number: Int): File?{
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
    }*/

    /*
    fun getAmountOfFilesInTemporaryPhotos(): Int{
        val files = getTemporaryPhotoFiles()
        return files?.size ?: 0
    }*/

    fun getFileForImage(): File {
        return File(context.getExternalFilesDir(usedDirectory), albumNameForVideos)
    }

    fun saveImageToTemporaryStorage(image: Image){
            val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
            val file = File(folderForTemporaryPhotos,"$fileNameStart$fileNumber$fileType")
            Log.i(MyApplication.tagForTesting,"name: ${file.name} path: ${file.path}")
            //https://stackoverflow.com/questions/41775968/how-to-convert-android-media-image-to-bitmap-object
            var fileOutputStream: FileOutputStream? = null
            val byteBuffer = image.planes[0].buffer
            //Log.i(MyApplication.tagForTesting,"bytebuffer: size ${byteBuffer.capacity()}")
            val bytes = ByteArray(byteBuffer.capacity())
            byteBuffer.get(bytes)
            Log.i(MyApplication.tagForTesting, "bytes size: $bytes")
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
            Log.i(MyApplication.tagForTesting,"bitmap: ${bitmap.width} ${bitmap.height}")
            try{
                fileOutputStream = FileOutputStream(file)
                Log.i(MyApplication.tagForTesting,"fileoutputstream: $fileOutputStream")
                bitmap.compress(Bitmap.CompressFormat.JPEG,80,fileOutputStream)
            }catch (ioException: IOException){
                Log.e(MyApplication.tagForTesting,ioException.toString())
            }finally {
                fileOutputStream?.close()
            }
           ++fileNumber

    }

    fun deleteImagesFromTemporaryStorage(){
       val files = getTemporaryPhotoFiles()
        if(files != null){
            for(file in files){
                file.delete()
            }
        }
    }

    fun saveEndProductToExternalStorage(){

    }
}