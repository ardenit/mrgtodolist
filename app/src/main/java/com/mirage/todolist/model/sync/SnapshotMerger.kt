package com.mirage.todolist.model.sync

import com.mirage.todolist.model.room.*
import java.util.*

/**
 * Merges two snapshots from local database and remote Google Drive storage
 * Returned snapshot should be stored in both locations
 * If merge is not required, resulted version will be the same as the version of an up-to-date snapshot.
 */
fun mergeSnapshots(databaseSnapshot: DatabaseSnapshot, gDriveSnapshot: DatabaseSnapshot): DatabaseSnapshot {
    val databaseVersion = databaseSnapshot.dataVersion
    val gDriveVersion = gDriveSnapshot.dataVersion
    if (databaseVersion == gDriveVersion) return databaseSnapshot
    val tags = (databaseSnapshot.tags + gDriveSnapshot.tags)
        .groupBy { it.tagId }
        .mapNotNull { (_, tags) -> tags.maxByOrNull { it.lastModifiedTimeMillis } }
        .toSet()
    val tasks = (databaseSnapshot.tasks + gDriveSnapshot.tasks)
        .groupBy { it.taskId }
        .mapNotNull { (_, tasks) -> tasks.maxByOrNull { it.lastModifiedTimeMillis } }
        .toSet()
    val relations = (databaseSnapshot.relations + gDriveSnapshot.relations)
        .groupBy { Pair(it.tagId, it.taskId) }
        .mapNotNull { (_, relations) -> relations.maxByOrNull { it.lastModifiedTimeMillis } }
        .toSet()
    val newVersion = when {
        (tags == gDriveSnapshot.tags &&
                tasks == gDriveSnapshot.tasks &&
                relations == gDriveSnapshot.relations
                ) -> gDriveSnapshot.dataVersion
        (tags == databaseSnapshot.tags &&
                tasks == databaseSnapshot.tasks &&
                relations == databaseSnapshot.relations
                ) -> databaseSnapshot.dataVersion
        else -> UUID.randomUUID()
    }
    val meta = setOf(MetaEntity("data_version", newVersion))
    return DatabaseSnapshot(tasks, tags, relations, meta)
}