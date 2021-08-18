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
                showToast(R.string.protection_result_none)
            }
            SettingsKeys.PROTECTION_TAP_KEY -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                showToast(R.string.protection_result_tap)
            }
            SettingsKeys.PROTECTION_GRAPHICAL_KEY -> {
                openGraphicalKeyFragment()
            }
            SettingsKeys.PROTECTION_PASSWORD_KEY -> {
                onBackPressed()
                settingsFragment.updateSummaries()
                showToast(R.string.protection_result_password)
            }
            SettingsKeys.PROTECTION_FINGERPRINT_KEY -> {
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
            .putString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_FINGERPRINT_KEY)
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