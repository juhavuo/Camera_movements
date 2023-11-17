package fi.julavu.cameramovements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.io.File

class ImageDialogFragment: DialogFragment() {

    private val fileName: String
        get() = requireArguments().getString(FILE_NAME) ?: ""

    companion object{
        private const val FILE_NAME = "file_name"
        fun newInstance(fName: String) = ImageDialogFragment().apply {
            arguments = bundleOf(
                FILE_NAME to fName
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_view,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView = view.findViewById<ImageView>(R.id.fragment_image_image_view)
        val context = requireContext()
        val fileHandler = FileHandler(context)
        Thread{
            val imageFile = File(fileHandler.getExternalStoragePath(FileHandler.externalImageFolderName),fileName)
            val bitmap = fileHandler.getBitmap(imageFile)
            activity?.runOnUiThread {
                imageView.setImageBitmap(bitmap)
            }
        }.start()
        val closeButton = view.findViewById<ImageButton>(R.id.fragment_image_close_button)
        closeButton.setOnClickListener {
            dismiss()
        }
    }
}