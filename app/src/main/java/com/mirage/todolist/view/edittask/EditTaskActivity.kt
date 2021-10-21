package com.mirage.todolist.view.edittask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.mirage.todolist.R
import com.mirage.todolist.App
import com.mirage.todolist.model.tasks.*
import com.mirage.todolist.view.settings.showToast
import com.mirage.todolist.view.todolist.tags.TagsView
import java.util.*
import javax.inject.Inject


class EditTaskActivity : AppCompatActivity() {

    @Inject
    lateinit var todolistModel: TodolistModel
    private var initialTask: LiveTask? = null
    private var tasklistID: Int = 1
    private var newTagsList: MutableList<LiveTag> = arrayListOf()
    private var newTaskDate: TaskDate = TaskDate(-1, -1, -1)
    private var newTaskTime: TaskTime = TaskTime(-1, -1)
    private var newTaskPeriod: TaskPeriod = TaskPeriod.NOT_REPEATABLE
    /** Flag to prevent multiple clicks on "Save" button */
    private var savePressed: Boolean = false

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var newTagBtn: Button
    private lateinit var tagsView: TagsView
    private lateinit var dateBtn: Button
    private lateinit var timeBtn: Button
    private lateinit var periodBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).appComponent.inject(this)
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
            newTaskDate = task.date.value ?: TaskDate(-1, -1, -1)
            newTaskTime = task.time.value ?: TaskTime(-1, -1)
            newTaskPeriod = task.period.value ?: TaskPeriod.NOT_REPEATABLE
        }
        tagsView.recreateTags(newTagsList)
        newTagBtn.setOnClickListener {
            if (newTagsList.containsAll(todolistModel.getAllTags().values)) {
                showToast(R.string.edit_task_full_tags_toast)
            }
            else {
                openNewTagDialog()
            }
        }
        dateBtn = findViewById(R.id.edit_task_date_btn)
        timeBtn = findViewById(R.id.edit_task_time_btn)
        periodBtn = findViewById(R.id.edit_task_period_btn)
        dateBtn.setOnClickListener {
            openDateChooserDialog()
        }
        timeBtn.setOnClickListener {
            openTimeChooserDialog()
        }
        periodBtn.setOnClickListener {
            openPeriodChooserDialog()
        }
        updateTooltips()
    }

    private fun openNewTagDialog() {
        val addTagDialog = EditTaskAddTagDialogFragment()
        addTagDialog.tags = todolistModel.getAllTags().values.filterNot {
            it in newTagsList
        }
        addTagDialog.onTagSelected = {
            if (it !in newTagsList) {
                newTagsList += it
                newTagsList.sortBy { tag -> tag.tagIndex }
                tagsView.recreateTags(newTagsList)
            }
            addTagDialog.dismiss()
        }
        addTagDialog.show(supportFragmentManager, "AddTagDialog")
    }

    private fun updateTooltips() {
        val dateText = newTaskDate.let {
            if (it.year < 0 || it.monthOfYear < 0 || it.dayOfMonth < 0) {
                resources.getString(R.string.edit_task_date_not_set)
            }
            else {
                "${twoDigits(it.dayOfMonth)}.${twoDigits(it.monthOfYear + 1)}.${it.year}"
            }
        }
        dateBtn.text = dateText
        val timeText = newTaskTime.let {
            if (it.hour < 0 || it.minute < 0) {
                resources.getString(R.string.edit_task_time_not_set)
            }
            else {
                "${twoDigits(it.hour)}:${twoDigits(it.minute)}"
            }
        }
        timeBtn.text = timeText
        val periodText = resources.getString(newTaskPeriod.nameRes)
        periodBtn.text = periodText
    }

    private fun twoDigits(number: Int): String =
        if (number < 10) "0$number" else number.toString()

    private fun openDateChooserDialog() {
        val dateListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            val newDate = TaskDate(year, month, day)
            newTaskDate = newDate
            updateTooltips()
        }
        val currentTime = Calendar.getInstance()
        val hasDate = newTaskDate.year >= 0 && newTaskDate.monthOfYear >= 0 && newTaskDate.dayOfMonth >= 0
        val dialog = DatePickerDialog(
            this,
            R.style.Theme_TodoApp_EditTaskAlertDialog,
            dateListener,
            if (hasDate) newTaskDate.year else currentTime.get(Calendar.YEAR),
            if (hasDate) newTaskDate.monthOfYear else currentTime.get(Calendar.MONTH),
            if (hasDate) newTaskDate.dayOfMonth else currentTime.get(Calendar.DAY_OF_MONTH),
        )
        dialog.show()
        recolorDialogButtons(dialog)
    }

    private fun openTimeChooserDialog() {
        val timeListener = TimePickerDialog.OnTimeSetListener { datePicker, hour, minute ->
            val newTime = TaskTime(hour, minute)
            newTaskTime = newTime
            updateTooltips()
        }
        val currentTime = Calendar.getInstance()
        val hasTime = newTaskTime.hour >= 0 && newTaskTime.minute >= 0
        val dialog = TimePickerDialog(
            this,
            R.style.Theme_TodoApp_EditTaskAlertDialog,
            timeListener,
            if (hasTime) newTaskTime.hour else currentTime.get(Calendar.HOUR_OF_DAY),
            if (hasTime) newTaskTime.minute else currentTime.get(Calendar.MINUTE),
            true
        )
        dialog.show()
        recolorDialogButtons(dialog)
    }

    private fun openPeriodChooserDialog() {
        val periodListener: (Int) -> Unit = {
            val newPeriod = TaskPeriod.values()[it]
            newTaskPeriod = newPeriod
            updateTooltips()
        }
        val dialog = EditTaskPeriodDialogFragment()
        dialog.onPeriodSelected = periodListener
        dialog.show(supportFragmentManager, "PeriodDialog")
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
            tags = newTagsList,
            date = newTaskDate,
            time = newTaskTime,
            period = newTaskPeriod
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
        alertDialog.show()
        recolorDialogButtons(alertDialog)
    }

    private fun recolorDialogButtons(alertDialog: AlertDialog) {
        val positiveColor = ContextCompat.getColor(this, R.color.light_blue)
        val negativeColor = ContextCompat.getColor(this, R.color.light_orange)
        val neutralColor = ContextCompat.getColor(this, R.color.light_grey)
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(negativeColor)
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(neutralColor)
    }

    private fun recolorDialogButtons(dialog: DatePickerDialog) {
        val positiveColor = ContextCompat.getColor(this, R.color.light_blue)
        val negativeColor = ContextCompat.getColor(this, R.color.light_orange)
        val neutralColor = ContextCompat.getColor(this, R.color.light_grey)
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(negativeColor)
        dialog.getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(neutralColor)
    }

    private fun recolorDialogButtons(dialog: TimePickerDialog) {
        val positiveColor = ContextCompat.getColor(this, R.color.light_blue)
        val negativeColor = ContextCompat.getColor(this, R.color.light_orange)
        val neutralColor = ContextCompat.getColor(this, R.color.light_grey)
        dialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
        dialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(negativeColor)
        dialog.getButton(TimePickerDialog.BUTTON_NEUTRAL).setTextColor(neutralColor)
    }

    override fun onBackPressed() {
        val newTitle = titleInput.text.toString()
        val newDescription = descriptionInput.text.toString()
        val task = initialTask ?: todolistModel.createNewTask(tasklistID)
        //TODO other changes
        if (
            newTitle != task.title.value ||
            newDescription != task.description.value ||
            newTagsList != task.tags.value ||
            newTaskDate != task.date.value ||
            newTaskTime != task.time.value ||
            newTaskPeriod != task.period.value
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