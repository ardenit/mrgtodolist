package com.mirage.todolist.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface TaskTagDao {

    @Insert
    fun insertRelation(relation: TaskTagEntity)

    @Query("""
        SELECT count(*)
        FROM tasks_x_tags
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun checkRelation(taskId: UUID, tagId: UUID): Int

    @Query("""
        UPDATE tasks_x_tags
        SET deleted = 1
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun deleteRelation(taskId: UUID, tagId: UUID)

    @Query("""
        UPDATE tasks_x_tags
        SET deleted = 0
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun restoreRelation(taskId: UUID, tagId: UUID)

    @Query("""
        UPDATE tasks_x_tags
        SET last_modified = :lastModifiedTimeMillis
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun setRelationModifiedTime(taskId: UUID, tagId: UUID, lastModifiedTimeMillis: Long)

    @Query("SELECT * FROM tasks_x_tags")
    fun getAllRelations(): List<TaskTagEntity>
}