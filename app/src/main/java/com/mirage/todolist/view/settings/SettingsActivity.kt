package com.mirage.todolist.view.settings

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.mirage.todolist.R

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        settingsFragment = SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, settingsFragment)
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        (fragment as? ProtectionSettingsFragment)?.onOptionSelected = ::onProtectionOptionSelected
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        if (pref.key == SettingsKeys.SET_PROTECTION_KEY) {
            supportActionBar?.setTitle(R.string.protection_action_bar_title)
        }
        return true
    }

    private fun onProtectionOptionSelected(key: String?) {
        when (key) {
            SettingsKeys.PROTECTION_NONE_KEY -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                Toast.makeText(this, R.string.protection_result_none, Toast.LENGTH_SHORT).show()
            }
            SettingsKeys.PROTECTION_TAP_KEY -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                Toast.makeText(this, R.string.protection_result_tap, Toast.LENGTH_SHORT).show()
            }
            SettingsKeys.PROTECTION_PASSWORD_KEY -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                Toast.makeText(this, R.string.protection_result_password, Toast.LENGTH_SHORT).show()
            }
            else -> {

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        supportActionBar?.setTitle(R.string.settings_activity_title)
        super.onBackPressed()
    }
}