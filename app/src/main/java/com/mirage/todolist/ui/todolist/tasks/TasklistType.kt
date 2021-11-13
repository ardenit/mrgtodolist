package com.mirage.todolist.ui.todolist.tasks

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mirage.todolist.R

enum class TasklistType(
    val index: Int,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @ColorRes val strokeColor: Int,
    @ColorRes val fillColor: Int,
    @ColorRes val fillColorFocused: Int
) {

    ARCHIVE(
        index = 0,
        title = R.string.footer_archive_btn,
        icon = R.drawable.ic_nav_archive,
        strokeColor = R.color.archive_stroke,
        fillColor = R.color.archive_fill,
        fillColorFocused = R.color.archive_fill_focused
    ),
    TODO(
        index = 1,
        title = R.string.footer_todo_btn,
        icon = R.drawable.ic_nav_todo,
        strokeColor = R.color.todo_stroke,
        fillColor = R.color.todo_fill,
        fillColorFocused = R.color.todo_fill_focused
    ),
    DONE(
        index = 2,
        title = R.string.footer_done_btn,
        icon = R.drawable.ic_nav_done,
        strokeColor = R.color.done_stroke,
        fillColor = R.color.done_fill,
        fillColorFocused = R.color.done_fill_focused
    );

    companion object {
        val typesCount = values().size

        fun getType(index: Int): TasklistType = values()[index.coerceIn(0, typesCount - 1)]
    }
}