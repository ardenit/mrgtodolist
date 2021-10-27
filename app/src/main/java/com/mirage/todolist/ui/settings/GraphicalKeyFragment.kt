package com.mirage.todolist.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.mirage.todolist.R
import com.mirage.todolist.util.PasswordValidator

class GraphicalKeyFragment : Fragment() {

    private lateinit var patternLock: PatternLockView
    private lateinit var confirmBtn: Button
    private lateinit var preferences: SharedPreferences
    private lateinit var container: ViewGroup
    var onPatternConfirm: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflater.inflate(R.layout.fragment_settings_graphical_key, container)
        this.container = container!!
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        patternLock = container.findViewById(R.id.create_pattern_lock)
        confirmBtn = container.findViewById(R.id.create_pattern_btn)
        confirmBtn.setOnClickListener {
            when {
                patternLock.pattern.size == 0 -> {
                    Toast.makeText(requireContext(), R.string.protection_create_graphical_empty, Toast.LENGTH_SHORT).show()
                }
                patternLock.pattern.size < 4 -> {
                    Toast.makeText(requireContext(), R.string.protection_create_graphical_simple, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val patternString = PatternLockUtils.patternToString(patternLock, patternLock.pattern)
                    val patternHash = PasswordValidator.getSHA256(patternString)
                    preferences.edit()
                        .putString(resources.getString(R.string.key_set_protection), resources.getString(R.string.value_protection_graphical))
                        .putString(resources.getString(R.string.key_graphical_key_hash), patternHash)
                        .apply()
                    onPatternConfirm?.invoke()
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun clear() {
        container.removeAllViews()
    }
}