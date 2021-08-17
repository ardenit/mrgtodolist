package com.mirage.todolist.view.settings

import android.content.SharedPreferences
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

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var changeThemePreference: DropDownPreference
    private lateinit var setProtectionPreference: Preference
    private lateinit var notifyOnSyncPreference: SwitchPreference
    private lateinit var notifyOnDatetimePreference: ListPreference
    private lateinit var syncSelectAccPreference: Preference

    private lateinit var preferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
        initializePreferences()
        updateSummaries()
    }

    fun updateSummaries() {
        val protectionSummaryRes = when (preferences.getString(SET_PROTECTION_KEY, PROTECTION_NONE_KEY)) {
            PROTECTION_NONE_KEY -> R.string.settings_password_no_protection
            PROTECTION_TAP_KEY -> R.string.settings_password_tap_to_unlock
            PROTECTION_GRAPHICAL_KEY -> R.string.settings_password_graphical_key
            PROTECTION_PASSWORD_KEY -> R.string.settings_password_password
            PROTECTION_FINGERPRINT_KEY -> R.string.settings_password_fingerprint
            else -> R.string.settings_password_no_protection
        }
        setProtectionPreference.setSummary(protectionSummaryRes)
    }

    private fun initializePreferences() {
        preferences = preferenceManager.sharedPreferences
        changeThemePreference = findPreference(CHANGE_THEME_KEY)!!
        setProtectionPreference = findPreference(SET_PROTECTION_KEY)!!
        notifyOnSyncPreference = findPreference(NOTIFY_ON_SYNC_KEY)!!
        notifyOnDatetimePreference = findPreference(NOTIFY_ON_DATETIME_KEY)!!
        syncSelectAccPreference = findPreference(SYNC_SELECT_ACC_KEY)!!
        changeThemePreference.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                THEME_LIGHT_VALUE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                THEME_DARK_VALUE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                THEME_SYSTEM_VALUE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            true
        }
    }
}