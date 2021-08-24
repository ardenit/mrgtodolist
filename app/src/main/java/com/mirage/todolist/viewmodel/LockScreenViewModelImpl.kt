package com.mirage.todolist.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.TodolistModel
import com.mirage.todolist.model.tasks.getTodolistModel

class LockScreenViewModelImpl(application: Application) : LockScreenViewModel(application), PreferenceHolder {

    override val lockScreenType: MutableLiveData<LockScreenType> = MutableLiveData()

    override val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
    override val resources: Resources = application.resources

    //TODO Inject
    private val todolistModel: TodolistModel = getTodolistModel()

    override fun init() {
        todolistModel.init(getApplication<Application>().applicationContext)
        processThemePreference()
        processNotificationPreference()
        processProtectionPreference()
    }

    override fun tryPattern(patternString: String): Boolean {
        val patternHash = PasswordValidator.getSHA256(patternString)
        val validHash = getStringPreference(R.string.key_protection_graphical)
        return patternHash == validHash
    }

    override fun tryPassword(password: String): Boolean {
        val inputHash = PasswordValidator.getSHA256(password)
        val validHash = getStringPreference(R.string.key_password_hash)
        return inputHash == validHash
    }

    private fun processProtectionPreference() {
        lockScreenType.value = when (getStringPreference(R.string.key_set_protection, R.string.value_protection_none)) {
            resources.getString(R.string.value_protection_none) -> {
                setStringPreference(R.string.key_set_protection, R.string.value_protection_none)
                LockScreenType.NO_PROTECTION
            }
            resources.getString(R.string.value_protection_tap) -> {
                LockScreenType.TAP_TO_UNLOCK
            }
            resources.getString(R.string.value_protection_graphical) -> {
                LockScreenType.GRAPHICAL_KEY
            }
            resources.getString(R.string.value_protection_password) -> {
                LockScreenType.PASSWORD
            }
            resources.getString(R.string.value_protection_fingerprint) -> {
                LockScreenType.FINGERPRINT
            }
            else -> {
                setStringPreference(R.string.key_set_protection, R.string.value_protection_none)
                LockScreenType.NO_PROTECTION
            }
        }
    }

    private fun processThemePreference() {
        when (getStringPreference(R.string.key_change_theme, R.string.value_theme_light)) {
            resources.getString(R.string.value_theme_light) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setStringPreference(R.string.key_change_theme, R.string.value_theme_light)
            }
            resources.getString(R.string.value_theme_dark) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            resources.getString(R.string.value_theme_system) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun processNotificationPreference() {
        when (getStringPreference(R.string.key_notify_on_datetime, R.string.value_notify_never)) {
            resources.getString(R.string.value_notify_never) -> {
                setStringPreference(R.string.key_notify_on_datetime, R.string.value_notify_never)
            }
        }
    }
}