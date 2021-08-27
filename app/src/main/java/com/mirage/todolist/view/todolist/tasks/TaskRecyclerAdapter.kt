package com.mirage.todolist.view.todolist.tasks

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.view.todolist.tags.TagsView
import com.mirage.todolist.viewmodel.TasklistType
import com.mirage.todolist.viewmodel.TaskRecyclerViewModel

const val STROKE_WIDTH = 8

class TasklistRecyclerAdapter(
    private val context: Context,
    private val viewModel: TaskRecyclerViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onTagSearchListener: (LiveTag) -> Unit
) : RecyclerView.Adapter<TasklistRecyclerAdapter.TasklistViewHolder>() {

    inner class TasklistViewHolder(itemView: View, tasklistType: TasklistType) : RecyclerView.ViewHolder(itemView) {

        val rootLayout: ConstraintLayout = itemView.findViewById(R.id.task_item_layout)
        val taskTitleView: TextView = itemView.findViewById(R.id.task_title)
        val taskDescriptionView: TextView = itemView.findViewById(R.id.task_description)
        val taskDatetimeView: TextView = itemView.findViewById(R.id.task_datetime)
        val taskPlaceView: TextView = itemView.findViewById(R.id.task_place)
        val taskTags: TagsView = itemView.findViewById(R.id.task_tags)
        //TODO Start drag and swipe manually on click to layout to prevent dragging after btn press
        val taskEditBtn: ImageButton = itemView.findViewById(R.id.task_edit_btn)
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
        viewModel.addOnFullUpdateTaskListener(lifecycleOwner) {
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
            println("notifyDataSetChanged")
            println(viewModel.getVisibleTaskCount())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasklistViewHolder {
        val itemView = LayoutInflater.from(context ?: parent.context)
            .inflate(R.layout.task_recycler_item, parent, false)
        return TasklistViewHolder(itemView, TasklistType.getType(viewModel.getTasklistID()))
    }

    override fun onBindViewHolder(holder: TasklistViewHolder, position: Int) {
        val task = viewModel.getTaskByVisibleIndex(position) ?: return
        task.title.observe(lifecycleOwner) {
            holder.taskTitleView.text = it
        }
        task.description.observe(lifecycleOwner) {
            holder.taskDescriptionView.text = it
        }
        holder.taskTags.lifecycleOwner = lifecycleOwner
        holder.taskTags.onTagClickListener = { tag ->
            //TODO delete? viewModel.searchForTag(tag)
            onTagSearchListener(tag)
        }
        task.tags.observe(lifecycleOwner) {
            holder.taskTags.recreateTags(it)
        }
    }

    override fun getItemCount(): Int = viewModel.getVisibleTaskCount()

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        viewModel.dragTask(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemSwipeLeft(position: Int) {
        viewModel.swipeTaskLeft(position)
        notifyItemRemoved(position)
    }

    fun onItemSwipeRight(position: Int) {
        viewModel.swipeTaskRight(position)
        notifyItemRemoved(position)
    }
}