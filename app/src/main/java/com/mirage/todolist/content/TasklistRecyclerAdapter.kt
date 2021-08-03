package com.mirage.todolist.content

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mirage.todolist.R

class TasklistRecyclerAdapter : RecyclerView.Adapter<TasklistRecyclerAdapter.TasklistViewHolder>(){

    inner class TasklistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var taskTitleView: TextView? = null
        var taskDescriptionView: TextView? = null

        init {
            taskTitleView = itemView.findViewById(R.id.task_title)
            taskDescriptionView = itemView.findViewById(R.id.task_description)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasklistViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: TasklistViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

}