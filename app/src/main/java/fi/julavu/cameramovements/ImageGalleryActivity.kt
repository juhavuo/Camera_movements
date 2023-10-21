package fi.julavu.cameramovements

import android.os.Bundle
import androidx.activity.ComponentActivity
import java.io.File

class ImageGalleryActivity : ComponentActivity() {

    private lateinit var fileHandler: FileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)

        fileHandler = FileHandler(this)




        val files: Array<File>? = fileHandler.getImageFilesFromExternalStorage()
        /*
        if(files != null){

        }*/
    }
}

