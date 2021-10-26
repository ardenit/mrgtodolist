package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mirage.todolist.R
import com.mirage.todolist.util.showToast
import com.mirage.todolist.util.startHintTextColorAnimation
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class FingerprintFragment : Fragment(R.layout.fragment_lockscreen_fingerprint) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    private lateinit var executor: ExecutorService
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        executor = Executors.newSingleThreadExecutor()
        val rootLayout: ConstraintLayout = view.findViewById(R.id.fingerprint_root)
        rootLayout.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
        lifecycleScope.startHintTextColorAnimation(view.findViewById(R.id.fingerprint_subtitle))
        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                viewModel.unlockTodolist()
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
        biometricPrompt.authenticate(promptInfo)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}