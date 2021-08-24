package com.mirage.todolist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

enum class LockScreenType {
    NO_PROTECTION,
    TAP_TO_UNLOCK,
    GRAPHICAL_KEY,
    PASSWORD,
    FINGERPRINT
}

abstract class LockScreenViewModel(application: Application) : AndroidViewModel(application) {

    abstract val lockScreenType: LiveData<LockScreenType>

    abstract fun init()

    abstract fun tryPattern(patternString: String): Boolean

    abstract fun tryPassword(password: String): Boolean

}