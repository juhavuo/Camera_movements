package fi.julavu.cameramovements

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recordingButton = findViewById<Button>(R.id.main_activity_start_recording_button)
        recordingButton.setOnClickListener {
            val intent = Intent(this,RecordingActivity::class.java)
            startActivity(intent)
        }

        val galleryButton = findViewById<Button>(R.id.main_activity_gallery_button)
        galleryButton.setOnClickListener {
            val intent = Intent(this,ImageGalleryActivity::class.java)
            startActivity(intent)
        }

    }


}