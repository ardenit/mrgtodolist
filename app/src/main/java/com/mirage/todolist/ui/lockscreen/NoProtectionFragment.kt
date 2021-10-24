package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.mirage.todolist.R
import timber.log.Timber
import javax.inject.Inject

class NoProtectionFragment : Fragment(R.layout.fragment_lockscreen_splash) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LockScreenViewModel by viewModels({ requireActivity() }) { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.v("NoProtectionFragment - onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.v("NoProtectionFragment - onViewCreated")
        super.onViewCreated(view, savedInstanceState)
    }

}