package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mirage.todolist.R
import com.mirage.todolist.util.startHintTextColorAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

class TapFragment : Fragment(R.layout.fragment_lockscreen_tap) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rootLayout: ConstraintLayout = view.findViewById(R.id.lockscreen_tap_root)
        rootLayout.setOnClickListener {
            viewModel.unlockTodolist() // TODO use data binding
        }
        lifecycleScope.startHintTextColorAnimation(view.findViewById(R.id.tap_unlock_hint))
        super.onViewCreated(view, savedInstanceState)
    }
}