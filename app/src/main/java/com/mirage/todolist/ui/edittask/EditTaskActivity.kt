package com.mirage.todolist.ui.edittask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.mirage.todolist.R
import com.mirage.todolist.databinding.ActivityEditTaskBinding
import com.mirage.todolist.di.App
import com.mirage.todolist.model.repository.*
import com.mirage.todolist.util.OptionalDate
import com.mirage.todolist.util.OptionalTime
import com.mirage.todolist.util.showToast
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.inject.Inject


class EditTaskActivity : AppCompatActivity() {

    @Inject
    lateinit var todoRepository: TodoRepository
    private var initialTask: LiveTask? = null
    private var tasklistID: Int = 1
    private var newTagsList: MutableList<LiveTag> = arrayListOf()
    private var newTaskDate: OptionalDate = OptionalDate.NOT_SET
    private var newTaskTime: OptionalTime = OptionalTime.NOT_SET
    private var newTaskPeriod: TaskPeriod = TaskPeriod.NOT_REPEATABLE
    /** Flag to prevent multiple clicks on "Save" button */
    private var savePressed: Boolean = false

    private var _binding: ActivityEditTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as App).appComponent.inject(this)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_task)
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
                initialTask = todoRepository.getAllTasks()[UUID.fromString(idString)]
            }
        }
        tasklistID = intent?.getIntExtra(EDITOR_TASKLIST_ID_KEY, 1) ?: 1
        initTaskEditor()
    }

    private fun initTaskEditor() {
        val task = initialTask
        if (task != null) {
            binding.editTaskTitleInput.setText(task.title.value)
            binding.editTaskDescriptionInput.setText(task.description.value)
            newTagsList = task.tags.value?.toMutableList() ?: arrayListOf()
            newTaskDate = task.date.value ?: OptionalDate.NOT_SET
            newTaskTime = task.time.value ?: OptionalTime.NOT_SET
            newTaskPeriod = task.period.value ?: TaskPeriod.NOT_REPEATABLE
        }
        with(binding.editTaskTagsView) {
            lifecycleOwner = this@EditTaskActivity
            onTagClickListener = { tag ->
                removeTag(tag)
                newTagsList.remove(tag)
            }
            recreateTags(newTagsList)
        }
        binding.editTaskTagsNewBtn.setOnClickListener {
            if (newTagsList.containsAll(todoRepository.getAllTags().values)) {
                showToast(R.string.edit_task_full_tags_toast)
            }
            else {
                openNewTagDialog()
            }
        }
        binding.editTaskDateBtn.setOnClickListener {
            openDateChooserDialog()
        }
        binding.editTaskTimeBtn.setOnClickListener {
            openTimeChooserDialog()
        }
        binding.editTaskPeriodBtn.setOnClickListener {
            openPeriodChooserDialog()
        }
        updateTooltips()
    }

    private fun openNewTagDialog() {
        val addTagDialog = EditTaskAddTagDialogFragment()
        addTagDialog.tags = todoRepository.getAllTags().values.filterNot {
            it in newTagsList
        }
        addTagDialog.onTagSelected = {
            if (it !in newTagsList) {
                newTagsList += it
                newTagsList.sortBy { tag -> tag.tagIndex }
                binding.editTaskTagsView.recreateTags(newTagsList)
            }
            addTagDialog.dismiss()
        }
        addTagDialog.show(supportFragmentManager, "AddTagDialog")
    }

    private fun updateTooltips() {
        val dateText = if (newTaskDate.dateSet) {
            "${twoDigits(newTaskDate.date.dayOfMonth)}.${twoDigits(newTaskDate.date.monthValue + 1)}.${newTaskDate.date.year}"
        } else {
            resources.getString(R.string.edit_task_date_not_set)
        }
        binding.editTaskDateBtn.text = dateText
        val timeText = if (newTaskTime.timeSet) {
            "${twoDigits(newTaskTime.time.hour)}:${twoDigits(newTaskTime.time.minute)}"
        } else {
            resources.getString(R.string.edit_task_time_not_set)
        }
        binding.editTaskTimeBtn.text = timeText
        val periodText = resources.getString(newTaskPeriod.nameRes)
        binding.editTaskPeriodBtn.text = periodText
    }

    private fun twoDigits(number: Int): String =
        if (number < 10) "0$number" else number.toString()

    private fun openDateChooserDialog() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val newDate = LocalDate.of(year, month, day)
            newTaskDate = OptionalDate(newDate, true)
            updateTooltips()
        }
        val dialog = DatePickerDialog(
            this,
            R.style.Theme_TodoApp_EditTaskAlertDialog,
            dateListener,
            newTaskDate.date.year,
            newTaskDate.date.monthValue,
            newTaskDate.date.dayOfMonth
        )
        dialog.show()
        recolorDialogButtons(dialog)
    }

    private fun openTimeChooserDialog() {
        val timeListener = TimePickerDialog.OnTimeSetListener { datePicker, hour, minute ->
            val newTime = LocalTime.of(hour, minute)
            newTaskTime = OptionalTime(newTime, true)
            updateTooltips()
        }
        val currentTime = Calendar.getInstance()
        val dialog = TimePickerDialog(
            this,
            R.style.Theme_TodoApp_EditTaskAlertDialog,
            timeListener,
            newTaskTime.time.hour,
            newTaskTime.time.minute,
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
        val newTitle = binding.editTaskTitleInput.text.toString()
        val newDescription = binding.editTaskDescriptionInput.text.toString()
        val task = initialTask ?: todoRepository.createNewTask(tasklistID)
        todoRepository.modifyTask(
            taskID = task.taskId,
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
        val newTitle = binding.editTaskTitleInput.text.toString()
        val newDescription = binding.editTaskDescriptionInput.text.toString()
        val task = initialTask ?: todoRepository.createNewTask(tasklistID)
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

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val EDITOR_TASKLIST_ID_KEY = "tasklist_id"
        const val EDITOR_TASK_ID_KEY = "task_id"
        const val EDITOR_TYPE_KEY = "editor_type"
        const val EDITOR_TYPE_CREATE_TASK = "create_task"
        const val EDITOR_TYPE_EDIT_TASK = "edit_task"
    }
}