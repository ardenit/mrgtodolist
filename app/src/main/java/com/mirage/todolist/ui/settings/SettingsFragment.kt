package com.mirage.todolist.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.model.repository.TodoRepository
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var changeThemePreference: DropDownPreference
    private lateinit var setProtectionPreference: Preference
    private lateinit var notifyOnDatetimePreference: ListPreference
    private lateinit var syncSelectAccPreference: Preference

    private lateinit var preferences: SharedPreferences

    @Inject
    lateinit var todoRepository: TodoRepository

    var onSyncPressed: () -> Unit = {}

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        (requireActivity().application as App).appComponent.inject(this)
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
        initializePreferences()
        updateSummaries()
        setSyncConfiguredSummary()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tasklist_background))
    }

    fun updateSummaries() {
        val protectionSummaryRes = when (preferences.getString(getString(R.string.key_set_protection), getString(R.string.value_protection_none))) {
            getString(R.string.value_protection_none) -> R.string.settings_password_no_protection
            getString(R.string.value_protection_tap) -> R.string.settings_password_tap_to_unlock
            getString(R.string.value_protection_graphical) -> R.string.settings_password_graphical_key
            getString(R.string.value_protection_password) -> R.string.settings_password_password
            getString(R.string.value_protection_fingerprint) -> R.string.settings_password_fingerprint
            else -> R.string.settings_password_no_protection
        }
        setProtectionPreference.setSummary(protectionSummaryRes)
    }

    fun setSyncConnectionSummary(pendingEmail: String) {
        val summary = resources.getString(R.string.settings_sync_with_drive_connecting, pendingEmail)
        syncSelectAccPreference.summary = summary
    }

    fun setSyncConfiguredSummary() {
        val currentEmail = preferences.getString(resources.getString(R.string.key_sync_select_acc), "")
        if (currentEmail.isNullOrBlank()) {
            syncSelectAccPreference.setSummary(R.string.settings_sync_status_no_sync)
        } else {
            syncSelectAccPreference.summary = currentEmail
        }
    }

    private fun initializePreferences() {
        preferences = preferenceManager.sharedPreferences
        changeThemePreference = findPreference(getString(R.string.key_change_theme))!!
        setProtectionPreference = findPreference(getString(R.string.key_set_protection))!!
        notifyOnDatetimePreference = findPreference(getString(R.string.key_notify_on_datetime))!!
        syncSelectAccPreference = findPreference(getString(R.string.key_sync_select_acc))!!
        changeThemePreference.setOnPreferenceChangeListener { _, newValue ->
            when (newValue) {
                getString(R.string.value_theme_light) -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                getString(R.string.value_theme_dark) -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                getString(R.string.value_theme_system) -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            true
        }
        syncSelectAccPreference.setOnPreferenceClickListener {
            onSyncPressed()
            true
        }
    }
}