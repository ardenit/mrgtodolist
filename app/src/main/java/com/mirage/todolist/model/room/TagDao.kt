package com.mirage.todolist.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface TagDao {

    @Insert
    fun insertTag(tag: TagEntity)

    @Query("SELECT count(*) FROM tags WHERE NOT deleted")
    fun getTagsCount(): Int

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
        SET last_modified = :lastModifiedTimeMillis
        WHERE tag_id = :tagId
        """)
    fun setTagLastModifiedTime(tagId: UUID, lastModifiedTimeMillis: Long)
}