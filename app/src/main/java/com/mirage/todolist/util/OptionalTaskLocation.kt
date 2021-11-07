package com.mirage.todolist.util

data class OptionalTaskLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val locationSet: Boolean
) {
    companion object {
        val NOT_SET = OptionalTaskLocation(0.0, 0.0, "", false)
    }
}