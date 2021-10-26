package com.mirage.todolist.ui.todolist.tags

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mirage.todolist.R
import com.mirage.todolist.model.repository.LiveTag
import com.mirage.todolist.model.repository.TagID

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

/**
 * View that contains a list of colored tags
 */
class TagsView(context: Context, attrs: AttributeSet?) : ChipGroup(context, attrs) {

    var onTagClickListener: (LiveTag) -> Unit = {}
    var lifecycleOwner: LifecycleOwner? = null

    /** Whether the view should be enlarged (true for tags in Tags tab, false for task's own tag list) */
    private val enlarged: Boolean = attrs?.getAttributeBooleanValue("http://mirage.com/mrg", "enlarged", false) ?: false
    private val closeable: Boolean = attrs?.getAttributeBooleanValue("http://mirage.com/mrg", "closeIconEnabled", false) ?: false

    fun addNewTag(tag: LiveTag) {
        val owner = lifecycleOwner ?: return
        val chip = Chip(context)
        tag.name.observe(owner) {
            chip.text = it
        }
        tag.styleIndex.observe(owner) {
            val style = TagStyle.values()[it.coerceIn(TagStyle.values().indices)]
            val backgroundColor = ContextCompat.getColorStateList(context, style.backgroundColor)
            val textColor = ContextCompat.getColor(context, style.textColor)
            chip.chipBackgroundColor = backgroundColor
            chip.setTextColor(textColor)
            chip.setEnsureMinTouchTargetSize(false)
            chip.setChipStrokeColorResource(style.textColor)
            chip.chipStrokeWidth =
                if (enlarged) resources.getDimension(R.dimen.tag_chip_stroke_width_enlarged)
                else resources.getDimension(R.dimen.tag_chip_stroke_width_normal)
            chip.textSize =
                if (enlarged) resources.getDimension(R.dimen.tag_chip_text_size_enlarged)
                else resources.getDimension(R.dimen.tag_chip_text_size_normal)
            val padding =
                if (enlarged) resources.getDimension(R.dimen.tag_chip_padding_enlarged)
                else resources.getDimension(R.dimen.tag_chip_padding_normal)
            chip.chipStartPadding = padding
            chip.chipEndPadding = padding
        }
        if (closeable) {
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                onTagClickListener(tag)
            }
        }
        else {
            chip.setOnClickListener {
                onTagClickListener(tag)
            }
        }
        addView(chip)
    }

    fun removeTag(tag: LiveTag) {
        removeViewAt(tag.tagIndex)
    }

    fun recreateTags(tags: List<LiveTag>) {
        lifecycleOwner ?: return
        val tagList = tags.sortedBy { it.tagIndex }
        removeAllViews()
        tagList.forEach { tag ->
            addNewTag(tag)
        }
    }

    fun recreateTags(tags: Map<TagID, LiveTag>) {
        recreateTags(tags.values.toList())
    }
}