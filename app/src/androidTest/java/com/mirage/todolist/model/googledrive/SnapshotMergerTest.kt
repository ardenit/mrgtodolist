package com.mirage.todolist.model.googledrive

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import com.mirage.todolist.model.database.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber
import java.time.Clock
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class SnapshotMergerTest {

    @Inject
    lateinit var snapshotMerger: SnapshotMerger

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
    }

    @Test
    fun testSnapshotMerger() {
        val instantOne = Clock.systemUTC().instant()
        Thread.sleep(5L)
        val instantTwo = Clock.systemUTC().instant()
        val localTasks = (0 until 5).map {
            TaskEntity(
                taskId = UUID(0, it.toLong()),
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = it,
                title = "LOCAL-$it",
                lastModified = instantOne
            )
        }
        // Removed task 2 (and moved other tasks), did not have task 4, added tasks 5 and 6 with indices 4 and 5
        val remoteTasks = listOf(
            TaskEntity(
                taskId = localTasks[0].taskId,
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = 0,
                title = "UNCHANGED-0",
                lastModified = instantTwo
            ),
            TaskEntity(
                taskId = localTasks[1].taskId,
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = 1,
                title = "UNCHANGED-1",
                lastModified = instantTwo
            ),
            TaskEntity(
                taskId = localTasks[2].taskId,
                accountName = testEmailOne,
                tasklistId = -1,
                taskIndex = 0,
                title = "REMOVED-2",
                lastModified = instantTwo
            ),
            TaskEntity(
                taskId = localTasks[3].taskId,
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = 2,
                title = "MOVED-3",
                lastModified = instantTwo
            ),
            TaskEntity(
                taskId = UUID(0, 5),
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = 3,
                title = "NEW-5",
                lastModified = instantTwo
            ),
            TaskEntity(
                taskId = UUID(0, 6),
                accountName = testEmailOne,
                tasklistId = 0,
                taskIndex = 4,
                title = "NEW-6",
                lastModified = instantTwo
            )
        )
        val localTags = (0 until 4).map {
            TagEntity(
                tagId = UUID(0, it.toLong()),
                accountName = testEmailOne,
                tagIndex = it,
                name = "LOCAL-$it",
                lastModified = instantOne
            )
        }
        // Removed tag 0 (and moved other tags), did not have tag 3, added tag 4 with index 2
        val remoteTags = listOf(
            TagEntity(
                tagId = localTags[0].tagId,
                accountName = testEmailOne,
                tagIndex = 0,
                name = "REMOVED-0",
                deleted = true,
                lastModified = instantTwo
            ),
            TagEntity(
                tagId = localTags[1].tagId,
                accountName = testEmailOne,
                tagIndex = 0,
                name = "UNCHANGED-1",
                lastModified = instantTwo
            ),
            TagEntity(
                tagId = localTags[2].tagId,
                accountName = testEmailOne,
                tagIndex = 1,
                name = "UNCHANGED-2",
                lastModified = instantTwo
            ),
            TagEntity(
                tagId = UUID(0, 4),
                accountName = testEmailOne,
                tagIndex = 2,
                name = "NEW-4",
                lastModified = instantTwo
            )
        )
        val localRelations = listOf(
            0 to 1, 0 to 2,
            1 to 0, 1 to 2, 1 to 3,
            2 to 2,
            4 to 1, 4 to 3
        ).map { (taskIndex, tagIndex) ->
            RelationEntity(
                taskId = localTasks[taskIndex].taskId,
                tagId = localTags[tagIndex].tagId,
                accountName = testEmailOne,
                deleted = false,
                lastModified = instantOne
            )
        }
        // Removed task 2 (and moved other tasks), did not have task 4, added tasks 5 and 6 with indices 4 and 5
        // Removed tag 0 (and moved other tags), did not have tag 3, added tag 4 with index 2
        // Removed relation 1-2
        // Added relation 1-4
        val remoteRelations = listOf(
            0 to 1, 0 to 2,
            1 to 0, 1 to 3
        ).map { (taskIndex, tagIndex) ->
            RelationEntity(
                taskId = remoteTasks[taskIndex].taskId,
                tagId = remoteTags[tagIndex].tagId,
                accountName = testEmailOne,
                deleted = false,
                lastModified = instantOne
            )
        } + RelationEntity(
            taskId = remoteTasks[1].taskId,
            tagId = remoteTags[2].tagId,
            accountName = testEmailOne,
            deleted = true,
            lastModified = instantTwo
        )
        val localVersion = VersionEntity(
            accountName = testEmailOne,
            dataVersion = UUID(0, 0),
            mustBeProcessed = false
        )
        val remoteVersion = VersionEntity(
            accountName = testEmailOne,
            dataVersion = UUID(0, 1),
            mustBeProcessed = false
        )
        val localSnapshot =
            AccountSnapshot(localTasks, localTags, localRelations, localVersion, testEmailOne)
        val remoteSnapshot =
            AccountSnapshot(remoteTasks, remoteTags, remoteRelations, remoteVersion, testEmailOne)
        val resultSnapshot = snapshotMerger.mergeSnapshots(localSnapshot, remoteSnapshot)
        Timber.v("==== TASKS ====")
        Timber.v(resultSnapshot.tasks.sortedBy { it.taskIndex }.map { it.title }.toString())
        Timber.v("==== TAGS ====")
        Timber.v(resultSnapshot.tags.sortedBy { it.tagIndex }.map { it.name }.toString())
        Timber.v("==== RELATIONS ====")
        resultSnapshot.relations.forEach { relation ->
            val task = resultSnapshot.tasks.first { it.taskId == relation.taskId }
            val tag = resultSnapshot.tags.first { it.tagId == relation.tagId }
            Timber.v("${task.title} TO ${tag.name}")
        }
        assertThat(resultSnapshot.tasks).hasSize(7)
        assertThat(resultSnapshot.tags).hasSize(5)
        assertThat(resultSnapshot.relations).hasSize(9)
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun init() {
            App.instance.setTestComponent(DaggerTestAppComponent.create())
        }
    }
}