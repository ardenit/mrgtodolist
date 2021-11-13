package com.mirage.todolist.model.googledrive

import com.mirage.todolist.model.database.AccountSnapshot
import com.mirage.todolist.model.database.TagEntity
import com.mirage.todolist.model.database.TaskEntity
import com.mirage.todolist.model.database.VersionEntity
import okhttp3.internal.toImmutableList
import java.util.*

class SnapshotMerger {
    /**
     * Merges two snapshots from local database and remote Google Drive storage.
     * Returned snapshot should be stored in both locations
     * If merge is not required, resulted version will be the same as the version of an up-to-date snapshot.
     */
    fun mergeSnapshots(localSnapshot: AccountSnapshot, remoteSnapshot: AccountSnapshot): AccountSnapshot {
        val localVersion = localSnapshot.version.dataVersion
        val remoteVersion = remoteSnapshot.version.dataVersion
        if (localVersion == remoteVersion) return localSnapshot
        val tagsSlice = (localSnapshot.tags + remoteSnapshot.tags)
            .groupBy { it.tagId }
            .mapNotNull { (_, tags) -> tags.maxByOrNull { it.lastModified } }
            .groupBy { it.deleted }
            .flatMap { (_, tags) -> tags
                .sortedBy { it.tagIndex }
                .mapIndexed { index, tag ->
                    TagEntity(
                        tagId = tag.tagId,
                        accountName = tag.accountName,
                        tagIndex = index,
                        name = tag.name,
                        styleIndex = tag.styleIndex,
                        deleted = tag.deleted,
                        lastModified = tag.lastModified
                    )
                }
            }
        val tasksSlice = (localSnapshot.tasks + remoteSnapshot.tasks)
            .groupBy { it.taskId }
            .mapNotNull { (_, tasks) -> tasks.maxByOrNull { it.lastModified } }
            .groupBy { it.tasklistId }
            .flatMap { (_, tags) -> tags
                .sortedBy { it.taskIndex }
                .mapIndexed { index, task ->
                    TaskEntity(
                        taskId = task.taskId,
                        accountName = task.accountName,
                        tasklistId = task.tasklistId,
                        taskIndex = index,
                        title = task.title,
                        description = task.description,
                        location = task.location,
                        date = task.date,
                        time = task.time,
                        period = task.period,
                        lastModified = task.lastModified
                    )
                }
            }
        val relationsSlice = (localSnapshot.relations + remoteSnapshot.relations)
            .groupBy { Pair(it.tagId, it.taskId) }
            .mapNotNull { (_, relations) -> relations.maxByOrNull { it.lastModified } }
        val newVersion = when {
            (tagsSlice.toSet() == remoteSnapshot.tags.toSet() &&
                    tasksSlice.toSet() == remoteSnapshot.tasks.toSet() &&
                    relationsSlice.toSet() == remoteSnapshot.relations.toSet()
                    ) -> remoteSnapshot.version.dataVersion
            (tagsSlice.toSet() == localSnapshot.tags.toSet() &&
                    tasksSlice.toSet() == localSnapshot.tasks.toSet() &&
                    relationsSlice.toSet() == localSnapshot.relations.toSet()
                    ) -> localSnapshot.version.dataVersion
            else -> UUID.randomUUID()
        }
        val version = VersionEntity(localSnapshot.accountName, newVersion, true)
        return AccountSnapshot(tasksSlice, tagsSlice, relationsSlice, version, localSnapshot.accountName)
    }
}