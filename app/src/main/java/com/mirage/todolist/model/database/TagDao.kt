package com.mirage.todolist.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import java.time.Instant
import java.util.*

@Dao
interface TagDao {

    @Insert(onConflict = REPLACE)
    fun insertTag(tag: TagEntity)

    @Query("SELECT count(*) FROM tags WHERE NOT deleted")
    fun getTagsCount(): Int

    @Query("""
        SELECT * FROM tags
        WHERE tag_id = :tagId
    """)
    fun getTag(tagId: UUID): TagEntity

    @Query(value = "SELECT * FROM tags")
    fun getAllTags(): List<TagEntity>

    @Query("""
        UPDATE tags
        SET name = :name
        WHERE tag_id = :tagId
        """)
    fun setTagName(tagId: UUID, name: String)

    @Query("""
        UPDATE tags
        SET tag_index = :tagIndex
        WHERE tag_id = :tagId
        """)
    fun setTagIndex(tagId: UUID, tagIndex: Int)

    @Query("""
        UPDATE tags
        SET style_index = :styleIndex
        WHERE tag_id = :tagId
        """)
    fun setTagStyleIndex(tagId: UUID, styleIndex: Int)

    @Query("""
        UPDATE tags
        SET deleted = :deleted
        WHERE tag_id = :tagId
        """)
    fun setTagDeleted(tagId: UUID, deleted: Boolean)

    @Query("""
        UPDATE tags
        SET last_modified = :lastModified
        WHERE tag_id = :tagId
        """)
    fun setTagLastModifiedTime(tagId: UUID, lastModified: Instant)

    @Query("""
        DELETE FROM tags
        WHERE account_name = :email
    """)
    fun removeAllTags(email: String)

    @Query("""
        DELETE FROM tags
        WHERE 1
    """)
    fun clear()

    @Insert(onConflict = REPLACE)
    fun insertAllTags(tags: List<TagEntity>)
}