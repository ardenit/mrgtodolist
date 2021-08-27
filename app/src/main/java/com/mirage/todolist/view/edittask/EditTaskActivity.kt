package com.mirage.todolist.view.edittask

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.mirage.todolist.R
import com.mirage.todolist.model.tasks.LiveTag
import com.mirage.todolist.model.tasks.LiveTask
import com.mirage.todolist.model.tasks.TodolistModel
import com.mirage.todolist.model.tasks.getTodolistModel
import com.mirage.todolist.view.settings.showToast
import com.mirage.todolist.view.todolist.tags.TagsView
import java.util.*


class EditTaskActivity : AppCompatActivity() {

    //TODO Inject
    private val todolistModel: TodolistModel = getTodolistModel()
    private var initialTask: LiveTask? = null
    private var tasklistID: Int = 1
    private var newTagsList: MutableList<LiveTag> = arrayListOf()
    /** Flag to prevent multiple clicks on "Save" button */
    private var savePressed: Boolean = false

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var newTagBtn: Button
    private lateinit var tagsView: TagsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_task_root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val editorType = intent?.getStringExtra(EDITOR_TYPE_KEY)
        if (editorType == EDITOR_TYPE_CREATE_TASK) {
            supportActionBar?.setTitle(R.string.edit_task_toolbar_create)
        }
        else {
            supportActionBar?.setTitle(R.string.edit_task_toolbar_edit)
            val idString = intent?.getStringExtra(EDITOR_TASK_ID_KEY)
            if (idString != null) {
                initialTask = todolistModel.getAllTasks()[UUID.fromString(idString)]
            }
        }
        tasklistID = intent?.getIntExtra(EDITOR_TASKLIST_ID_KEY, 1) ?: 1
        initTaskEditor()
    }

    private fun initTaskEditor() {
        titleInput = findViewById(R.id.edit_task_title_input)
        descriptionInput = findViewById(R.id.edit_task_description_input)
        newTagBtn = findViewById(R.id.edit_task_tags_new_btn)
        tagsView = findViewById(R.id.edit_task_tags_view)
        tagsView.lifecycleOwner = this
        tagsView.onTagClickListener = { tag ->
            tagsView.removeTag(tag)
            newTagsList.remove(tag)
        }
        val task = initialTask
        if (task != null) {
            titleInput.setText(task.title.value)
            descriptionInput.setText(task.description.value)
            newTagsList = task.tags.value?.toMutableList() ?: arrayListOf()
        }
        tagsView.recreateTags(newTagsList)
        newTagBtn.setOnClickListener {
            //TODO New tag dialog
        }
    }

    private fun saveTask() {
        if (savePressed) return
        savePressed = true
        val newTitle = titleInput.text.toString()
        val newDescription = descriptionInput.text.toString()
        val task = initialTask ?: todolistModel.createNewTask(tasklistID)
        todolistModel.modifyTask(
            taskID = task.taskID,
            title = newTitle,
            description = newDescription,
            tags = newTagsList
        )
        showToast(R.string.edit_task_saved_toast)
        super.onBackPressed()
    }

    private fun openBackConfirmDialog() {
        val alertDialog = AlertDialog.Builder(ContextThemeWrapper(this, R.style.Theme_TodoApp_EditTaskAlertDialog))
            .setTitle(R.string.edit_task_back_confirm_title)
            .setMessage(R.string.edit_task_back_confirm_description)
            .setPositiveButton(R.string.edit_task_back_save_btn) { _, _ ->
                saveTask()
            }
            .setNegativeButton(R.string.edit_task_back_discard_btn) { _, _ ->
                super.onBackPressed()
            }
            .setNeutralButton(R.string.edit_task_back_cancel_btn) { _, _ -> }
            .create()
        val positiveColor = ContextCompat.getColor(this, R.color.light_blue)
        val negativeColor = ContextCompat.getColor(this, R.color.light_orange)
        val neutralColor = ContextCompat.getColor(this, R.color.light_grey)
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(negativeColor)
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(neutralColor)
    }

    override fun onBackPressed() {
        val newTitle = titleInput.text.toString()
        val newDescription = descriptionInput.text.toString()
        val task = initialTask ?: todolistModel.createNewTask(tasklistID)
        //TODO other changes
        if (
            newTitle != task.title.value ||
            newDescription != task.description.value ||
            newTagsList != task.tags.value
        ) {
            openBackConfirmDialog()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_task_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        else if (item.itemId == R.id.edit_task_toolbar_save) {
            saveTask()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        const val EDITOR_TASKLIST_ID_KEY = "tasklist_id"
        const val EDITOR_TASK_ID_KEY = "task_id"
        const val EDITOR_TYPE_KEY = "editor_type"
        const val EDITOR_TYPE_CREATE_TASK = "create_task"
        const val EDITOR_TYPE_EDIT_TASK = "edit_task"
    }
}