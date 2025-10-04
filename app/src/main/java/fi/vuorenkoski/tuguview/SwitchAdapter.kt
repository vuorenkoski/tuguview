package fi.vuorenkoski.tuguview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat // If you format dates here
import java.util.Locale // If you format dates here


class SwitchAdapter(
    private val context: Context, // Changed to non-null
    private var mData: MutableList<Switch> // Changed to MutableList for easier updates
) : RecyclerView.Adapter<SwitchAdapter.ViewHolder>() { // ViewHolder non-null

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null

    // data is passed into the constructor (Handled by primary constructor)

    override fun onCreateViewHolder(
        parent: ViewGroup, // Changed to non-null
        viewType: Int
    ): ViewHolder { // Return type ViewHolder non-null
        val view: View = mInflater.inflate(R.layout.switch_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder, // ViewHolder non-null
        position: Int
    ) {
        val switch = mData[position] // Use Kotlin index access

        // It's good practice to handle potential nulls from your data source
        holder.switchNameView.text = switch.description ?: "N/A"

        // Example: If sensor.date is a java.util.Date and needs formatting
        // val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        // holder.sensorDateView.text = sensor.date?.let { dateFormat.format(it) } ?: "No Date"
        // If sensor.date is already a formatted String:
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        holder.switchDateView.text = switch.date?.let { dateValue ->
            // Ensure 'dateValue' is indeed a Date if sensor.date's type is more general like Any?
            if (dateValue is java.util.Date) {
                sdf.format(dateValue)
            } else {
                null // Or "Invalid Date Type" directly if you don't want "No Date"
            }
        } ?: "No Date"

        holder.switchValueView.text = switch.on
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    // Method to update the data in the adapter
    fun updateData(newData: List<Switch>) {
        mData.clear()
        mData.addAll(newData)
        notifyDataSetChanged() // Or use DiffUtil for better performance
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        // Assume these IDs are correct and the views are always present
        val switchNameView: TextView = itemView.findViewById(R.id.switch_name)
        val switchDateView: TextView = itemView.findViewById(R.id.switch_date)
        val switchValueView: TextView = itemView.findViewById(R.id.switch_value)

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
    fun getItem(position: Int): Switch? {
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
