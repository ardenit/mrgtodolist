package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mirage.todolist.R
import com.mirage.todolist.databinding.FragmentLockscreenFingerprintBinding
import com.mirage.todolist.util.autoCleared
import com.mirage.todolist.util.showToast
import com.mirage.todolist.util.startHintTextColorAnimation
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class FingerprintFragment : Fragment(R.layout.fragment_lockscreen_fingerprint) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    private var binding by autoCleared<FragmentLockscreenFingerprintBinding>()

    private lateinit var executor: ExecutorService
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_lockscreen_password,
            container,
            false
        )
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        executor = Executors.newSingleThreadExecutor()
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
        processFingerprint()
        super.onViewCreated(view, savedInstanceState)
    }

    fun processFingerprint() {
        biometricPrompt.authenticate(promptInfo)
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}