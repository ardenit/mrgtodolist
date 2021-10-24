package com.mirage.todolist.view.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.mirage.todolist.R
import com.mirage.todolist.ui.PasswordValidator

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
            getString(R.string.key_protection_none) -> {
                preferences.edit()
                    .putString(getString(R.string.key_set_protection), getString(R.string.value_protection_none))
                    .apply()
                onOptionSelected?.invoke(getString(R.string.key_protection_none))
            }
            getString(R.string.key_protection_tap) -> {
                preferences.edit()
                    .putString(getString(R.string.key_set_protection), getString(R.string.value_protection_tap))
                    .apply()
                onOptionSelected?.invoke(getString(R.string.key_protection_tap))
            }
            getString(R.string.key_protection_graphical) -> {
                onOptionSelected?.invoke(getString(R.string.key_protection_graphical))
            }
            getString(R.string.key_protection_fingerprint) -> {
                onOptionSelected?.invoke(getString(R.string.key_protection_fingerprint))
            }
            else -> {
                return super.onPreferenceTreeClick(preference)
            }
        }
        return true
    }

    private fun initializePreferences() {
        preferences = preferenceManager.sharedPreferences
        noProtectionPreference = findPreference(getString(R.string.key_protection_none))!!
        tapToUnlockPreference = findPreference(getString(R.string.key_protection_tap))!!
        graphicalKeyPreference = findPreference(getString(R.string.key_protection_graphical))!!
        passwordPreference = findPreference(getString(R.string.key_protection_password))!!
        fingerprintPreference = findPreference(getString(R.string.key_protection_fingerprint))!!
        passwordPreference.setOnBindEditTextListener { editText ->
            editText.setText(R.string.protection_password_empty)
            preferences.edit()
                .putString(getString(R.string.key_protection_password), "")
                .apply()
        }
        passwordPreference.setOnPreferenceChangeListener { _, newValue ->
            val newPassword = newValue.toString()
            if (PasswordValidator.isPasswordValid(newPassword)) {
                preferences.edit()
                    .putString(getString(R.string.key_set_protection), getString(R.string.value_protection_password))
                    .putString(getString(R.string.key_protection_password), "")
                    .putString(getString(R.string.key_password_hash), PasswordValidator.getSHA256(newPassword))
                    .apply()
                onOptionSelected?.invoke(getString(R.string.key_protection_password))
            }
            else {
                Toast.makeText(context, R.string.protection_password_invalid, Toast.LENGTH_SHORT).show()
            }
            true
        }
    }
}