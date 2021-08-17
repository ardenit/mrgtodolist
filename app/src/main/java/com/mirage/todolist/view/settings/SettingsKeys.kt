package com.mirage.todolist.view.settings

/** Keys used for preference items in res/xml/settings_screen.xml */
object SettingsKeys {

    const val CHANGE_THEME_KEY = "change_theme"
    const val SET_PROTECTION_KEY = "set_protection"
    const val NOTIFY_ON_SYNC_KEY = "notify_on_sync"
    const val NOTIFY_ON_DATETIME_KEY = "notify_on_datetime"
    const val SYNC_SELECT_ACC_KEY = "sync_select_acc"

    const val THEME_LIGHT_VALUE = "1"
    const val THEME_DARK_VALUE = "2"
    const val THEME_SYSTEM_VALUE = "3"

    const val PROTECTION_NONE_VALUE = "1"
    const val PROTECTION_TAP_VALUE = "2"
    const val PROTECTION_GRAPHICAL_KEY_VALUE = "3"
    const val PROTECTION_PASSWORD_VALUE = "4"
    const val PROTECTION_FINGERPRINT_VALUE = "5"

    const val NOTIFY_DATETIME_NEVER = "1"
    const val NOTIFY_DATETIME_5_MIN = "2"
    const val NOTIFY_DATETIME_10_MIN = "3"
    const val NOTIFY_DATETIME_30_MIN = "4"
    const val NOTIFY_DATETIME_1_HOUR = "5"
}