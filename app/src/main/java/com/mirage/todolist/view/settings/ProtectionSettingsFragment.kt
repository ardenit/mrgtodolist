package com.mirage.todolist.view.settings

import android.content.SharedPreferences
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.mirage.todolist.R
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_FINGERPRINT_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_GRAPHICAL_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_NONE_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_PASSWORD_HASH_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_PASSWORD_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_TAP_KEY
import com.mirage.todolist.view.settings.SettingsKeys.SET_PROTECTION_KEY
import java.util.concurrent.Executors

class ProtectionSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var noProtectionPreference: Preference
    private lateinit var tapToUnlockPreference: Preference
    private lateinit var graphicalKeyPreference: Preference
    private lateinit var passwordPreference: EditTextPreference
    private lateinit var fingerprintPreference: Preference

    private lateinit var preferences: SharedPreferences
    var onOptionSelected: ((String?) -> Unit)? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_protection_screen, rootKey)
        initializePreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tasklist_background))
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            PROTECTION_NONE_KEY -> {
                preferences.edit()
                    .putString(SET_PROTECTION_KEY, PROTECTION_NONE_KEY)
                    .apply()
                onOptionSelected?.invoke(PROTECTION_NONE_KEY)
            }
            PROTECTION_TAP_KEY -> {
                preferences.edit()
                    .putString(SET_PROTECTION_KEY, PROTECTION_TAP_KEY)
                    .apply()
                onOptionSelected?.invoke(PROTECTION_TAP_KEY)
            }
            PROTECTION_GRAPHICAL_KEY -> {
                onOptionSelected?.invoke(PROTECTION_GRAPHICAL_KEY)
            }
            PROTECTION_FINGERPRINT_KEY -> {
                onOptionSelected?.invoke(PROTECTION_FINGERPRINT_KEY)
            }
            else -> {
                return super.onPreferenceTreeClick(preference)
            }
        }
        return true
    }

    private fun initializePreferences() {
        preferences = preferenceManager.sharedPreferences
        noProtectionPreference = findPreference(PROTECTION_NONE_KEY)!!
        tapToUnlockPreference = findPreference(PROTECTION_TAP_KEY)!!
        graphicalKeyPreference = findPreference(PROTECTION_GRAPHICAL_KEY)!!
        passwordPreference = findPreference(PROTECTION_PASSWORD_KEY)!!
        fingerprintPreference = findPreference(PROTECTION_FINGERPRINT_KEY)!!
        passwordPreference.setOnBindEditTextListener { editText ->
            editText.setText(R.string.protection_password_empty)
            preferences.edit()
                .putString(PROTECTION_PASSWORD_KEY, "")
                .apply()
        }
        passwordPreference.setOnPreferenceChangeListener { _, newValue ->
            val newPassword = newValue.toString()
            if (PasswordValidator.isPasswordValid(newPassword)) {
                preferences.edit()
                    .putString(SET_PROTECTION_KEY, PROTECTION_PASSWORD_KEY)
                    .putString(PROTECTION_PASSWORD_KEY, "")
                    .putString(PROTECTION_PASSWORD_HASH_KEY, PasswordValidator.getSHA256(newPassword))
                    .apply()
                onOptionSelected?.invoke(PROTECTION_PASSWORD_KEY)
            }
            else {
                Toast.makeText(context, R.string.protection_password_invalid, Toast.LENGTH_SHORT).show()
            }
            true
        }
    }
}