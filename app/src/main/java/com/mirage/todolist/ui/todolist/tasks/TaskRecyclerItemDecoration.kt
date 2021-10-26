package com.mirage.todolist.ui.todolist.tasks

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TasklistItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = RECYCLER_ITEM_SPACING
        outRect.right = RECYCLER_ITEM_SPACING
        outRect.top = RECYCLER_ITEM_SPACING
        outRect.bottom = 0
    }

    companion object {
        private const val RECYCLER_ITEM_SPACING = 32
    }
}