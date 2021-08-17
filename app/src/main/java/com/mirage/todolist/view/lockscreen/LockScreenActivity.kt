package com.mirage.todolist.view.lockscreen

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.mirage.todolist.R
import com.mirage.todolist.view.settings.SettingsKeys
import com.mirage.todolist.view.todolist.TodolistActivity
import kotlinx.coroutines.*

class LockScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val coroutineScope = CoroutineScope(SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        processProtectionPreference()
        processThemePreference()
    }

    private fun openTodolist() {
        startActivity(Intent(this, TodolistActivity::class.java))
    }

    private fun processProtectionPreference() {
        when (sharedPreferences.getString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_VALUE)) {
            SettingsKeys.PROTECTION_NONE_VALUE -> {
                sharedPreferences.edit()
                    .putString(SettingsKeys.SET_PROTECTION_KEY, SettingsKeys.PROTECTION_NONE_VALUE)
                    .apply()
                setContentView(R.layout.lockscreen_splash)
                coroutineScope.launch(Dispatchers.Main) {
                    delay(1000)
                    openTodolist()
                }
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
}