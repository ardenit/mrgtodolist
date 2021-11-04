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
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class DatabaseModelTest {

    @Inject
    lateinit var database: AppDatabase
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

    companion object {
        @BeforeClass
        @JvmStatic
        fun init() {
            App.instance.setTestComponent(DaggerTestAppComponent.create())
        }
    }
}