package fi.julavu.cameramovements

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GalleryRecyclerViewAdapter(private val imageInfoList: ArrayList<ImageInfoForGallery>): RecyclerView.Adapter<GalleryRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val title = view.findViewById<TextView>(R.id.gallery_recycler_view_row_title)
        val thumbnail = view.findViewById<ImageButton>(R.id.gallery_recycler_view_row_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_gallery_recycler_view,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = imageInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.title.text = imageInfoList[position].name
        holder.thumbnail.setImageBitmap(imageInfoList[position].thumbnail)
        holder.thumbnail.setOnClickListener {
            Log.i(MyApplication.tagForTesting,"image at $position clicked")
        }

    }
}