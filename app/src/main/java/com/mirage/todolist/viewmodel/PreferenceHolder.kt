package com.mirage.todolist.viewmodel

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.StringRes

interface PreferenceHolder {
    val preferences: SharedPreferences
    val resources: Resources
}

fun PreferenceHolder.getStringPreference(@StringRes key: Int, @StringRes defaultValue: Int) =
    preferences.getString(resources.getString(key), resources.getString(defaultValue))

fun PreferenceHolder.getStringPreference(@StringRes key: Int) =
    preferences.getString(resources.getString(key), "")

fun PreferenceHolder.setStringPreference(@StringRes key: Int, @StringRes value: Int) {
    preferences.edit()
        .putString(resources.getString(key), resources.getString(value))
        .apply()
}