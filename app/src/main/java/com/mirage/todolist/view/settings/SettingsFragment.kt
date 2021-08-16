package com.mirage.todolist.view.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.mirage.todolist.R
import com.mirage.todolist.view.settings.SettingsKeys.CHANGE_THEME_KEY
import com.mirage.todolist.view.settings.SettingsKeys.NOTIFY_ON_DATETIME_KEY
import com.mirage.todolist.view.settings.SettingsKeys.NOTIFY_ON_SYNC_KEY
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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
        initializePreferences()
    }

    private fun initializePreferences() {
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