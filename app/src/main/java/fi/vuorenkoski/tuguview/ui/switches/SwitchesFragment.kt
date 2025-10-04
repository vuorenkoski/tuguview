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
import kotlinx.coroutines.delay

class SwitchesFragment : Fragment() {

    private var _binding: FragmentSwitchesBinding? = null
    private val binding get() = _binding!!

    private lateinit var switchAdapter: SwitchAdapter
    private lateinit var switchesViewModel: SwitchesViewModel

    // Use a single connector instance for the fragment's lifecycle
    private val connector = GraphQLConnector()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        switchesViewModel = ViewModelProvider(this).get(SwitchesViewModel::class.java)
        _binding = FragmentSwitchesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        loadAndFetchData()

        return root
    }

    private fun setupRecyclerView() {
        // Initialize the adapter directly, passing the toggle logic as a lambda.
        // The compiler knows the lambda must return Unit from the adapter's constructor signature.
        switchAdapter = SwitchAdapter(
            requireContext(),
            mutableListOf()
        ) { switchId: String, isChecked: Boolean ->
            // This is the body of the onSwitchToggled lambda.
            // It correctly returns Unit because it's part of the function call.
            lifecycleScope.launch {
                try {
                    // 1. Call the suspend function to update the switch on the backend.
                    switchesViewModel.toggleSwitch(connector, switchId, isChecked)

                    // 2. After the update is done, re-fetch all switches to get the latest state.
                    var notOk = true
                    val expectedStatus = if (isChecked) "ON" else "OFF"
                    while (notOk) {
                        delay(1000) // Wait for 2 seconds before checking
                        val switches = fetchAndDisplaySwitches()

                        // Find the switch we are interested in
                        val toggledSwitch = switches.find { it.id == switchId }

                        if (toggledSwitch != null) {
                            // Check if the server status matches what we expect
                            if (toggledSwitch.on.equals(expectedStatus, ignoreCase = true)) {
                                Log.d("SwitchesFragment", "Switch status confirmed on server.")
                                notOk = false // Exit the loop
                            } else {
                                Log.d("SwitchesFragment", "Waiting for switch status to update on server...")
                            }
                        } else {
                            // If the switch is not found, something is wrong, exit the loop to avoid an infinite loop.
                            Log.w("SwitchesFragment", "Toggled switch not found in fetch response, exiting poll loop.")
                            notOk = false
                        }
                    }

                } catch (e: Exception) {
                    Log.e("MainActivity", "Error during switch toggle and refresh", e)
                    Snackbar.make(binding.root, "Failed to update switch", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // Assign the layout manager and the newly created adapter to the RecyclerView.
        binding.switchList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = switchAdapter
        }
    }



    private fun loadAndFetchData() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val backend = sharedPreferences.getString("backend_text", "") ?: ""
        val username = sharedPreferences.getString("name_text", "") ?: ""
        val password = sharedPreferences.getString("password_text", "") ?: ""

        if (backend.isBlank() || username.isBlank()) {
            Snackbar.make(binding.root, "Backend or Username not set in settings", Snackbar.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                // First, log in
                val loginSuccessful = withContext(Dispatchers.IO) {
                    connector.login(username, password, "https://$backend/api/graphql")
                    true // Assume login is successful if no exception is thrown
                }

                // If login is successful, fetch the initial list of switches
                if (loginSuccessful) {
                    fetchAndDisplaySwitches()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to login or get initial data", e)
                Snackbar.make(binding.root, "Failed to get data: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // This function now fetches switches and updates the UI
    private suspend fun fetchAndDisplaySwitches(): List<Switch> {
        try {
            val switches: List<Switch> = withContext(Dispatchers.IO) {
                connector.fetchSwitches()
            }
            withContext(Dispatchers.Main) {
                switchAdapter.updateData(switches)
            }
            return switches
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to refresh switches", e)
            // Use withContext(Dispatchers.Main) to safely show Snackbar from a suspend function
            withContext(Dispatchers.Main) {
                Snackbar.make(binding.root, "Failed to refresh switches: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
            return emptyList() // Return an empty list on error
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
