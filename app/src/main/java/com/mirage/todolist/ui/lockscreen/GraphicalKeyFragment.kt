package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import timber.log.Timber
import javax.inject.Inject

class GraphicalKeyFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity().application as App).appComponent.inject(this)
        return inflater.inflate(R.layout.fragment_lockscreen_graphical_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.v("ON_VIEW_CREATED")
        val patternLock: PatternLockView = view.findViewById(R.id.graphical_key_pattern_lock)
        patternLock.addPatternLockListener(object : PatternLockViewListener {

            override fun onStarted() { }

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) { }

            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                val patternString = PatternLockUtils.patternToString(patternLock, pattern)
                Timber.v("Pattern $patternString")
                if (viewModel.tryPattern(patternString)) {
                    Timber.v("Correct pattern")
                    viewModel.unlockTodolist()
                }
                else {
                    Timber.v("Wrong pattern")
                    patternLock.clearPattern()
                }
            }

            override fun onCleared() { }
        })
        super.onViewCreated(view, savedInstanceState)
    }
}