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
class RelationDaoTest {

    @Inject
    lateinit var database: AppDatabase
    @Inject
    lateinit var taskDao: TaskDao
    @Inject
    lateinit var tagDao: TagDao
    @Inject
    lateinit var relationDao: RelationDao

    private val tagOne = TagEntity(
        tagId = UUID.randomUUID(),
        accountName = "test@example.com",
        tagIndex = 0,
        name = "Tag0",
        styleIndex = 0,
        deleted = false
    )
    private val tagTwo = TagEntity(
        tagId = UUID.randomUUID(),
        accountName = "test@example.com",
        tagIndex = 1,
        name = "Tag1",
        styleIndex = 1,
        deleted = false
    )
    private val deletedTag = TagEntity(
        tagId = UUID.randomUUID(),
        accountName = "test@example.com",
        tagIndex = 1,
        name = "Tag2",
        styleIndex = 2,
        deleted = true
    )
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
    private val testTags = listOf(tagOne, tagTwo, deletedTag)
    private val testTasks = listOf(taskOne, taskTwo)

    private val relationOne = RelationEntity(
        taskId = taskOne.taskId,
        tagId = tagOne.tagId,
        accountName = taskOne.accountName,
        deleted = false
    )
    private val relationTwo = RelationEntity(
        taskId = taskTwo.taskId,
        tagId = tagOne.tagId,
        accountName = taskTwo.accountName,
        deleted = false
    )

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
        taskDao.insertAllTasks(testTasks)
        tagDao.insertAllTags(testTags)
    }

    @After
    fun clear() {
        taskDao.clear()
        assertThat(taskDao.getAllTasks()).isEmpty()
        tagDao.clear()
        assertThat(tagDao.getAllTags()).isEmpty()
        relationDao.clear()
        assertThat(relationDao.getAllRelations()).isEmpty()
    }

    @Test
    fun testInsertion() {
        with(relationDao) {
            assertThat(getAllRelations()).isEmpty()
            insertRelation(relationOne)
            assertThat(getAllRelations()).hasSize(1)
            insertRelation(relationTwo)
            assertThat(getAllRelations()).hasSize(2)
            insertRelation(relationTwo)
            assertThat(getAllRelations()).hasSize(2)
            removeAllRelations("prod@company.org")
            assertThat(getAllRelations()).hasSize(2)
            removeAllRelations("test@example.com")
            assertThat(getAllRelations()).isEmpty()
        }
    }

    @Test
    fun testUpdates() {
        with(relationDao) {
            assertThat(getAllRelations()).isEmpty()
            insertAllRelations(listOf(relationOne, relationTwo))
            assertThat(getAllRelations()).hasSize(2)
            val instant = Clock.systemUTC().instant()
            setRelationModifiedTime(taskOne.taskId, tagTwo.tagId, instant)
            assertThat(getRelation(taskOne.taskId, tagOne.tagId).lastModified).isNotEqualTo(instant)
            setRelationModifiedTime(taskOne.taskId, tagOne.tagId, instant)
            assertThat(getRelation(taskOne.taskId, tagOne.tagId).lastModified).isEqualTo(instant)
        }
    }

    @Test
    fun testDeletion() {
        with(relationDao) {
            assertThat(getAllRelations()).isEmpty()
            insertAllRelations(listOf(relationOne, relationTwo))
            assertThat(getAllRelations()).hasSize(2)
            assertThat(checkRelation(taskOne.taskId, tagTwo.tagId)).isEqualTo(0)
            assertThat(checkRelation(taskOne.taskId, tagOne.tagId)).isEqualTo(1)
            deleteRelation(taskOne.taskId, tagOne.tagId)
            assertThat(getRelation(taskOne.taskId, tagOne.tagId).deleted).isTrue()
            restoreRelation(taskOne.taskId, tagOne.tagId)
            assertThat(getRelation(taskOne.taskId, tagOne.tagId).deleted).isFalse()
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