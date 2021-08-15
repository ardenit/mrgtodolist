package com.mirage.todolist.viewmodel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mirage.todolist.R

enum class TasklistType(
    val index: Int,
    @StringRes val title: Int,
    @DrawableRes val icon: Int
    ) {

    ARCHIVE(0, R.string.footer_archive_btn, R.drawable.ic_nav_archive),
    TODO(1, R.string.footer_todo_btn, R.drawable.ic_nav_todo),
    DONE(2, R.string.footer_done_btn, R.drawable.ic_nav_done);

    companion object {

        val typesCount = values().size

        fun getType(index: Int): TasklistType = values()[index.coerceIn(0, typesCount - 1)]

    }
}