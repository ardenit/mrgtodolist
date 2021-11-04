package com.mirage.todolist.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.time.Instant
import java.util.*

@Dao
interface RelationDao {

    @Insert(onConflict = REPLACE)
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
        SET last_modified = :lastModified
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun setRelationModifiedTime(taskId: UUID, tagId: UUID, lastModified: Instant)

    @Query("""
        SELECT *
        FROM relations
        WHERE task_id = :taskId AND tag_id = :tagId
    """)
    fun getRelation(taskId: UUID, tagId: UUID): RelationEntity

    @Query("SELECT * FROM relations")
    fun getAllRelations(): List<RelationEntity>

    @Query("""
        DELETE FROM relations
        WHERE account_name = :email
    """)
    fun removeAllRelations(email: String)

    @Query("""
        DELETE FROM relations
        WHERE 1
    """)
    fun clear()

    @Insert(onConflict = REPLACE)
    fun insertAllRelations(relations: List<RelationEntity>)
}