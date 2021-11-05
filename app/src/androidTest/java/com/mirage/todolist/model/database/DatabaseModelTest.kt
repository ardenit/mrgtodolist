package com.mirage.todolist.model.database

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.mirage.todolist.R
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import com.mirage.todolist.model.repository.TaskPeriod
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class DatabaseModelTest {

    @Inject
    lateinit var taskDao: TaskDao
    @Inject
    lateinit var tagDao: TagDao
    @Inject
    lateinit var relationDao: RelationDao
    @Inject
    lateinit var versionDao: VersionDao
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var resources: Resources
    @Inject
    lateinit var databaseModel: DatabaseModel

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
    }

    @After
    fun clear() {
        taskDao.clear()
        assertThat(taskDao.getAllTasks()).isEmpty()
        tagDao.clear()
        assertThat(tagDao.getAllTags()).isEmpty()
        relationDao.clear()
        assertThat(relationDao.getAllRelations()).isEmpty()
        versionDao.clear()
        assertThat(versionDao.getAllVersions()).isEmpty()
    }

    @Test
    fun testDatabaseSnapshot() {
        taskDao.insertAllTasks(testTasks)
        tagDao.insertAllTags(testTags)
        relationDao.insertAllRelations(testRelations)
        versionDao.insertAllVersions(testVersions)
        val snapshot = runBlocking { databaseModel.getDatabaseSnapshot() }
        assertThat(snapshot.tasks).containsExactlyElementsIn(testTasks)
        assertThat(snapshot.tags).containsExactlyElementsIn(testTags)
        assertThat(snapshot.relations).containsExactlyElementsIn(testRelations)
        assertThat(snapshot.versions).containsExactlyElementsIn(testVersions)
    }

    @Test
    fun testCreateTask() = runBlocking {
        with(databaseModel) {
            val (taskIdOne, jobOne) = createNewTask(0)
            Timber.v("Joining the job")
            jobOne.join()
            Timber.v("Job finished")
            assertThat(taskDao.getAllTasks()).hasSize(1)
            Timber.v("Job finished123")
            assertThat(taskDao.getAllTasks(testEmailOne)).hasSize(1)
            val (taskIdTwo, jobTwo) = createNewTask(0)
            jobTwo.join()
            assertThat(taskDao.getAllTasks(testEmailOne)).hasSize(2)
            assertThat(taskDao.getAllTasks(testEmailTwo)).hasSize(0)
            val (taskIdThree, jobThree) = createNewTask(1)
            jobThree.join()
            assertThat(taskDao.getTaskIndex(taskIdOne)).isEqualTo(0)
            assertThat(taskDao.getTaskIndex(taskIdTwo)).isEqualTo(1)
            assertThat(taskDao.getTaskIndex(taskIdThree)).isEqualTo(0)
        }
    }

    @Test
    fun testTaskUpdate() = runBlocking {
        taskDao.insertAllTasks(testTasks)
        with(databaseModel) {
            setTaskTitle(taskOne.taskId, "TITLE").join()
            setTaskDescription(taskTwo.taskId, "DESC").join()
            setTaskPeriod(taskOne.taskId, TaskPeriod.YEARLY).join()
            assertThat(taskDao.getTask(taskOne.taskId).title).isEqualTo("TITLE")
            assertThat(taskDao.getTask(taskTwo.taskId).description).isEqualTo("DESC")
            assertThat(taskDao.getTask(taskOne.taskId).period).isEqualTo(TaskPeriod.YEARLY)
        }
    }

    @Test
    fun testTaskMoveInList() = runBlocking {
        val tasks = (0 until 10).map {
            TaskEntity(
                taskId = UUID.randomUUID(),
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = it
            )
        }.toMutableList()
        taskDao.insertAllTasks(tasks)
        with(databaseModel) {
            tasks.forEachIndexed { index, task ->
                assertThat(taskDao.getTask(task.taskId).taskIndex).isEqualTo(index)
            }
            var movedTask = tasks[5]
            moveTaskInList(movedTask.taskId, 2).join()
            tasks.removeAt(5)
            tasks.add(2, movedTask)
            tasks.forEachIndexed { index, task ->
                assertThat(taskDao.getTask(task.taskId).taskIndex).isEqualTo(index)
            }
            movedTask = tasks[7]
            moveTaskInList(movedTask.taskId, 9)
            tasks.removeAt(7)
            tasks.add(9, movedTask)
            tasks.forEachIndexed { index, task ->
                assertThat(taskDao.getTask(task.taskId).taskIndex).isEqualTo(index)
            }
        }
    }

    @Test
    fun testTaskMove() = runBlocking {
        val tasksOne = (0 until 10).map {
            TaskEntity(
                taskId = UUID.randomUUID(),
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = it
            )
        }.toMutableList()
        taskDao.insertAllTasks(tasksOne)
        val tasksTwo = (0 until 5).map {
            TaskEntity(
                taskId = UUID.randomUUID(),
                accountName = testEmailOne,
                tasklistId = 1,
                taskIndex = it
            )
        }.toMutableList()
        taskDao.insertAllTasks(tasksTwo)
        with(databaseModel) {
            var movedTask = tasksOne[5]
            moveTask(movedTask.taskId, 1)
            tasksOne.removeAt(5)
            val newTask = TaskEntity(
                taskId = movedTask.taskId,
                accountName = movedTask.accountName,
                tasklistId = 1,
                taskIndex = 5
            )
            tasksTwo += newTask
            tasksOne.forEachIndexed { index, task ->
                assertThat(taskDao.getTask(task.taskId).taskIndex).isEqualTo(index)
            }
            tasksTwo.forEachIndexed { index, task ->
                assertThat(taskDao.getTask(task.taskId).taskIndex).isEqualTo(index)
            }
            movedTask = tasksTwo[0]
            moveTask(movedTask.taskId, -1)
            tasksTwo.removeAt(0)
            tasksOne.forEachIndexed { index, task ->
                assertThat(taskDao.getTask(task.taskId).taskIndex).isEqualTo(index)
            }
            tasksTwo.forEachIndexed { index, task ->
                assertThat(taskDao.getTask(task.taskId).taskIndex).isEqualTo(index)
            }
            assertThat(taskDao.getTask(movedTask.taskId).taskIndex).isEqualTo(0)
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