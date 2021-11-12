package com.mirage.todolist.ui.lockscreen

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import javax.inject.Inject

class LockScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: LockScreenViewModel by viewModels { viewModelFactory }

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        (application as App).appComponent.inject(this)
        navController = Navigation.findNavController(this, R.id.nav_lock_screen_host_fragment)
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

    override fun onResume() {
        super.onResume()
        viewModel.processProtectionPreference()
    }
}