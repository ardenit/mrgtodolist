package com.mirage.todolist.ui.todolist.tasks

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
import com.mirage.todolist.model.repository.LiveTag
import com.mirage.todolist.model.repository.LiveTask
import com.mirage.todolist.ui.todolist.tags.TagsView
import timber.log.Timber

class TasklistRecyclerAdapter(
    private val context: Context,
    private val viewModel: TaskRecyclerViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onTagSearchListener: (LiveTag) -> Unit,
    private val onTaskEditListener: (LiveTask) -> Unit
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
            Timber.v("Adapter notified - onNewTask on position ${it.taskIndex}")
            notifyItemInserted(it.taskIndex)
        }
        viewModel.addOnFullUpdateTaskListener(lifecycleOwner) {
            Timber.v("Adapter notified - onFullUpdate")
            @Suppress("NotifyDataSetChanged")
            notifyDataSetChanged()
        }
        Timber.v("TaskRecyclerAdapter ${viewModel.getTasklistID()} - visible tasks: ${viewModel.getVisibleTaskCount()}")
        @Suppress("NotifyDataSetChanged")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasklistViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_todolist_task, parent, false)
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
            onTagSearchListener(tag)
        }
        task.tags.observe(lifecycleOwner) {
            holder.taskTags.recreateTags(it)
        }
        holder.taskEditBtn.setOnClickListener {
            onTaskEditListener(task)
        }
        task.location.observe(lifecycleOwner) {
            updateLocationText(task, holder)
        }
        task.date.observe(lifecycleOwner) {
            updateDatetimeText(task, holder)
        }
        task.time.observe(lifecycleOwner) {
            updateDatetimeText(task, holder)
        }
        task.period.observe(lifecycleOwner) {
            updateDatetimeText(task, holder)
        }
    }

    private fun updateLocationText(task: LiveTask, holder: TasklistViewHolder) {
        val location = task.location.value ?: return
        if (location.locationSet) {
            holder.taskPlaceView.text = location.address
            holder.taskPlaceView.visibility = View.VISIBLE
        } else {
            holder.taskPlaceView.visibility = View.GONE
        }
    }

    private fun updateDatetimeText(task: LiveTask, holder: TasklistViewHolder) {
        var datetimeText = ""
        val date = task.date.value ?: return
        val time = task.time.value ?: return
        val period = task.period.value ?: return
        if (date.dateSet) {
            datetimeText += "${twoDigits(date.date.dayOfMonth)}.${twoDigits(date.date.monthValue + 1)}.${date.date.year} "
        }
        if (time.timeSet) {
            datetimeText += "${twoDigits(time.time.hour)}:${twoDigits(time.time.minute)} "
        }
        if (datetimeText.isEmpty()) {
            holder.taskDatetimeView.visibility = View.GONE
        } else {
            datetimeText += "(${context.resources.getString(period.nameRes)})"
            holder.taskDatetimeView.text = datetimeText
            holder.taskDatetimeView.visibility = View.VISIBLE
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

    private fun twoDigits(number: Int): String =
        if (number < 10) "0$number" else number.toString()

    companion object {
        private const val STROKE_WIDTH = 8
    }
}