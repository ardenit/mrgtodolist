package com.mirage.todolist.view.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.mirage.todolist.R

/** Keys used for preference items in res/xml/settings_screen.xml */
private const val CHANGE_THEME_KEY = "change_theme"
private const val SET_PROTECTION_KEY = "set_protection"
private const val NOTIFY_ON_SYNC_KEY = "notify_on_sync"
private const val NOTIFY_ON_DATETIME_KEY = "notify_on_datetime"
private const val SYNC_SELECT_ACC_KEY = "sync_select_acc"

private const val THEME_LIGHT_VALUE = "1"
private const val THEME_DARK_VALUE = "2"
private const val THEME_SYSTEM_VALUE = "3"

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
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                THEME_DARK_VALUE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                THEME_SYSTEM_VALUE -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            true
        }
    }
}