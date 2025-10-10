package fi.vuorenkoski.tuguview.ui.sensors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fi.vuorenkoski.tuguview.GraphQLConnector
import fi.vuorenkoski.tuguview.Sensor
import fi.vuorenkoski.tuguview.SensorAdapter
import fi.vuorenkoski.tuguview.databinding.FragmentSensorsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.forEach

class SensorsFragment : Fragment() {

    private var _binding: FragmentSensorsBinding? = null

    private val sensorList: MutableList<Sensor> = mutableListOf() // Initialize here or in onCreate
    private lateinit var sensorAdapter: SensorAdapter // Initialize in onCreate

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        fetchSensorData()
    }

    private fun setupRecyclerView() {
        // Initialize with an empty list. The adapter will be updated later.
        sensorAdapter = SensorAdapter(requireContext(), ArrayList())
        binding.sensorList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sensorAdapter
            setHasFixedSize(true)
        }
    }

    private fun fetchSensorData() {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val backend = sharedPreferences.getString("backend_text", "") ?: ""
        val username = sharedPreferences.getString("name_text", "") ?: ""
        val password = sharedPreferences.getString("password_text", "") ?: ""

        // Check if settings are configured before trying to fetch data
        if (backend.isBlank() || username.isBlank()) {
            Log.i("MainActivity", "getting credentials blank?")
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Backend or Username not set",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        // Perform network operations on a background thread
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val connector = GraphQLConnector()
                val loginSuccessful = withContext(Dispatchers.IO) { // Switch to IO thread
                    connector.login(username, password, "https://$backend/api/graphql")
                    true // Placeholder for actual login success check
                }

                if (loginSuccessful) {
                    val sensors: List<Sensor> = withContext(Dispatchers.IO) { // Switch to IO thread
                        connector.fetchSensors()
                    }
                    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT)
                    sensors.forEach { sensor ->
                        // Be careful with potential nulls here if your data can have them
                        val sensorName = sensor.sensorFullname ?: "Unknown Sensor"
                        val sensorValue = sensor.lastValue ?: "N/A"
                        val sensorDate = sensor.date?.let { sdf.format(it) } ?: "No Date"
                        Log.i(
                            "MainActivity",
                            "Sensor: $sensorName, Value: $sensorValue, Date: $sensorDate"
                        )
                    }
                    // Update your RecyclerView Adapter on the Main thread
                    withContext(Dispatchers.Main) {
                        sensorAdapter.updateData(sensors)
                    }
                } else {
                    Log.e("MainActivity", "Login failed")
                    Snackbar.make(binding.root, "login failed", Snackbar.LENGTH_LONG)
                        .setAction(
                            "Action",
                            null
                        ) // You can remove this if you don't need an action
                        .show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to get data", e) // Log the original exception
                Snackbar.make(binding.root, "Failed to get data: " + e.message, 10000)
                    .setAction("Action", null) // You can remove this if you don't need an action
                    .show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}