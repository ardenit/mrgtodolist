package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mirage.todolist.R
import com.mirage.todolist.databinding.FragmentLockscreenTapBinding
import com.mirage.todolist.di.App
import com.mirage.todolist.util.autoCleared
import com.mirage.todolist.util.startHintTextColorAnimation
import javax.inject.Inject

class TapFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    private var binding by autoCleared<FragmentLockscreenTapBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as App).appComponent.inject(this)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_lockscreen_tap,
            container,
            false
        )
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.startHintTextColorAnimation(view.findViewById(R.id.tap_unlock_hint))
        super.onViewCreated(view, savedInstanceState)
    }
}