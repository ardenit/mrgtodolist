package com.mirage.todolist.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskTagDao {

    @Insert
    fun insertRelation(relation: TaskTagEntity)

    @Query("""
        SELECT count(*)
        FROM tasks_x_tags
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast AND tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
    """)
    fun checkRelation(taskIdFirst: Long, taskIdLast: Long, tagIdFirst: Long, tagIdLast: Long): Int

    @Query("""
        UPDATE tasks_x_tags
        SET deleted = 1
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast AND tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
    """)
    fun deleteRelation(taskIdFirst: Long, taskIdLast: Long, tagIdFirst: Long, tagIdLast: Long)

    @Query("""
        UPDATE tasks_x_tags
        SET deleted = 0
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast AND tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
    """)
    fun restoreRelation(taskIdFirst: Long, taskIdLast: Long, tagIdFirst: Long, tagIdLast: Long)

    @Query("""
        UPDATE tasks_x_tags
        SET last_modified = :lastModifiedTimeMillis
        WHERE task_id_first = :taskIdFirst AND task_id_last = :taskIdLast AND tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
    """)
    fun setRelationModifiedTime(taskIdFirst: Long, taskIdLast: Long, tagIdFirst: Long, tagIdLast: Long, lastModifiedTimeMillis: Long)

    @Query("SELECT * FROM tasks_x_tags")
    fun getAllRelations(): List<TaskTagEntity>
}