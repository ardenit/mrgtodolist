package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.mirage.todolist.R
import javax.inject.Inject

class GraphicalKeyFragment : Fragment(R.layout.fragment_lockscreen_graphical_key) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LockScreenViewModel by activityViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val patternLock: PatternLockView = view.findViewById(R.id.graphical_key_pattern_lock)
        patternLock.addPatternLockListener(object : PatternLockViewListener {

            override fun onStarted() { }

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) { }

            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                val patternString = PatternLockUtils.patternToString(patternLock, pattern)
                if (viewModel.tryPattern(patternString)) {
                    viewModel.unlockTodolist()
                }
                else {
                    patternLock.clearPattern()
                }
            }

            override fun onCleared() { }
        })
        super.onViewCreated(view, savedInstanceState)
    }
}