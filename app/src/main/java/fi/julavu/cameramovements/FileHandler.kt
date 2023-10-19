package fi.julavu.cameramovements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Environment
import android.os.Handler
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files

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

    fun getAmountOfFilesInTemporaryPhotos(): Int{
        val files = getTemporaryPhotoFiles()
        return files?.size ?: 0
    }

    fun getFileForImage(): File {
        return File(context.getExternalFilesDir(usedDirectory), albumNameForVideos)
    }

    private fun saveImageToFolder(bitmap: Bitmap, fileToSave: File): Boolean{
        var isSaved = false
        var fileOutputStream: FileOutputStream? = null

        //Log.i(MyApplication.tagForTesting,"bytebuffer: size ${byteBuffer.capacity()}")
        //Log.i(MyApplication.tagForTesting,"bitmap: ${bitmap.width} ${bitmap.height}")
        try{
            fileOutputStream = FileOutputStream(fileToSave)
            Log.i(MyApplication.tagForTesting,"fileoutputstream: $fileOutputStream")
            bitmap.compress(Bitmap.CompressFormat.JPEG,80,fileOutputStream)
            isSaved = true
        }catch (ioException: IOException){
            Log.e(MyApplication.tagForTesting,ioException.toString())
        }finally {
            fileOutputStream?.close()
        }

        return isSaved
    }

    fun saveImageToTemporaryStorage(image: Image){
            val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
            val file = File(folderForTemporaryPhotos,"$fileNameStart$fileNumber$fileType")
            Log.i(MyApplication.tagForTesting,"name: ${file.name} path: ${file.path}")
            //https://stackoverflow.com/questions/41775968/how-to-convert-android-media-image-to-bitmap-object
            val byteBuffer = image.planes[0].buffer
            val bytes = ByteArray(byteBuffer.capacity())
            byteBuffer.get(bytes)
            Log.i(MyApplication.tagForTesting, "bytes size: $bytes")
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
            val isSaved = saveImageToFolder(bitmap,file)
        /*
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
            }*/
            if(isSaved) {
                ++fileNumber
            }
    }

    fun deleteImagesFromTemporaryStorage(){
       val files = getTemporaryPhotoFiles()
        if(files != null){
            for(file in files){
                file.delete()
            }
        }
    }

    fun saveEndProductToExternalStorage(bitmap: Bitmap): String{
        val imageName = "image${System.currentTimeMillis()}.jpg"
        val externalDir = Environment.getExternalStoragePublicDirectory(usedDirectory)
        val imageFile = File(externalDir,imageName)
        saveImageToFolder(bitmap,imageFile)
        return imageName
    }

    //https://mkyong.com/java/how-to-rename-file-in-java/
    fun renameSavedFile(fileName: String){
        val externalPath = Environment.getExternalStoragePublicDirectory(usedDirectory).toPath()
        Files.move(externalPath,externalPath.resolveSibling("fileName$fileType"))
    }
}