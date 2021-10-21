package com.mirage.todolist.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface RelationDao {

    @Insert
    fun insertRelation(relation: RelationEntity)

    @Query("""
        SELECT count(*)
        FROM relations
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun checkRelation(taskId: UUID, tagId: UUID): Int

    @Query("""
        UPDATE relations
        SET deleted = 1
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun deleteRelation(taskId: UUID, tagId: UUID)

    @Query("""
        UPDATE relations
        SET deleted = 0
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun restoreRelation(taskId: UUID, tagId: UUID)

    @Query("""
        UPDATE relations
        SET last_modified = :lastModifiedTimeMillis
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun setRelationModifiedTime(taskId: UUID, tagId: UUID, lastModifiedTimeMillis: Long)

    @Query("SELECT * FROM relations")
    fun getAllRelations(): List<RelationEntity>

    @Query("""
        DELETE FROM relations
        WHERE 1
    """)
    fun removeAllRelations()

    @Insert
    fun insertAllRelations(relations: List<RelationEntity>)
}