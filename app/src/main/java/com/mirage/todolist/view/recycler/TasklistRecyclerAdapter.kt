package com.mirage.todolist.view.recycler

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.R
import com.mirage.todolist.viewmodel.TasklistType
import com.mirage.todolist.viewmodel.TasklistViewModel

const val STROKE_WIDTH = 8

class TasklistRecyclerAdapter(
    private val context: Context,
    private val viewModel: TasklistViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<TasklistRecyclerAdapter.TasklistViewHolder>(), ItemTouchHelperAdapter {

    inner class TasklistViewHolder(itemView: View, tasklistType: TasklistType) : RecyclerView.ViewHolder(itemView) {

        val taskTitleView: TextView = itemView.findViewById(R.id.task_title)
        val taskDescriptionView: TextView = itemView.findViewById(R.id.task_description)
        val background: GradientDrawable = itemView.background.current as GradientDrawable

        @ColorInt val strokeColor = ContextCompat.getColor(context, tasklistType.strokeColor)
        @ColorInt val fillColor = ContextCompat.getColor(context, tasklistType.fillColor)
        @ColorInt val fillColorFocused = ContextCompat.getColor(context, tasklistType.fillColorFocused)

        @ColorInt val strokeColorToLeft: Int = ContextCompat.getColor(
            context,
            TasklistType.getType(tasklistType.index - 1).strokeColor
        )
        @ColorInt val strokeColorToRight: Int = ContextCompat.getColor(
            context,
            TasklistType.getType(tasklistType.index + 1).strokeColor
        )
        @ColorInt val fillColorToLeft: Int = ContextCompat.getColor(
            context,
            TasklistType.getType(tasklistType.index - 1).fillColor
        )
        @ColorInt val fillColorToRight: Int = ContextCompat.getColor(
            context,
            TasklistType.getType(tasklistType.index + 1).fillColor
        )

        init {
            background.setColor(fillColor)
            background.setStroke(STROKE_WIDTH, strokeColor)
        }
    }

    init {
        viewModel.addOnNewTaskListener(lifecycleOwner) {
            notifyItemInserted(it.taskIndex)
        }
        viewModel.addOnFullTasklistUpdateListener(lifecycleOwner) {
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasklistViewHolder {
        val itemView = LayoutInflater.from(context ?: parent.context)
            .inflate(R.layout.task_recycler_item, parent, false)
        return TasklistViewHolder(itemView, TasklistType.getType(viewModel.getTasklistID()))
    }

    override fun onBindViewHolder(holder: TasklistViewHolder, position: Int) {
        val task = viewModel.getTaskByIndex(position) ?: return
        task.title.observe(lifecycleOwner) {
            holder.taskTitleView.text = it
        }
        task.description.observe(lifecycleOwner) {
            holder.taskDescriptionView.text = it
        }
    }

    override fun getItemCount(): Int = viewModel.getTaskCount()

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        viewModel.dragTask(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemSwipeLeft(position: Int) {
        viewModel.swipeTaskLeft(position)
        notifyItemRemoved(position)
    }

    override fun onItemSwipeRight(position: Int) {
        viewModel.swipeTaskRight(position)
        notifyItemRemoved(position)
    }
}