package com.mirage.todolist.view.recycler

import android.graphics.Canvas
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.viewmodel.TasklistType
import kotlin.math.abs
import kotlin.math.roundToInt

class TasklistItemTouchHelperCallback(
    private val adapter: ItemTouchHelperAdapter,
    private val tasklistID: Int
) : ItemTouchHelper.Callback() {

    private var draggedItem: TasklistRecyclerAdapter.TasklistViewHolder? = null

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        var swipeFlags = 0
        if (tasklistID > 0) swipeFlags = swipeFlags or ItemTouchHelper.START
        if (tasklistID < TasklistType.values().size - 1) swipeFlags = swipeFlags or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        if (direction == ItemTouchHelper.START) {
            adapter.onItemSwipeLeft(position)
        }
        else if (direction == ItemTouchHelper.END) {
            adapter.onItemSwipeRight(position)
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (viewHolder == null) {
            draggedItem?.run {
                background.setColor(fillColor)
            }
            draggedItem = null
        }
        else {
            viewHolder as TasklistRecyclerAdapter.TasklistViewHolder
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.background.setColor(viewHolder.fillColorFocused)
                draggedItem = viewHolder
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        viewHolder as TasklistRecyclerAdapter.TasklistViewHolder
        val width = viewHolder.itemView.width.toFloat()
        val shift = dX / width
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                val alpha = 1.0f - abs(dX) / width
                viewHolder.itemView.alpha = alpha
                when {
                    shift < -0.2f -> {
                        val progress = (-0.2f - shift) * 1.25f
                        val strokeColor = progressedColor(viewHolder.strokeColor, viewHolder.strokeColorToLeft, progress)
                        viewHolder.background.setStroke(STROKE_WIDTH, strokeColor)
                        val fillColor = progressedColor(viewHolder.fillColor, viewHolder.fillColorToLeft, progress)
                        viewHolder.background.setColor(fillColor)
                    }
                    shift > 0.2f -> {
                        val progress = (shift - 0.2f) * 1.25f
                        val strokeColor = progressedColor(viewHolder.strokeColor, viewHolder.strokeColorToRight, progress)
                        viewHolder.background.setStroke(STROKE_WIDTH, strokeColor)
                        val fillColor = progressedColor(viewHolder.fillColor, viewHolder.fillColorToRight, progress)
                        viewHolder.background.setColor(fillColor)
                    }
                    else -> {
                        viewHolder.background.setColor(viewHolder.fillColor)
                        viewHolder.background.setStroke(STROKE_WIDTH, viewHolder.strokeColor)
                    }
                }
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        viewHolder as TasklistRecyclerAdapter.TasklistViewHolder
        viewHolder.background.setColor(viewHolder.fillColor)
        viewHolder.background.setStroke(STROKE_WIDTH, viewHolder.strokeColor)
        super.clearView(recyclerView, viewHolder)
    }
}

@ColorInt
fun progressedColor(@ColorInt startColor: Int, @ColorInt endColor: Int, progress: Float): Int {
    val r = progressedValue(startColor.red, endColor.red, progress)
    val g = progressedValue(startColor.green, endColor.green, progress)
    val b = progressedValue(startColor.blue, endColor.blue, progress)
    return Color.argb(startColor.alpha, r, g, b)
}

private fun progressedValue(start: Int, end: Int, progress: Float): Int =
    start + ((end - start).toFloat() * progress).roundToInt()