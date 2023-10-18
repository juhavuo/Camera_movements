package fi.julavu.cameramovements

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.io.File

class ImageManipulator(val context: Context) {

    private var handler: Handler
    private var handlerThread: HandlerThread = HandlerThread("imagemanipulatiopthread")
    private var fileHandler: FileHandler
    private var amountOfFiles = 0
    private var fileNumber = 0
    private var files: Array<File>?

    init {
        handlerThread.start()
        handler = Handler(
            handlerThread.looper
        )
        fileHandler = FileHandler(context, handler)
        files = fileHandler.getTemporaryPhotoFiles()
        if(files != null) {
            for (file in files!!) {
                Log.i(MyApplication.tagForTesting,"filename: ${file.name} filesize: ${file.freeSpace}")
            }
        }
        stopBackgroundThread()
    }


    private fun stopBackgroundThread() {
        handlerThread.quitSafely()
        handlerThread.join()
    }


}