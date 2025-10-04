package fi.vuorenkoski.tuguview.ui.switches

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fi.vuorenkoski.tuguview.GraphQLConnector
import fi.vuorenkoski.tuguview.Switch
import fi.vuorenkoski.tuguview.SwitchAdapter
import fi.vuorenkoski.tuguview.databinding.FragmentSwitchesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.forEach

class SwitchesFragment : Fragment() {

    private var _binding: FragmentSwitchesBinding? = null
    private val switchList: MutableList<Switch> = mutableListOf() // Initialize here or in onCreate
    private lateinit var switchAdapter: SwitchAdapter // Initialize in onCreate

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val switchesViewModel =
            ViewModelProvider(this).get(SwitchesViewModel::class.java)

        _binding = FragmentSwitchesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // 2. Retrieve the saved values using the keys from your root_preferences.xml
        //    Provide a default value (e.g., an empty string) in case nothing is saved yet.
        val backend = sharedPreferences.getString("backend_text", "") ?: ""
        val username = sharedPreferences.getString("name_text", "") ?: ""
        val password = sharedPreferences.getString("password_text", "") ?: ""

        // --- END OF CHANGES ---

        // Check if settings are configured before trying to fetch data
        if (backend.isBlank() || username.isBlank()) {
            Snackbar.make(binding.root, "Backend or Username not set in settings", Snackbar.LENGTH_LONG).show()
            // Don't proceed to fetch data if settings are missing
            return root
        }
        val recyclerView: RecyclerView = binding.switchList // Or use binding.sensorList if defined in XML
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.setHasFixedSize(true)
        switchAdapter = SwitchAdapter(requireContext(), switchList)
        recyclerView.adapter = switchAdapter

        // Perform network operations on a background thread
        lifecycleScope.launch {
            try {
                val connector = GraphQLConnector()
                val loginSuccessful = withContext(Dispatchers.IO) { // Switch to IO thread
                    connector.login(username, password, "https://$backend/api/graphql")
                    true // Placeholder for actual login success check
                }

                if (loginSuccessful) {
                    val switches: List<Switch> = withContext(Dispatchers.IO) { // Switch to IO thread
                        connector.fetchSwitches()
                    }
                    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT)
                    switches.forEach { switch ->
                        // Be careful with potential nulls here if your data can have them
                        val switchName = switch.description ?: "Unknown Sensor"
                        val switchValue = switch.on ?: "N/A"
                        val switchDate = switch.date?.let { sdf.format(it) } ?: "No Date"
                        Log.i("MainActivity", "Switch: $switchName, Value: $switchValue, Date: $switchDate")
                    }
                    // Update your RecyclerView Adapter on the Main thread
                    withContext(Dispatchers.Main) {
                        switchAdapter.updateData(switches)
                    }
                } else {
                    Log.e("MainActivity", "Login failed")
                    Snackbar.make(binding.root, "login failed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null) // You can remove this if you don't need an action
                        .show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to get data", e) // Log the original exception
                Snackbar.make(binding.root, "Failed to get data: " + e.message, 10000)
                    .setAction("Action", null) // You can remove this if you don't need an action
                    .show()
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}