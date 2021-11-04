package com.mirage.todolist.model.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import com.google.common.truth.Truth.assertThat
import com.mirage.todolist.model.repository.TaskPeriod
import org.junit.*
import java.time.Clock

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    @Inject
    lateinit var database: AppDatabase
    @Inject
    lateinit var taskDao: TaskDao

    private val emails = listOf("", "test@example.com", "prod@company.org")
    private val tasks = emails.flatMap { email ->
        (0 until 30).map {
            TaskEntity(
                taskId = UUID.randomUUID(),
                accountName = email,
                tasklistId = it / 10,
                taskIndex = it % 10
            )
        }
    }.shuffled()
    private val taskOne = TaskEntity(
        taskId = UUID.randomUUID(),
        accountName = "test@example.com",
        tasklistId = 1,
        taskIndex = 0
    )
    private val taskTwo = TaskEntity(
        taskId = UUID.randomUUID(),
        accountName = "test@example.com",
        tasklistId = 1,
        taskIndex = 1
    )

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
    }

    @After
    fun clear() {
        taskDao.clear()
        assertThat(taskDao.getAllTasks()).isEmpty()
    }

    @Test
    fun testInsertion() {
        with(taskDao) {
            assertThat(getAllTasks()).isEmpty()
            insertTask(taskOne)
            assertThat(getAllTasks()).hasSize(1)
            insertTask(taskTwo)
            assertThat(getAllTasks()).hasSize(2)
            insertTask(taskTwo)
            assertThat(getAllTasks()).hasSize(2)
            removeAllTasks("prod@company.org")
            assertThat(getAllTasks()).hasSize(2)
            removeAllTasks("test@example.com")
            assertThat(getAllTasks()).isEmpty()
        }
    }

    @Test
    fun testGetters() {
        with(taskDao) {
            assertThat(getAllTasks()).isEmpty()
            insertAllTasks(listOf(taskOne, taskTwo))
            assertThat(getAllTasks()).hasSize(2)
            assertThat(getTaskIndex(taskTwo.taskId)).isEqualTo(1)
            assertThat(getTasklistSize(0)).isEqualTo(0)
            assertThat(getTaskIndex(taskOne.taskId)).isEqualTo(0)
            assertThat(getTask(taskTwo.taskId)).isEqualTo(taskTwo)
        }
    }

    @Test
    fun testTimeModified() {
        with(taskDao) {
            insertAllTasks(listOf(taskOne, taskTwo))
            val instant = Clock.systemUTC().instant()
            setTimeModifiedInSlice(0, 0, 2, instant)
            assertThat(getTask(taskOne.taskId).lastModified).isNotEqualTo(instant)
            setTimeModifiedInSlice(1, 0, 1, instant)
            assertThat(getTask(taskOne.taskId).lastModified).isEqualTo(instant)
            assertThat(getTask(taskTwo.taskId).lastModified).isNotEqualTo(instant)
            setTimeModifiedInSlice(1, 0, 2, instant)
            assertThat(getTask(taskTwo.taskId).lastModified).isEqualTo(instant)
            val newInstant = Clock.systemUTC().instant()
            setTaskLastModifiedTime(taskOne.taskId, newInstant)
            assertThat(getTask(taskOne.taskId).lastModified).isEqualTo(newInstant)
        }
    }

    @Test
    fun testUpdates() {
        with(taskDao) {
            insertAllTasks(listOf(taskOne, taskTwo))
            setTaskTitle(taskOne.taskId, "Title")
            assertThat(getTask(taskOne.taskId).title).isEqualTo("Title")
            setTaskDescription(taskTwo.taskId, "Description")
            assertThat(getTask(taskTwo.taskId).description).isEqualTo("Description")
            setTaskDescription(taskTwo.taskId, "Desc")
            assertThat(getTask(taskTwo.taskId).description).isEqualTo("Desc")
            setTaskIndex(taskOne.taskId, 2)
            assertThat(getTask(taskOne.taskId).taskIndex).isEqualTo(2)
            setTasklistId(taskOne.taskId, 0)
            assertThat(getTask(taskOne.taskId).tasklistId).isEqualTo(0)
            setTaskPeriod(taskTwo.taskId, TaskPeriod.MONTHLY)
            assertThat(getTask(taskTwo.taskId).period).isEqualTo(TaskPeriod.MONTHLY)
        }
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun init() {
            App.instance.setTestComponent(DaggerTestAppComponent.create())
        }
    }
}