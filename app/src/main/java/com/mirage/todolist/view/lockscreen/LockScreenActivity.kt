package com.mirage.todolist.view.lockscreen

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.mirage.todolist.R
import com.mirage.todolist.view.settings.SettingsKeys
import com.mirage.todolist.view.todolist.TodolistActivity
import kotlinx.coroutines.*

class LockScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val coroutineScope = lifecycleScope

    private val todolistResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        processProtectionPreference()
        processThemePreference()
        processNotificationPreference()
    }

    private fun initializeSplashScreen() {
        setContentView(R.layout.lockscreen_splash)
        coroutineScope.launch(Dispatchers.Main) {
            delay(1000)
            openTodolist()
        }
    }

    private fun initializeTapToUnlockScreen() {
        setContentView(R.layout.lockscreen_tap)
        val rootLayout: ConstraintLayout = findViewById(R.id.lockscreen_tap_root)
        rootLayout.setOnClickListener {
            openTodolist()
        }
    }

    private fun openTodolist() {
        val intent = Intent(this, TodolistActivity::class.java)
        todolistResultLauncher.launch(intent)
    }

    private fun processProtectionPreference() {
        when (sharedPreferences.getString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_VALUE)) {
            SettingsKeys.PROTECTION_NONE_VALUE -> {
                sharedPreferences.edit()
                    .putString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_VALUE)
                    .apply()
                initializeSplashScreen()
            }
            SettingsKeys.PROTECTION_TAP_VALUE -> {
                initializeTapToUnlockScreen()
            }
        }
    }

    private fun processThemePreference() {
        when (sharedPreferences.getString(SettingsKeys.CHANGE_THEME_KEY, SettingsKeys.THEME_LIGHT_VALUE)) {
            SettingsKeys.THEME_LIGHT_VALUE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                sharedPreferences.edit()
                    .putString(SettingsKeys.CHANGE_THEME_KEY, SettingsKeys.THEME_LIGHT_VALUE)
                    .apply()
            }
            SettingsKeys.THEME_DARK_VALUE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            SettingsKeys.THEME_SYSTEM_VALUE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun processNotificationPreference() {
        when (sharedPreferences.getString(SettingsKeys.NOTIFY_ON_DATETIME_KEY, SettingsKeys.NOTIFY_DATETIME_NEVER)) {
            SettingsKeys.NOTIFY_DATETIME_NEVER -> {
                sharedPreferences.edit()
                    .putString(SettingsKeys.NOTIFY_ON_DATETIME_KEY, SettingsKeys.NOTIFY_DATETIME_NEVER)
                    .apply()
            }
        }
    }
}