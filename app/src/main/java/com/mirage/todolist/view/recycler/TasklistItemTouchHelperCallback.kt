package com.mirage.todolist.view.recycler

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.viewmodel.TasklistType

class TasklistItemTouchHelperCallback(
    private val adapter: ItemTouchHelperAdapter,
    private val tasklistID: Int
) : ItemTouchHelper.Callback() {

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
        adapter.onItemMove(viewHolder.layoutPosition, target.layoutPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.layoutPosition
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
}