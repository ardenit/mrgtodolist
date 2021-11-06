package com.mirage.todolist.model.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class RelationDaoTest {

    @Inject
    lateinit var taskDao: TaskDao
    @Inject
    lateinit var tagDao: TagDao
    @Inject
    lateinit var relationDao: RelationDao

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
        taskDao.insertAllTasks(testTasks)
        tagDao.insertAllTags(testTags)
    }

    @After
    fun clear() {
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
            insertAllRelations(testRelations)
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
            insertAllRelations(testRelations)
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