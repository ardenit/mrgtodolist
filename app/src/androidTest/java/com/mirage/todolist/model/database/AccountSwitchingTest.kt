package com.mirage.todolist.model.database

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AccountSwitchingTest {

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
    fun testAccountSwitching() = runBlocking {
        val tasksOne = (0 until 5).map {
            TaskEntity(
                taskId = UUID.randomUUID(),
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = it
            )
        }
        val tagsOne = (0 until 5).map {
            TagEntity(
                tagId = UUID.randomUUID(),
                accountName = testEmailOne,
                tagIndex = it
            )
        }
        val relationsOne = listOf(
            0 to 0, 0 to 1, 0 to 2, 0 to 4,
            1 to 1, 1 to 4,
            2 to 4, 2 to 0, 2 to 3
        ).map { (taskIndex, tagIndex) ->
            RelationEntity(
                taskId = tasksOne[taskIndex].taskId,
                tagId = tagsOne[tagIndex].tagId,
                accountName = testEmailOne,
                deleted = false
            )
        }
        val tasksTwo = (0 until 8).map {
            TaskEntity(
                taskId = UUID.randomUUID(),
                accountName = testEmailTwo,
                tasklistId = 0,
                taskIndex = it
            )
        }
        val tagsTwo = (0 until 8).map {
            TagEntity(
                tagId = UUID.randomUUID(),
                accountName = testEmailTwo,
                tagIndex = it
            )
        }
        val relationsTwo = listOf(
            0 to 6, 0 to 4, 0 to 3, 0 to 7, 0 to 5,
            3 to 5, 3 to 4, 3 to 6,
            6 to 2,
            7 to 1, 7 to 3, 7 to 5, 7 to 6
        ).map { (taskIndex, tagIndex) ->
            RelationEntity(
                taskId = tasksTwo[taskIndex].taskId,
                tagId = tagsTwo[tagIndex].tagId,
                accountName = testEmailTwo,
                deleted = false
            )
        }
        taskDao.insertAllTasks(tasksOne + tasksTwo)
        tagDao.insertAllTags(tagsOne + tagsTwo)
        relationDao.insertAllRelations(relationsOne + relationsTwo)
        val snapshots: MutableList<AccountSnapshot> = CopyOnWriteArrayList()
        with(databaseModel) {
            val snapshotHandlerLatch = CountDownLatch(1)
            setOnSyncUpdateListener {
                snapshots += it
                snapshotHandlerLatch.countDown()
            }
            startObservingAccount(testEmailTwo).join()
            snapshots += getAccountSnapshot()
            var updatedVersion = VersionEntity(
                accountName = testEmailTwo,
                dataVersion = UUID.randomUUID(),
                mustBeProcessed = true
            )
            versionDao.insertVersion(updatedVersion)
            snapshotHandlerLatch.await()
            assertThat(snapshots).hasSize(2)
            updatedVersion = VersionEntity(
                accountName = testEmailOne,
                dataVersion = UUID.randomUUID(),
                mustBeProcessed = true
            )
            versionDao.insertVersion(updatedVersion)
            Thread.sleep(40L)
            assertThat(snapshots).hasSize(2)
            startObservingAccount("unknown@email.com").join()
            val snapshot = getAccountSnapshot()
            assertThat(snapshot.tasks).isEmpty()
            assertThat(snapshot.tags).isEmpty()
            assertThat(snapshot.relations).isEmpty()
            assertThat(snapshot.accountName).isEqualTo("unknown@email.com")
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