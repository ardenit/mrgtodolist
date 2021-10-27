package com.mirage.todolist.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.mirage.todolist.R
import com.mirage.todolist.databinding.FragmentAddGraphicalKeyBinding
import com.mirage.todolist.databinding.FragmentTasklistBinding
import com.mirage.todolist.di.App
import com.mirage.todolist.util.PasswordValidator
import com.mirage.todolist.util.autoCleared
import timber.log.Timber
import javax.inject.Inject

class AddGraphicalKeyFragment : Fragment() {

    @Inject
    lateinit var preferences: SharedPreferences
    var onPatternConfirm: (() -> Unit)? = null

    private var binding by autoCleared<FragmentAddGraphicalKeyBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity().application as App).appComponent.inject(this)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_graphical_key,
            container,
            false
        )
        return binding.createPatternRoot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.v("AddGraphicalKeyFragment - onViewCreated")
        binding.createPatternBtn.setOnClickListener {
            with(binding.createPatternLock) {
                when {
                    pattern.size == 0 -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.protection_create_graphical_empty,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    pattern.size < 4 -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.protection_create_graphical_simple,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        val patternString = PatternLockUtils.patternToString(
                            this,
                            pattern
                        )
                        val patternHash = PasswordValidator.getSHA256(patternString)
                        preferences.edit()
                            .putString(
                                resources.getString(R.string.key_set_protection),
                                resources.getString(R.string.value_protection_graphical)
                            )
                            .putString(
                                resources.getString(R.string.key_graphical_key_hash),
                                patternHash
                            )
                            .apply()
                        onPatternConfirm?.invoke()
                    }
                }
            }
        }
    }

    fun clear() {
        binding.createPatternRoot.removeAllViews()
    }
}