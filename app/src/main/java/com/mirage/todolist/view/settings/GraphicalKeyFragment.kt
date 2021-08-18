package com.mirage.todolist.view.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.andrognito.patternlockview.PatternLockView
import com.mirage.todolist.R

class GraphicalKeyFragment : Fragment() {

    private lateinit var patternLock: PatternLockView
    private lateinit var confirmBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflater.inflate(R.layout.settings_graphical_key, container)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        patternLock = view.findViewById(R.id.create_pattern_lock)
        confirmBtn = view.findViewById(R.id.create_pattern_btn)
    }

}