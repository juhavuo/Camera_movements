package fi.julavu.cameramovements

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SettingsAdapter(private var settingsDataList: ArrayList<SettingsData>, val context: Context): RecyclerView.Adapter<SettingsAdapter.SettingsHolder>() {

    private var seekbarProgress = 0

    inner class SettingsHolder(v: View): RecyclerView.ViewHolder(v){
        val title = v.findViewById<TextView>(R.id.row_recyclerview_title)
        val seekBar = v.findViewById<SeekBar>(R.id.row_recyclerview_seekbar)
        val minValueTextView = v.findViewById<TextView>(R.id.row_recyclerview_timespan_seekbar_min_text_view)
        val maxValueTextView = v.findViewById<TextView>(R.id.row_recyclerview_timespan_seekbar_max_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsHolder{
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.recycleview_row_settings,parent,false)
        return SettingsHolder(inflatedView)
    }

    override fun getItemCount(): Int {
       return settingsDataList.size
    }

    override fun onBindViewHolder(holder: SettingsHolder, position: Int) {
        holder.title.text = settingsDataList[position].title
        holder.minValueTextView.text = settingsDataList[position].sliderMin.toString()
        holder.maxValueTextView.text = settingsDataList[position].sliderMax.toString()
        holder.seekBar.min = settingsDataList[position].sliderMin
        holder.seekBar.max = settingsDataList[position].sliderMax
        seekbarProgress = settingsDataList[position].startValue
        holder.seekBar.progress = seekbarProgress
        holder.seekBar.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekbarProgress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
    }

    fun getSeekbarProgress(): Int = seekbarProgress
}