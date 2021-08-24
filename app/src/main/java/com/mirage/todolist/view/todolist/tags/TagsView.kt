package com.mirage.todolist.view.todolist.tags

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.model.tasks.TagID

private const val CHIP_STROKE_WIDTH = 8f
private val CHIP_STYLES = listOf(
    ChipStyle(R.color.tag_background_green, R.color.white)
)

/**
 * View that contains a list of colored tags
 */
class TagsView(context: Context, attrs: AttributeSet? = null) : ChipGroup(context, attrs) {

    var onTagClickListener: (LiveTag) -> Unit = {}
    var lifecycleOwner: LifecycleOwner? = null

    /** Whether the view should be enlarged (true for tags in Tags tab, false for task's own tag list) */
    private val enlarged: Boolean = attrs?.getAttributeBooleanValue(R.attr.enlarged, false) ?: false

    init {

    }

    fun recreateTags(tags: Map<TagID, LiveTag>) {
        val owner = lifecycleOwner ?: return
        removeAllViews()
        val tagList = tags.values.sortedBy { it.tagIndex }
        tagList.forEach { tag ->
            val chip = Chip(context)
            tag.name.observe(owner) {
                chip.text = it
            }
            tag.styleIndex.observe(owner) {
                val style = CHIP_STYLES[it.coerceIn(CHIP_STYLES.indices)]
                val backgroundColor = ContextCompat.getColorStateList(context, style.backgroundColor)
                val textColor = ContextCompat.getColor(context, style.textColor)
                chip.chipBackgroundColor = backgroundColor
                chip.setTextColor(textColor)
                chip.setChipStrokeColorResource(style.textColor)
                chip.chipStrokeWidth = CHIP_STROKE_WIDTH
                if (enlarged) {

                }
            }
            chip.setOnClickListener {
                println("click ${tag.name}")
                onTagClickListener(tag)
            }
            addView(chip)
        }
    }
}

private data class ChipStyle(
    @ColorRes val backgroundColor: Int,
    @ColorRes val textColor: Int
)