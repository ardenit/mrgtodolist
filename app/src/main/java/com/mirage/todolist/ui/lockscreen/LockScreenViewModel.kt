package com.mirage.todolist.ui.lockscreen

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.model.repository.TodoRepository
import com.mirage.todolist.util.PasswordValidator
import com.mirage.todolist.util.PreferenceHolder
import com.mirage.todolist.util.getStringPreference
import com.mirage.todolist.util.setStringPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LockScreenViewModel @Inject constructor(
    private val application: App,
    override val preferences: SharedPreferences,
    override val resources: Resources
) : ViewModel(), PreferenceHolder {

    val lockScreenType: MutableLiveData<LockScreenType> = MutableLiveData()

    init {
        Timber.v("LockScreenViewModel - init")
        processThemePreference()
        processNotificationPreference()
    }

    fun unlockTodolist() {
        lockScreenType.postValue(LockScreenType.UNLOCKED)
    }

    fun tryPattern(patternString: String): Boolean {
        val patternHash = PasswordValidator.getSHA256(patternString)
        val validHash = getStringPreference(R.string.key_graphical_key_hash)
        return patternHash == validHash
    }

    fun tryPassword(password: String): Boolean {
        val inputHash = PasswordValidator.getSHA256(password)
        val validHash = getStringPreference(R.string.key_password_hash)
        return inputHash == validHash
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

    private fun startSplashScreenAutoUnlock() {
        viewModelScope.launch {
            delay(SPLASH_SCREEN_DELAY)
            lockScreenType.value = LockScreenType.UNLOCKED
        }
    }

    @SuppressLint("MissingPermission")
    fun processProtectionPreference() {
        val protectionType = getStringPreference(R.string.key_set_protection, R.string.value_protection_none)
        lockScreenType.value = when (protectionType) {
            resources.getString(R.string.value_protection_none) -> {
                setStringPreference(R.string.key_set_protection, R.string.value_protection_none)
                startSplashScreenAutoUnlock()
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
                val fingerprintManager = FingerprintManagerCompat.from(application.applicationContext)
                if (!fingerprintManager.isHardwareDetected || !fingerprintManager.hasEnrolledFingerprints()) {
                    startSplashScreenAutoUnlock()
                    LockScreenType.NO_PROTECTION
                } else {
                    LockScreenType.FINGERPRINT
                }
            }
            else -> {
                setStringPreference(R.string.key_set_protection, R.string.value_protection_none)
                LockScreenType.NO_PROTECTION
            }
        }
    }

    companion object {
        private const val SPLASH_SCREEN_DELAY = 1500L
    }
}