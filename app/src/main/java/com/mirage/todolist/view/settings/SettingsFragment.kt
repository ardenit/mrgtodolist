package com.mirage.todolist.view.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.mirage.todolist.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
    }
}