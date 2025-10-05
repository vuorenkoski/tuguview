package fi.vuorenkoski.tuguview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat // If you format dates here
import java.util.Locale // If you format dates here

class SensorAdapter(
    private val context: Context, // Changed to non-null
    private var mData: MutableList<Sensor> // Changed to MutableList for easier updates
) : RecyclerView.Adapter<SensorAdapter.ViewHolder>() { // ViewHolder non-null

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null

    // data is passed into the constructor (Handled by primary constructor)

    override fun onCreateViewHolder(
        parent: ViewGroup, // Changed to non-null
        viewType: Int
    ): ViewHolder { // Return type ViewHolder non-null
        val view: View = mInflater.inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, // ViewHolder non-null
        position: Int
    ) {
        val sensor = mData[position] // Use Kotlin index access

        holder.sensorNameView.text = sensor.sensorFullname ?: "N/A"

        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        holder.sensorDateView.text = sensor.date?.let { dateValue ->
                sdf.format(dateValue)
        } ?: "No Date"

        var preSign = ""
        if (sensor.lastValue>0) { preSign="+" }

        holder.sensorValueView.text = preSign + (sensor.lastValue?.toString() ?: "-") + " " + (sensor.sensorUnit ?: "")
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    // Method to update the data in the adapter
    fun updateData(newData: List<Sensor>) {
        mData.clear()
        mData.addAll(newData)
        notifyDataSetChanged() // Or use DiffUtil for better performance
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        // Assume these IDs are correct and the views are always present
        val sensorNameView: TextView = itemView.findViewById(R.id.sensor_name)
        val sensorDateView: TextView = itemView.findViewById(R.id.sensor_date)
        val sensorValueView: TextView = itemView.findViewById(R.id.sensor_value)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            // Check for NO_POSITION before accessing the item.
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                mClickListener?.onItemClick(view, bindingAdapterPosition)
            }
        }
    }

    // Changed to return the actual Sensor object
    fun getItem(position: Int): Sensor? {
        return if (position >= 0 && position < mData.size) {
            mData[position]
        } else {
            null
        }
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}
