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
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files

class FileHandler(private val context: Context) {

    private val usedDirectory = Environment.DIRECTORY_PICTURES
    private val nameOfInternalFolder = "temporaryphotos"
    private val externalStorageFolderName = "CameraMovementsImages"
    private var fileNumber = 0
    private val fileNameStart = "temp"
    private val fileType = ".jpg"

    /**
     * Create folder in internal storage.
     */
    fun createFolderForTemporaryPhotos(){
        val folderForTemporaryPhotos = context.getDir(nameOfInternalFolder,Context.MODE_PRIVATE)
        folderForTemporaryPhotos.mkdir()
    }

    private fun getExternalStoragePath(): File = File(Environment.getExternalStoragePublicDirectory(usedDirectory),externalStorageFolderName)

    fun createFolderInExternalStorage(): Boolean{
        var succeeded = false
        val externalStorageFolder = getExternalStoragePath()
        try {
            externalStorageFolder.mkdir()
            succeeded = true
        }catch (secExp: SecurityException){
            Log.e(MyApplication.tagForTesting, "failing creating folder: ${secExp.toString()}")
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
    fun getImageFilesFromExternalStorage(): Array<File>?{
        val externalStorageFolder = getExternalStoragePath()
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

    fun saveImageToTemporaryStorage(image: Image, alsoToExternal: Boolean){
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
            saveEndProductToExternalStorage(bitmap)
        }
        if(isSaved) {
            ++fileNumber
        }
    }

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
    fun saveEndProductToExternalStorage(bitmap: Bitmap): String{
        val imageName = "image${System.currentTimeMillis()}.jpg"
        val externalDir = getExternalStoragePath()
        val imageFile = File(externalDir,imageName)
        saveImageToFolder(bitmap,imageFile)
        return imageName
    }

    /**
     * Change file name in external storage.
     */
    //https://mkyong.com/java/how-to-rename-file-in-java/
    fun renameSavedFile(fileName: String){
        val externalPath = getExternalStoragePath().toPath()
        Files.move(externalPath,externalPath.resolveSibling("$fileName$fileType"))
    }

    fun getBitmap(sourceFile: File): Bitmap?{
        val bitmap: Bitmap? = null
        try {
            BitmapFactory.decodeFile(sourceFile.absolutePath)
        }catch (e: Exception){
            Log.e(MyApplication.tagForTesting,"in getBitmap, Filehandler: ${e.toString()}")
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
            val thumbnailHeight: Int = Math.round(thumbnailWidth / aspectRatio)
            Bitmap.createScaledBitmap(bitmap, thumbnailWidth, thumbnailHeight, false)
        } else null
    }
}