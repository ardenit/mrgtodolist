package com.mirage.todolist.view.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.mirage.todolist.R
import com.mirage.todolist.view.settings.SettingsKeys.CHANGE_THEME_KEY
import com.mirage.todolist.view.settings.SettingsKeys.NOTIFY_ON_DATETIME_KEY
import com.mirage.todolist.view.settings.SettingsKeys.NOTIFY_ON_SYNC_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_FINGERPRINT_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_GRAPHICAL_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_NONE_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_PASSWORD_KEY
import com.mirage.todolist.view.settings.SettingsKeys.PROTECTION_TAP_KEY
import com.mirage.todolist.view.settings.SettingsKeys.SET_PROTECTION_KEY
import com.mirage.todolist.view.settings.SettingsKeys.SYNC_SELECT_ACC_KEY
import com.mirage.todolist.view.settings.SettingsKeys.THEME_DARK_VALUE
import com.mirage.todolist.view.settings.SettingsKeys.THEME_LIGHT_VALUE
import com.mirage.todolist.view.settings.SettingsKeys.THEME_SYSTEM_VALUE

class ProtectionSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var noProtectionPreference: Preference
    private lateinit var tapToUnlockPreference: Preference
    private lateinit var graphicalKeyPreference: Preference
    private lateinit var passwordPreference: Preference
    private lateinit var fingerprintPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_protection_screen, rootKey)
        initializePreferences()
    }

    private fun initializePreferences() {
        noProtectionPreference = findPreference(PROTECTION_NONE_KEY)!!
        tapToUnlockPreference = findPreference(PROTECTION_TAP_KEY)!!
        graphicalKeyPreference = findPreference(PROTECTION_GRAPHICAL_KEY)!!
        passwordPreference = findPreference(PROTECTION_PASSWORD_KEY)!!
        fingerprintPreference = findPreference(PROTECTION_FINGERPRINT_KEY)!!
    }
}