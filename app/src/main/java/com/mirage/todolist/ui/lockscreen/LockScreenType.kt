package com.mirage.todolist.ui.lockscreen

enum class LockScreenType {
    NO_PROTECTION,
    TAP_TO_UNLOCK,
    GRAPHICAL_KEY,
    PASSWORD,
    FINGERPRINT,
    UNLOCKED // Should navigate to TodolistActivity
}