/**
 * Main activity. One can go to RecordingActivity where one can start taking photos. One can go to
 * to ImageGalleryActivity, where one can view end results. Settings for taking photos are accessed
 * from Recording activity. If there would be app level settings it could be added to MainActivity.
 *
 * Juha Vuokko
 */

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