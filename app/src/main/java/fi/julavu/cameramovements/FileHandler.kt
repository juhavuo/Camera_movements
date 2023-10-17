package fi.julavu.cameramovements

import android.content.Context
import android.media.Image
import android.os.Environment
import android.os.Handler
import java.io.File

class FileHandler(val context: Context, val handler: Handler) {

    private val usedDirectory = Environment.DIRECTORY_PICTURES
    private val albumNameForVideos = "cameramovementsrawvideos"
    private val nameOfInternalFolder = "temporaryphotos"
    private val fileNumber = 0

    private fun createFolderForTemporaryPhotos(){
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

    fun getAmountOfFilesInTemporaryPhotos(): Int{
        val files = getTemporaryPhotoFiles()
        return files?.size ?: 0
    }

    fun getFileForImage(): File {
        return File(context.getExternalFilesDir(usedDirectory), albumNameForVideos)
    }

    fun saveImageToTemporaryStorage(image: Image){
        createFolderForTemporaryPhotos()

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