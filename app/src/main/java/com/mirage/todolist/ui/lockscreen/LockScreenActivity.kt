package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.math.abs


class LockScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LockScreenViewModel by viewModels { viewModelFactory }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).appComponent.inject(this)
        navController = Navigation.findNavController(this, R.id.nav_fragment_no_protection)
        viewModel.lockScreenType.observe(this) {
            val destination = when (it) {
                LockScreenType.NO_PROTECTION -> R.id.nav_fragment_no_protection
                LockScreenType.TAP_TO_UNLOCK -> R.id.nav_fragment_tap
                LockScreenType.GRAPHICAL_KEY -> R.id.nav_fragment_graphical_key
                LockScreenType.PASSWORD -> R.id.nav_fragment_password
                LockScreenType.FINGERPRINT -> R.id.nav_fragment_fingerprint
                else -> R.id.nav_unlock_todolist
            }
            navController.navigate(destination)
        }
    }
}