package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.mirage.todolist.R
import com.mirage.todolist.util.autoCleared
import javax.inject.Inject

class PasswordFragment : Fragment(R.layout.fragment_lockscreen_password) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    var binding by autoCleared<PasswordFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val passwordInput: EditText = view.findViewById(R.id.password_input)
        passwordInput.setOnKeyListener(::onPasswordKeyInput)
        val passwordInputBtn: Button = view.findViewById(R.id.password_input_btn)
        passwordInputBtn.setOnClickListener(::onPasswordButtonClick) // TODO data binding
        super.onViewCreated(view, savedInstanceState)
    }

    private fun onPasswordKeyInput(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean =
        if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            confirmPassword(passwordInput.text.toString())
            true
        }
        else false

    private fun onPasswordButtonClick(view: View): Boolean {
        confirmPassword(passwordInput.text.toString())
        return true
    }

    private fun confirmPassword(input: String) {
        if (viewModel.tryPassword(input)) {
            viewModel.unlockTodolist()
        }
        else {
            Toast.makeText(requireActivity(), R.string.lockscreen_password_wrong, Toast.LENGTH_SHORT).show()
        }
    }
}