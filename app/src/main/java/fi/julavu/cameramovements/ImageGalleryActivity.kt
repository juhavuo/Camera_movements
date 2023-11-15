package fi.julavu.cameramovements

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ImageGalleryActivity : ComponentActivity() {

    private lateinit var fileHandler: FileHandler
    private val imageInfoList: ArrayList<ImageInfoForGallery> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)

        fileHandler = FileHandler(this)

        val backButton = findViewById<Button>(R.id.gallery_activity_back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.gallery_activity_recycler_view)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        val galleryRecyclerViewAdapter = GalleryRecyclerViewAdapter(imageInfoList)
        recyclerView.adapter = galleryRecyclerViewAdapter

        Thread {
            val files: Array<File>? = fileHandler.getImageFilesFromExternalStorage(FileHandler.externalImageFolderName)
            val fileWidth = 64
            var thumbnail: Bitmap?
            if (files != null) {
                for (i in files.indices) {
                    thumbnail =  fileHandler.getThumbnail(files[i], fileWidth)
                    if(thumbnail != null) {
                        imageInfoList.add(
                            ImageInfoForGallery(
                                files[i].name,
                                thumbnail
                                )
                        )
                    }
                }
            }
            runOnUiThread {
                galleryRecyclerViewAdapter.notifyDataSetChanged()
            }
        }.start()
    }

}

