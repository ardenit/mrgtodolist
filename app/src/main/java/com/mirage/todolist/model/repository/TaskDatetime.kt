package com.mirage.todolist.model.repository

import androidx.annotation.StringRes
import com.mirage.todolist.R

/** Immutable information about task's date. If [year] < 0, the date is considered not set */
data class TaskDate(
    val year: Int,
    val monthOfYear: Int,
    val dayOfMonth: Int
) {

    fun isValid(): Boolean =
        year >= 0 && monthOfYear >= 0 && dayOfMonth >= 0
}

/** Immutable information about task's time. If [hour] < 0, the time is considered not set */
data class TaskTime(
    val hour: Int,
    val minute: Int
) {

    fun isValid(): Boolean =
        hour >= 0 && minute >= 0
}

enum class TaskPeriod(@StringRes val nameRes: Int) {
    NOT_REPEATABLE(R.string.task_period_not_repeatable),
    DAILY(R.string.task_period_daily),
    WEEKLY(R.string.task_period_weekly),
    MONTHLY(R.string.task_period_monthly),
    YEARLY(R.string.task_period_yearly)
}
