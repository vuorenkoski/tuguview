package fi.vuorenkoski.tuguview.ui.switches

import android.util.Log
import androidx.lifecycle.ViewModel
import fi.vuorenkoski.tuguview.GraphQLConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SwitchesViewModel : ViewModel() {
    // Make this a suspend function
    suspend fun toggleSwitch(connector: GraphQLConnector, switchId: String, isChecked: Boolean) {
        withContext(Dispatchers.IO) { // Ensure it runs on a background thread
            try {
                connector.setSwitch(switchId, isChecked)
            } catch (e: Exception) {
                Log.e("SwitchesViewModel", "Failed to toggle switch", e)
                // Optionally re-throw or handle with LiveData
            }
        }
    }
}

