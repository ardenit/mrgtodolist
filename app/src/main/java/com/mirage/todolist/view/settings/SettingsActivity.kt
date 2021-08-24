package com.mirage.todolist.view.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.mirage.todolist.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class SettingsScreen {
    ROOT,
    PROTECTION,
    GRAPHICAL_KEY
}

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var settingsFragment: SettingsFragment
    private var protectionFragment: ProtectionSettingsFragment? = null
    private var graphicalKeyFragment: GraphicalKeyFragment? = null
    private var settingsScreen = SettingsScreen.ROOT
    private lateinit var preferences: SharedPreferences

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
        settingsScreen = SettingsScreen.ROOT
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        protectionFragment = fragment as? ProtectionSettingsFragment
        settingsScreen = SettingsScreen.PROTECTION
        protectionFragment?.onOptionSelected = ::onProtectionOptionSelected
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        if (pref.key == resources.getString(R.string.key_set_protection)) {
            supportActionBar?.setTitle(R.string.protection_action_bar_title)
        }
        return true
    }

    private fun onProtectionOptionSelected(key: String?) {
        when (key) {
            resources.getString(R.string.key_protection_none) -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                showToast(R.string.protection_result_none)
            }
            resources.getString(R.string.key_protection_tap) -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                showToast(R.string.protection_result_tap)
            }
            resources.getString(R.string.key_protection_graphical) -> {
                openGraphicalKeyFragment()
            }
            resources.getString(R.string.key_protection_password) -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                showToast(R.string.protection_result_password)
            }
            resources.getString(R.string.key_protection_fingerprint) -> {
                processFingerprintOption()
            }
        }
    }

    private fun processFingerprintOption() {
        val fingerprintManager = FingerprintManagerCompat.from(this)
        if (!fingerprintManager.isHardwareDetected) {
            showToast(R.string.protection_create_fingerprint_not_supported)
            return
        }
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            showToast(R.string.protection_create_fingerprint_none)
            return
        }
        preferences.edit()
            .putString(resources.getString(R.string.key_set_protection), resources.getString(R.string.value_protection_fingerprint))
            .apply()
        onBackPressed()
        settingsFragment.updateSummaries()
        showToast(R.string.protection_result_fingerprint)
    }

    private fun openGraphicalKeyFragment() {
        settingsScreen = SettingsScreen.GRAPHICAL_KEY
        graphicalKeyFragment = graphicalKeyFragment ?: GraphicalKeyFragment()
        graphicalKeyFragment!!.setTargetFragment(protectionFragment, 0)
        graphicalKeyFragment!!.onPatternConfirm = {
            onBackPressed()
            settingsFragment.updateSummaries()
            showToast(R.string.protection_result_graphical)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, graphicalKeyFragment!!)
            .addToBackStack(null)
            .commit()
        supportActionBar?.setTitle(R.string.protection_create_graphical_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when (settingsScreen) {
            SettingsScreen.ROOT -> { }
            SettingsScreen.PROTECTION -> {
                supportActionBar?.setTitle(R.string.settings_activity_title)
                settingsScreen = SettingsScreen.ROOT
            }
            SettingsScreen.GRAPHICAL_KEY -> {
                graphicalKeyFragment!!.clear()
                supportActionBar?.setTitle(R.string.protection_action_bar_title)
                settingsScreen = SettingsScreen.PROTECTION
            }
        }
        super.onBackPressed()
        if (settingsScreen == SettingsScreen.PROTECTION) {
            onBackPressed()
        }
    }
}

fun Context.showToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}