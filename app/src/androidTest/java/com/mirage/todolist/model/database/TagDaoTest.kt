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
class TagDaoTest {

    @Inject
    lateinit var tagDao: TagDao

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
    }

    @After
    fun clear() {
        tagDao.clear()
        assertThat(tagDao.getAllTags()).isEmpty()
    }

    @Test
    fun testInsertion() {
        with(tagDao) {
            assertThat(getAllTags()).isEmpty()
            insertTag(tagOne)
            assertThat(getAllTags()).hasSize(1)
            insertTag(tagTwo)
            assertThat(getAllTags()).hasSize(2)
            insertTag(tagTwo)
            assertThat(getAllTags()).hasSize(2)
            removeAllTags("prod@company.org")
            assertThat(getAllTags()).hasSize(2)
            removeAllTags("test@example.com")
            assertThat(getAllTags()).isEmpty()
        }
    }

    @Test
    fun testGetters() {
        with(tagDao) {
            assertThat(getAllTags()).isEmpty()
            insertAllTags(testTags)
            assertThat(getAllTags()).hasSize(3)
            assertThat(getActiveTags(testEmailOne)).hasSize(2)
            setTagIndex(tagTwo.tagId, 4)
            assertThat(getTag(tagTwo.tagId).tagIndex).isEqualTo(4)
            setTagDeleted(tagOne.tagId, true)
            assertThat(getTag(tagOne.tagId).deleted).isTrue()
        }
    }

    @Test
    fun testUpdates() {
        with(tagDao) {
            insertAllTags(testTags)
            setTagName(tagOne.tagId, "Name")
            assertThat(getTag(tagOne.tagId).name).isEqualTo("Name")
            setTagIndex(tagOne.tagId, 2)
            assertThat(getTag(tagOne.tagId).tagIndex).isEqualTo(2)
            setTagDeleted(deletedTag.tagId, false)
            assertThat(getTag(tagOne.tagId).deleted).isFalse()
            val instant = Clock.systemUTC().instant()
            setTagLastModifiedTime(deletedTag.tagId, instant)
            assertThat(getTag(deletedTag.tagId).lastModified).isEqualTo(instant)
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