package com.mirage.todolist.ui.todolist.tags

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.mirage.todolist.R


/**
 * Possible tag styles which can be selected by a user.
 */
enum class TagStyle(
    @StringRes val colorName: Int,
    @ColorRes val backgroundColor: Int,
    @ColorRes val textColor: Int
) {

    GREEN(R.string.tags_color_green, R.color.tag_background_green, R.color.white),
    BLUE(R.string.tags_color_blue, R.color.tag_background_blue, R.color.white),
    ORANGE(R.string.tags_color_orange, R.color.tag_background_orange, R.color.white),
    GREY(R.string.tags_color_grey, R.color.tag_background_grey, R.color.white),
    RED(R.string.tags_color_red, R.color.tag_background_red, R.color.white),
    PURPLE(R.string.tags_color_purple, R.color.tag_background_purple, R.color.white)
}