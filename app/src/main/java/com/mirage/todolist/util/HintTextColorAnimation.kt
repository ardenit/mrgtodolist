package com.mirage.todolist.util

import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TAP_HINT_ANIMATION_PERIOD = 4000L
private const val TAP_HINT_ANIMATION_DELAY = 40L

/**
 * Animates the color of the given TextView, smoothly changing its alpha value over time
 */
fun CoroutineScope.startHintTextColorAnimation(hintText: TextView) {
    val startTime = System.currentTimeMillis()
    launch {
        while (true) {
            val time = System.currentTimeMillis() - startTime
            val progress =
                (time % TAP_HINT_ANIMATION_PERIOD).toFloat() / TAP_HINT_ANIMATION_PERIOD.toFloat()
            hintText.alpha = abs(0.5f - progress) * 2f
            delay(TAP_HINT_ANIMATION_DELAY)
        }
    }
}
