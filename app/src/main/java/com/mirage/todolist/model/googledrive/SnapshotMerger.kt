package com.mirage.todolist.model.googledrive

import com.mirage.todolist.model.database.AccountSnapshot
import com.mirage.todolist.model.database.VersionEntity
import java.util.*

class SnapshotMerger {
    /**
     * Merges two snapshots from local database and remote Google Drive storage.
     * Only entities belonging to the given [email] are updated.
     * Returned snapshot should be stored in both locations
     * Returned snapshot only stores entities bound to given [email], so database should not be overridden completely.
     * If merge is not required, resulted version will be the same as the version of an up-to-date snapshot.
     */
    fun mergeSnapshots(localSnapshot: AccountSnapshot, remoteSnapshot: AccountSnapshot): AccountSnapshot {
        val localVersion = localSnapshot.version.dataVersion
        val remoteVersion = remoteSnapshot.version.dataVersion
        if (localVersion == remoteVersion) return localSnapshot
        val tagsSlice = (localSnapshot.tags + remoteSnapshot.tags)
            .groupBy { it.tagId }
            .mapNotNull { (_, tags) -> tags.maxByOrNull { it.lastModified } }
        val tasksSlice = (localSnapshot.tasks + remoteSnapshot.tasks)
            .groupBy { it.taskId }
            .mapNotNull { (_, tasks) -> tasks.maxByOrNull { it.lastModified } }
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