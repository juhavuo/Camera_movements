package fi.julavu.cameramovements

import android.content.Context
import android.os.Environment
import java.io.File

class FileHandler(val context: Context) {

    private val usedDirectory = Environment.DIRECTORY_PICTURES
    private val albumNameForVideos = "cameramovementsrawvideos"

    fun getFileForVideo(): File{
        val file = File(context.getExternalFilesDir(usedDirectory), albumNameForVideos)
        return file
    }
}