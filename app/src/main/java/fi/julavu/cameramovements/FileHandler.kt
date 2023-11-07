/**
 * To handle all file operations needed. First save photos to internal storage. After all photos are taken
 * load the bitmaps to do time consuming operations. After operations save the result to external storage
 * and remove the temporary files.
 *
 * Juha Vuokko
 */

package fi.julavu.cameramovements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import kotlin.math.roundToInt

class FileHandler(private val context: Context) {

    private val usedDirectory = Environment.DIRECTORY_PICTURES
    private val nameOfInternalFolder = "temporaryphotos"

    private var fileNumber = 0
    private val fileNameStart = "temp"
    private val fileType = ".jpg"

    companion object{
        const val externalImageFolderName = "CameraMovementsImages"
        const val nameOfFolderForPhotoSamples ="CameraMovementsTesting" //for checking that camera works correctly
    }

    /**
     * Create folder in internal storage.
     */
    fun createFolderForTemporaryPhotos(){
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
        folderForTemporaryPhotos.mkdir()
    }

    private fun getExternalStoragePath(externalFolderName: String): File = File(Environment.getExternalStoragePublicDirectory(usedDirectory),externalFolderName)

    fun createFolderInExternalStorage(externalFolderName: String): Boolean{
        var succeeded = false
        val externalStorageFolder = getExternalStoragePath(externalFolderName)
        try {
            externalStorageFolder.mkdir()
            succeeded = true
        }catch (secExp: SecurityException){
            Log.e(MyApplication.tagForTesting, "failing creating folder: $secExp")
        }
        return succeeded
    }

    /**
        Get Files from tempory storage. If directory doesn't exist (that should not happen),
        function returns null
     */
    fun getTemporaryPhotoFiles():Array<File>?{
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
        return if(folderForTemporaryPhotos.exists()){
            folderForTemporaryPhotos.listFiles()
        }else{
            null
        }
    }

    /**
        Get all files from external storage, if directory doesn't exist returns null
     */
    fun getImageFilesFromExternalStorage(externalFolderName: String): Array<File>?{
        val externalStorageFolder = getExternalStoragePath(externalFolderName)
        return if(externalStorageFolder.exists()){
            externalStorageFolder.listFiles()
        }else{
            null
        }
    }

    fun getAmountOfFilesInTemporaryPhotos(): Int{
        val files = getTemporaryPhotoFiles()
        return files?.size ?: 0
    }

    /**
     * This is for save images in both save functions that are used
     * to save to internal storage and to external one. Return value
     * tells if image was saved successfully.
     */
    private fun saveImageToFolder(bitmap: Bitmap, fileToSave: File): Boolean{
        var isSaved = false
        var fileOutputStream: FileOutputStream? = null
        try{
            fileOutputStream = FileOutputStream(fileToSave)
            //Log.i(MyApplication.tagForTesting,"fileoutputstream: $fileOutputStream")
            val bitmapOK = bitmap.compress(Bitmap.CompressFormat.JPEG,80,fileOutputStream)
            Log.i(MyApplication.tagForTesting,"bitmap compress: $bitmapOK")
            isSaved = true
        }catch (ioException: IOException){
            Log.e(MyApplication.tagForTesting,ioException.toString())
        }finally {
            fileOutputStream?.close()
        }
        return isSaved
    }

    fun getFileFromInternalStorage(number: Int): File{
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
        return File(folderForTemporaryPhotos,"$fileNameStart$number$fileType")

    }

    /**
     * The file is saved as temp{number}.jpg to internal storage
     */
    fun saveImageToTemporaryStorage(image: Image){
            val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
            val file = File(folderForTemporaryPhotos,"$fileNameStart$fileNumber$fileType")
            //Log.i(MyApplication.tagForTesting,"name: ${file.name} path: ${file.path}")
            //https://stackoverflow.com/questions/41775968/how-to-convert-android-media-image-to-bitmap-object
            val byteBuffer = image.planes[0].buffer
            val bytes = ByteArray(byteBuffer.capacity())
            byteBuffer.get(bytes)
            Log.i(MyApplication.tagForTesting, "bytes size: $bytes")
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
            val isSaved = saveImageToFolder(bitmap,file)
            if(isSaved) {
                ++fileNumber
            }
    }

    fun saveImageToTemporaryStorage(image: Image, alsoToExternal: Boolean, externalFolderName: String){
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
        val file = File(folderForTemporaryPhotos,"$fileNameStart$fileNumber$fileType")
        //Log.i(MyApplication.tagForTesting,"name: ${file.name} path: ${file.path}")
        //https://stackoverflow.com/questions/41775968/how-to-convert-android-media-image-to-bitmap-object
        val byteBuffer = image.planes[0].buffer
        val bytes = ByteArray(byteBuffer.capacity())
        byteBuffer.get(bytes)
        //Log.i(MyApplication.tagForTesting, "bytes size: $bytes")
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        val isSaved = saveImageToFolder(bitmap,file)
        if(alsoToExternal){
            Log.i(MyApplication.tagForTesting,"time to save photo to external storage")
            saveToExternalStorage(bitmap, externalFolderName)
        }
        if(isSaved) {
            ++fileNumber
        }
    }
/*
    fun saveImageToTemporaryStorage(uri: Uri, alsoToExternal: Boolean, externalFolderName: String){
        //Log.i(MyApplication.tagForTesting,"name: ${file.name} path: ${file.path}")
        //https://stackoverflow.com/questions/41775968/how-to-convert-android-media-image-to-bitmap-object
        val bitmap =
        val isSaved = saveImageToFolder(bitmap,file)
        if(alsoToExternal){
            Log.i(MyApplication.tagForTesting,"time to save photo to external storage")
            saveToExternalStorage(bitmap, externalFolderName)
        }
        if(isSaved) {
            ++fileNumber
        }
    }*/

    /**
        Deletes all files from temporary storage. This used after image operations are done to
        save space.
     */
    fun deleteImagesFromTemporaryStorage(){
       val files = getTemporaryPhotoFiles()
        if(files != null){
            for(file in files){
                file.delete()
            }
        }
    }

    /**
     * Save file as image{timestamp}.jpg and image name is given as return value
     */
    fun saveToExternalStorage(bitmap: Bitmap, externalFolderName: String): String{
        val imageName = "image${System.currentTimeMillis()}.jpg"
        val externalDir = getExternalStoragePath(externalFolderName)
        val imageFile = File(externalDir,imageName)
        val isSaved = saveImageToFolder(bitmap,imageFile)
        Log.i(MyApplication.tagForTesting,"saving to external storage: $isSaved")
        return imageName
    }



    /**
     * Change file name in external storage.
     */
    //https://mkyong.com/java/how-to-rename-file-in-java/
    fun renameSavedFile(fileName: String){
        val externalPath = getExternalStoragePath(externalImageFolderName).toPath()
        Files.move(externalPath,externalPath.resolveSibling("$fileName$fileType"))
    }

    fun getBitmap(sourceFile: File): Bitmap?{
        val bitmap: Bitmap? = null
        try {
            BitmapFactory.decodeFile(sourceFile.absolutePath)
        }catch (e: Exception){
            Log.e(MyApplication.tagForTesting,"in getBitmap, Filehandler: $e")
        }
        return bitmap
    }

    /**
     * Get thumbnail to be shown in ImageGalleryActivity.
     */
    //https://stackoverflow.com/questions/14110163/getting-image-thumbnail-in-android
    fun getThumbnail(imageFile: File, thumbnailWidth: Int): Bitmap?{
        val bitmap = getBitmap(imageFile)
        return if(bitmap != null) {
            val aspectRatio = (bitmap.width) / ((bitmap.height).toFloat())
            val thumbnailHeight: Int = (thumbnailWidth / aspectRatio).roundToInt()
            Bitmap.createScaledBitmap(bitmap, thumbnailWidth, thumbnailHeight, false)
        } else null
    }
}