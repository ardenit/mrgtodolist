package com.mirage.todolist.model.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
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

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("com.mirage.todolist", appContext.packageName)
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun init() {
            App.instance.setTestComponent(DaggerTestAppComponent.create())
        }
    }
}