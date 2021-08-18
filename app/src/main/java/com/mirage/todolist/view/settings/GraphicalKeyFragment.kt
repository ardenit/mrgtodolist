package com.mirage.todolist.view.settings

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
        inflater.inflate(R.layout.settings_graphical_key, container)
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
                        .putString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_GRAPHICAL_KEY)
                        .putString(SettingsKeys.PROTECTION_GRAPHICAL_HASH_KEY, patternHash)
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