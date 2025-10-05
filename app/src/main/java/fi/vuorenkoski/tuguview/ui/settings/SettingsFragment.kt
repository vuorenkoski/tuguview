package fi.vuorenkoski.tuguview.ui.settings

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fi.vuorenkoski.tuguview.R
import fi.vuorenkoski.tuguview.BuildConfig

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

        // Find the preference by its key
        val versionNamePreference: Preference? = findPreference("version_name")
        val versionCodePreference: Preference? = findPreference("version_code")
        val versionBuildtypePreference: Preference? = findPreference("version_buildtype")

        // 1. Get version name and code from BuildConfig
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val buildType = BuildConfig.BUILD_TYPE

        // 2. Format the string to display
        // Example for debug: "0.2-debug (1) [debug]"
        // Example for release: "0.2 (1) [release]"
        val versionText = "$versionName ($versionCode) [$buildType]"

        // 3. Set the summary of the preference
        versionNamePreference?.summary = versionName
        versionCodePreference?.summary = versionCode.toString()
        versionBuildtypePreference?.summary = buildType
    }

}
