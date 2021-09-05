package com.mirage.todolist.model.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

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
        WHERE tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
        """)
    fun setTagName(tagIdFirst: Long, tagIdLast: Long, name: String)

    @Query("""
        UPDATE tags
        SET style_index = :styleIndex
        WHERE tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
        """)
    fun setTagStyleIndex(tagIdFirst: Long, tagIdLast: Long, styleIndex: Int)

    @Query("""
        UPDATE tags
        SET deleted = :deleted
        WHERE tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
        """)
    fun setTagDeleted(tagIdFirst: Long, tagIdLast: Long, deleted: Boolean)

    @Query("""
        UPDATE tags
        SET last_modified = :lastModifiedTimeMillis
        WHERE tag_id_first = :tagIdFirst AND tag_id_last = :tagIdLast
        """)
    fun setTagLastModifiedTime(tagIdFirst: Long, tagIdLast: Long, lastModifiedTimeMillis: Long)
}