package com.mirage.todolist.model.repository

import androidx.annotation.StringRes
import com.mirage.todolist.R

enum class TaskPeriod(@StringRes val nameRes: Int) {
    NOT_REPEATABLE(R.string.task_period_not_repeatable),
    DAILY(R.string.task_period_daily),
    WEEKLY(R.string.task_period_weekly),
    MONTHLY(R.string.task_period_monthly),
    YEARLY(R.string.task_period_yearly)
}