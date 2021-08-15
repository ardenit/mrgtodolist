package com.mirage.todolist.view.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.R
import com.mirage.todolist.viewmodel.TasklistViewModel

class TasklistRecyclerAdapter(
    private val context: Context?,
    private val viewModel: TasklistViewModel
) : RecyclerView.Adapter<TasklistRecyclerAdapter.TasklistViewHolder>(), ItemTouchHelperAdapter {

    inner class TasklistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var taskTitleView: TextView? = null
        var taskDescriptionView: TextView? = null

        init {
            taskTitleView = itemView.findViewById(R.id.task_title)
            taskDescriptionView = itemView.findViewById(R.id.task_description)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasklistViewHolder {
        val itemView = LayoutInflater.from(context ?: parent.context)
            .inflate(R.layout.task_recycler_item, parent, false)
        return TasklistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TasklistViewHolder, position: Int) {
        holder.taskDescriptionView?.text = "onBindViewHolder position=$position"
    }

    override fun getItemCount(): Int = viewModel.getTaskCount()

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        //TODO
        println("move $fromPosition $toPosition")
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemSwipeLeft(position: Int) {
        //TODO
        println("swipe left $position")
        notifyItemRemoved(position)
    }

    override fun onItemSwipeRight(position: Int) {
        //TODO
        println("swipe right $position")
        notifyItemRemoved(position)
    }

}