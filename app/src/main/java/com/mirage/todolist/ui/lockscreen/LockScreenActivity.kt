package com.mirage.todolist.ui.lockscreen

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.view.settings.showToast
import com.mirage.todolist.view.todolist.TodolistActivity
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs


class LockScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val coroutineScope = lifecycleScope
    private lateinit var executor: ExecutorService
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val viewModel: LockScreenViewModel by viewModels { viewModelFactory }
    private lateinit var contentFragment: Fragment

    private val todolistResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).appComponent.inject(this)
        executor = Executors.newSingleThreadExecutor()
        viewModel.lockScreenType.observe(this) {
            when (it) {
                LockScreenType.NO_PROTECTION -> initializeSplashScreen()
                LockScreenType.TAP_TO_UNLOCK -> initializeTapToUnlockScreen()
                LockScreenType.GRAPHICAL_KEY -> initializeGraphicalKeyScreen()
                LockScreenType.PASSWORD -> initializePasswordScreen()
                LockScreenType.FINGERPRINT -> initializeFingerprintScreen()
                else -> initializeSplashScreen()
            }
        }
    }

    private fun initializeSplashScreen() {
        supportFragmentManager.beginTransaction()
            .replace()
        coroutineScope.launch(Dispatchers.Main) {
            delay(1000)
            openTodolist()
        }
    }

    private fun initializeTapToUnlockScreen() {
        setContentView(R.layout.fragment_lockscreen_tap)
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
        setContentView(R.layout.fragment_lockscreen_graphical_key)
        val patternLock: PatternLockView = findViewById(R.id.graphical_key_pattern_lock)
        patternLock.addPatternLockListener(object : PatternLockViewListener {

            override fun onStarted() { }

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) { }

            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                val patternString = PatternLockUtils.patternToString(patternLock, pattern)
                if (viewModel.tryPattern(patternString)) {
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
        setContentView(R.layout.fragment_lockscreen_password)
        val passwordInput: EditText = findViewById(R.id.password_input)
        passwordInput.setOnKeyListener { view, keyCode, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                confirmPassword(passwordInput.text.toString())
                true
            }
            else false
        }
        val passwordInputBtn: Button = findViewById(R.id.password_input_btn)
        passwordInputBtn.setOnClickListener {
            confirmPassword(passwordInput.text.toString())
        }
    }

    private fun confirmPassword(input: String) {
        if (viewModel.tryPassword(input)) {
            openTodolist()
        }
        else {
            Toast.makeText(this, R.string.lockscreen_password_wrong, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeFingerprintScreen() {
        // If fingerprint was deleted from device, don't require it
        val fingerprintManager = FingerprintManagerCompat.from(this)
        if (!fingerprintManager.isHardwareDetected || !fingerprintManager.hasEnrolledFingerprints()) {
            initializeSplashScreen()
            return
        }
        setContentView(R.layout.fragment_lockscreen_fingerprint)
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

    private fun openTodolist() {
        val intent = Intent(this, TodolistActivity::class.java)
        todolistResultLauncher.launch(intent)
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val TAP_HINT_TEXT_ANIMATION_DURATION = 4000L
    }
}