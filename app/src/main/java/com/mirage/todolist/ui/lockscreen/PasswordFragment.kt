package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.mirage.todolist.R
import com.mirage.todolist.databinding.FragmentLockscreenPasswordBinding
import com.mirage.todolist.di.App
import com.mirage.todolist.util.autoCleared
import javax.inject.Inject

class PasswordFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    private var binding by autoCleared<FragmentLockscreenPasswordBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as App).appComponent.inject(this)
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
        val passwordInput: EditText = view.findViewById(R.id.password_input)
        passwordInput.setOnKeyListener { _, keyCode, keyEvent ->
            onPasswordKeyInput(keyCode, keyEvent)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    fun onPasswordButtonClick(): Boolean {
        confirmPassword(binding.passwordInput.text.toString())
        return true
    }

    private fun onPasswordKeyInput(keyCode: Int, keyEvent: KeyEvent): Boolean =
        if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            confirmPassword(binding.passwordInput.text.toString())
            true
        }
        else false

    private fun confirmPassword(input: String) {
        if (viewModel.tryPassword(input)) {
            viewModel.unlockTodolist()
        }
        else {
            Toast.makeText(requireActivity(), R.string.lockscreen_password_wrong, Toast.LENGTH_SHORT).show()
        }
    }
}