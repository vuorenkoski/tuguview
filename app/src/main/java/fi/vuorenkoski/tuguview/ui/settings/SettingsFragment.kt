package fi.vuorenkoski.tuguview.ui.settings

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import fi.vuorenkoski.tuguview.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        // Find the password preference by its key.
        // The key "password_text" must match the app:key in your XML.
        val passwordPreference: EditTextPreference? = findPreference("password_text")

        // Set a listener that fires just before the dialog's EditText is shown.
        passwordPreference?.setOnBindEditTextListener { editText ->
            // This ensures the input type is set to a password type,
            // which will mask the text with dots (●●●●●●).
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

}
