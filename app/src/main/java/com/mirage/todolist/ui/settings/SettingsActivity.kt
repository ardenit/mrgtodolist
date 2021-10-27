package com.mirage.todolist.ui.settings

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.common.AccountPicker
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.model.googledrive.GoogleDriveConnectExceptionHandler
import com.mirage.todolist.model.repository.TodoRepository
import com.mirage.todolist.util.showToast
import javax.inject.Inject

enum class SettingsScreen {
    ROOT,
    PROTECTION,
    GRAPHICAL_KEY
}

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var settingsFragment: SettingsFragment
    private var protectionFragment: ProtectionSettingsFragment? = null
    private var addGraphicalKeyFragment: AddGraphicalKeyFragment? = null
    private var settingsScreen = SettingsScreen.ROOT
    private lateinit var preferences: SharedPreferences

    @Inject
    lateinit var todoRepository: TodoRepository

    /**
     * Activity result launcher for Google Drive [UserRecoverableAuthIOException] user intervene screen
     */
    private val gDriveUserInterveneResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onResultFromGDriveUserIntervene(result)
    }

    private var pendingEmail: String? = ""

    private val gDriveConnectExceptionHandler = object : GoogleDriveConnectExceptionHandler {

        override suspend fun onSuccessfulConnect() {
            println("GDRIVE_CONNECT_SUCCESSFUL")
            Toast.makeText(this@SettingsActivity, "OK", Toast.LENGTH_SHORT).show()
            preferences.edit().putString(resources.getString(R.string.key_sync_select_acc), pendingEmail ?: "").apply()
            settingsFragment.updateSummaries()
        }

        override suspend fun onUserRecoverableFailure(ex: UserRecoverableAuthIOException) {
            println("USER_RECOVERABLE")
            gDriveUserInterveneResultLauncher.launch(ex.intent)
        }

        override suspend fun onGoogleAuthFailure(ex: GoogleAuthIOException) {
            println("GOOGLE_AUTH_FAIL")
            println(ex.message)
            Toast.makeText(this@SettingsActivity, "GOOGLE_AUTH_FAILURE SEE LOGS", Toast.LENGTH_SHORT).show()
        }

        override suspend fun onUnspecifiedFailure(ex: Exception) {
            println("UNSPECIFIED_GDRIVE_CONNECT_FAILURE")
            println(ex.message)
            Toast.makeText(this@SettingsActivity, "UNSPECIFIED_GDRIVE_CONNECT_FAILURE SEE LOGS", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Activity result launcher for Google Drive synchronization account picker screen
     */
    private val accPickerResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onResultFromAccPicker(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).appComponent.inject(this)
        setContentView(R.layout.activity_settings)
        settingsFragment = SettingsFragment()
        settingsFragment.onSyncPressed = { configureSync() }
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

    private fun configureSync() {
        val options = AccountPicker.AccountChooserOptions.Builder()
            .setAllowableAccountsTypes(listOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE))
            .setAlwaysShowAccountPicker(true)
            .setTitleOverrideText(resources.getString(R.string.gdrive_acc_picker_title))
            .build()
        val intent = AccountPicker.newChooseAccountIntent(options)
        accPickerResultLauncher.launch(intent)
    }

    private fun onResultFromAccPicker(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val extras = result.data?.extras
            val authAccount = extras?.getString("authAccount")
            pendingEmail = authAccount
            //TODO todoRepository.setGDriveAccountEmail(authAccount, gDriveConnectExceptionHandler)
        }
        else {
            Toast.makeText(this, R.string.gdrive_sync_cancelled_toast, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onResultFromGDriveUserIntervene(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            //TODO todoRepository.setGDriveAccountEmail(todoRepository.getGDriveAccountEmail(), gDriveConnectExceptionHandler)
        }
        else {
            Toast.makeText(this, R.string.gdrive_sync_cancelled_toast, Toast.LENGTH_SHORT).show()
        }
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
        addGraphicalKeyFragment = addGraphicalKeyFragment ?: AddGraphicalKeyFragment()
        addGraphicalKeyFragment!!.setTargetFragment(protectionFragment, 0)
        addGraphicalKeyFragment!!.onPatternConfirm = {
            onBackPressed()
            settingsFragment.updateSummaries()
            showToast(R.string.protection_result_graphical)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, addGraphicalKeyFragment!!)
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
                addGraphicalKeyFragment!!.clear()
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