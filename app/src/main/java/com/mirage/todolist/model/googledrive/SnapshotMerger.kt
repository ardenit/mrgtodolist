package com.mirage.todolist.model.googledrive

import com.mirage.todolist.model.room.*
import java.util.*

class SnapshotMerger {
    /**
     * Merges two snapshots from local database and remote Google Drive storage.
     * Only entities belonging to the given [email] are updated.
     * Returned snapshot should be stored in both locations
     * Returned snapshot only stores entities bound to given [email], so database should not be overridden completely.
     * If merge is not required, resulted version will be the same as the version of an up-to-date snapshot.
     */
    fun mergeSnapshots(email: String, databaseSnapshot: DatabaseSnapshot, gDriveSnapshot: DatabaseSnapshot): DatabaseSnapshot {
        val databaseVersion = databaseSnapshot.getDataVersion(email)
        val gDriveVersion = gDriveSnapshot.getDataVersion(email)
        if (databaseVersion == gDriveVersion) return databaseSnapshot
        val tagsSlice = (databaseSnapshot.tags + gDriveSnapshot.tags)
            .filter { it.accountName == email }
            .groupBy { it.tagId }
            .mapNotNull { (_, tags) -> tags.maxByOrNull { it.lastModifiedTimeMillis } }
        val tasksSlice = (databaseSnapshot.tasks + gDriveSnapshot.tasks)
            .filter { it.accountName == email }
            .groupBy { it.taskId }
            .mapNotNull { (_, tasks) -> tasks.maxByOrNull { it.lastModifiedTimeMillis } }
        val relationsSlice = (databaseSnapshot.relations + gDriveSnapshot.relations)
            .filter { it.accountName == email }
            .groupBy { Pair(it.tagId, it.taskId) }
            .mapNotNull { (_, relations) -> relations.maxByOrNull { it.lastModifiedTimeMillis } }
        val newVersion = when {
            (tagsSlice.toSet() == gDriveSnapshot.tags.toSet() &&
                    tasksSlice.toSet() == gDriveSnapshot.tasks.toSet() &&
                    relationsSlice.toSet() == gDriveSnapshot.relations.toSet()
                    ) -> gDriveSnapshot.getDataVersion(email)
            (tagsSlice.toSet() == databaseSnapshot.tags.toSet() &&
                    tasksSlice.toSet() == databaseSnapshot.tasks.toSet() &&
                    relationsSlice.toSet() == databaseSnapshot.relations.toSet()
                    ) -> databaseSnapshot.getDataVersion(email)
            else -> UUID.randomUUID()
        }
        val versions = listOf(VersionEntity(email, newVersion, true))
        return DatabaseSnapshot(tasksSlice, tagsSlice, relationsSlice, versions)
    }
}