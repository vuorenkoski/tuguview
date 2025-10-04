package fi.vuorenkoski.tuguview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat // If you format dates here
import java.util.Locale // If you format dates here
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class SwitchAdapter(
    private val context: Context, // Changed to non-null
    private var mData: MutableList<Switch>, // Changed to MutableList for easier updates
    private var onSwitchToggled: (switchId: String, isChecked: Boolean) -> Unit
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

        holder.switchNameView.text = switch.description ?: "N/A"

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

        Log.e("MainActivity", "switch ")

        val isOn = switch.on.equals("ON", ignoreCase = true)
        // Temporarily disable the listener to prevent it from firing when we set the initial state
        holder.switchToggle.setOnCheckedChangeListener(null)
        holder.switchToggle.isChecked = isOn

        // 2. Re-attach the listener to handle user interaction
        holder.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            // 3. Call the lambda function passed from the Fragment
            onSwitchToggled(switch.id, isChecked)
        }
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
        val switchToggle: SwitchMaterial = itemView.findViewById(R.id.switch_toggle)

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
