package com.mirage.todolist.view.lockscreen

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.TodolistModel
import com.mirage.todolist.model.tasks.getTodolistModel
import com.mirage.todolist.view.settings.PasswordValidator
import com.mirage.todolist.view.settings.SettingsKeys
import com.mirage.todolist.view.settings.showToast
import com.mirage.todolist.view.todolist.TodolistActivity
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

private const val TAP_HINT_TEXT_ANIMATION_DURATION = 4000L

class LockScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val coroutineScope = lifecycleScope
    private lateinit var executor: ExecutorService
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    //TODO inject
    private val todolistModel: TodolistModel = getTodolistModel()

    private val todolistResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        executor = Executors.newSingleThreadExecutor()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        processProtectionPreference()
        processThemePreference()
        processNotificationPreference()
        todolistModel.init(applicationContext)
    }

    private fun initializeSplashScreen() {
        setContentView(R.layout.lockscreen_splash)
        coroutineScope.launch(Dispatchers.Main) {
            delay(1000)
            openTodolist()
        }
    }

    private fun initializeTapToUnlockScreen() {
        setContentView(R.layout.lockscreen_tap)
        val rootLayout: ConstraintLayout = findViewById(R.id.lockscreen_tap_root)
        rootLayout.setOnClickListener {
            openTodolist()
        }
        val hintText: TextView = findViewById(R.id.tap_unlock_hint)
        val startTime = System.currentTimeMillis()
        coroutineScope.launch {
            while (true) {
                val time = System.currentTimeMillis() - startTime
                val progress = (time % TAP_HINT_TEXT_ANIMATION_DURATION).toFloat() / TAP_HINT_TEXT_ANIMATION_DURATION.toFloat()
                val loopedProgress = abs(0.5f - progress) * 2f
                hintText.alpha = loopedProgress
                delay(40L)
            }
        }
    }

    private fun initializeGraphicalKeyScreen() {
        setContentView(R.layout.lockscreen_graphical_key)
        val patternLock: PatternLockView = findViewById(R.id.graphical_key_pattern_lock)
        patternLock.addPatternLockListener(object : PatternLockViewListener {

            override fun onStarted() { }

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) { }

            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                val patternString = PatternLockUtils.patternToString(patternLock, pattern)
                val patternHash = PasswordValidator.getSHA256(patternString)
                val validHash = sharedPreferences.getString(SettingsKeys.PROTECTION_GRAPHICAL_HASH_KEY, "")
                if (patternHash == validHash) {
                    openTodolist()
                }
                else {
                    patternLock.clearPattern()
                }
            }

            override fun onCleared() { }
        })
    }

    private fun initializePasswordScreen() {
        setContentView(R.layout.lockscreen_password)
        val passwordInput: EditText = findViewById(R.id.password_input)
        passwordInput.setOnKeyListener { view, keyCode, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                confirmPassword(passwordInput)
                true
            }
            else false
        }
        val passwordInputBtn: Button = findViewById(R.id.password_input_btn)
        passwordInputBtn.setOnClickListener {
            confirmPassword(passwordInput)
        }
    }

    private fun initializeFingerprintScreen() {
        // If fingerprint was deleted from device, don't require it
        val fingerprintManager = FingerprintManagerCompat.from(this)
        if (!fingerprintManager.isHardwareDetected || !fingerprintManager.hasEnrolledFingerprints()) {
            initializeSplashScreen()
            sharedPreferences.edit()
                .putString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_KEY)
                .apply()
            return
        }
        setContentView(R.layout.lockscreen_fingerprint)
        val rootLayout: ConstraintLayout = findViewById(R.id.fingerprint_root)
        rootLayout.setOnClickListener {
            requireFingerprint()
        }
        val hintText: TextView = findViewById(R.id.fingerprint_subtitle)
        val startTime = System.currentTimeMillis()
        coroutineScope.launch {
            while (true) {
                val time = System.currentTimeMillis() - startTime
                val progress = (time % TAP_HINT_TEXT_ANIMATION_DURATION).toFloat() / TAP_HINT_TEXT_ANIMATION_DURATION.toFloat()
                val loopedProgress = abs(0.5f - progress) * 2f
                hintText.alpha = loopedProgress
                delay(40L)
            }
        }
        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                openTodolist()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_HW_NOT_PRESENT, BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                        showToast(R.string.protection_create_fingerprint_not_supported)
                    }
                }
            }
        }
        biometricPrompt = BiometricPrompt(this, executor, callback)
        val title = resources.getString(R.string.lockscreen_fingerprint_dialog_title)
        val description = resources.getString(R.string.lockscreen_fingerprint_dialog_description)
        val cancel = resources.getString(R.string.lockscreen_fingerprint_dialog_cancel)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(cancel)
            .build()
        requireFingerprint()
    }

    private fun requireFingerprint() {
        biometricPrompt.authenticate(promptInfo)
    }

    private fun confirmPassword(passwordInput: EditText) {
        val input = passwordInput.text.toString()
        val inputHash = PasswordValidator.getSHA256(input)
        val validHash = sharedPreferences.getString(SettingsKeys.PROTECTION_PASSWORD_HASH_KEY, "")
        if (inputHash == validHash) {
            openTodolist()
        }
        else {
            Toast.makeText(this, R.string.lockscreen_password_wrong, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTodolist() {
        val intent = Intent(this, TodolistActivity::class.java)
        todolistResultLauncher.launch(intent)
    }

    private fun processProtectionPreference() {
        when (sharedPreferences.getString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_KEY)) {
            SettingsKeys.PROTECTION_NONE_KEY -> {
                sharedPreferences.edit()
                    .putString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_KEY)
                    .apply()
                initializeSplashScreen()
            }
            SettingsKeys.PROTECTION_TAP_KEY -> {
                initializeTapToUnlockScreen()
            }
            SettingsKeys.PROTECTION_GRAPHICAL_KEY -> {
                initializeGraphicalKeyScreen()
            }
            SettingsKeys.PROTECTION_PASSWORD_KEY -> {
                initializePasswordScreen()
            }
            SettingsKeys.PROTECTION_FINGERPRINT_KEY -> {
                initializeFingerprintScreen()
            }
            else -> {
                sharedPreferences.edit()
                    .putString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_KEY)
                    .apply()
                initializeSplashScreen()
            }
        }
    }

    private fun processThemePreference() {
        when (sharedPreferences.getString(SettingsKeys.CHANGE_THEME_KEY, SettingsKeys.THEME_LIGHT_VALUE)) {
            SettingsKeys.THEME_LIGHT_VALUE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences.edit()
                    .putString(SettingsKeys.CHANGE_THEME_KEY, SettingsKeys.THEME_LIGHT_VALUE)
                    .apply()
            }
            SettingsKeys.THEME_DARK_VALUE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            SettingsKeys.THEME_SYSTEM_VALUE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun processNotificationPreference() {
        when (sharedPreferences.getString(SettingsKeys.NOTIFY_ON_DATETIME_KEY, SettingsKeys.NOTIFY_DATETIME_NEVER)) {
            SettingsKeys.NOTIFY_DATETIME_NEVER -> {
                sharedPreferences.edit()
                    .putString(SettingsKeys.NOTIFY_ON_DATETIME_KEY, SettingsKeys.NOTIFY_DATETIME_NEVER)
                    .apply()
            }
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}