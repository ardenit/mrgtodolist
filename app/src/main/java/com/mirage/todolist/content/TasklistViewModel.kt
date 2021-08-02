package com.mirage.todolist.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TasklistViewModel : ViewModel() {

    val type = MutableLiveData<TasklistType>()

}