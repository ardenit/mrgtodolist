package com.mirage.todolist.util

import java.time.LocalDate
import java.time.LocalTime

data class OptionalDate(
    val date: LocalDate,
    val dateSet: Boolean
) {
    companion object {
        val NOT_SET = OptionalDate(LocalDate.MIN, false)
    }
}

data class OptionalTime(
    val time: LocalTime,
    val timeSet: Boolean
) {
    companion object {
        val NOT_SET = OptionalTime(LocalTime.MIN, false)
    }
}