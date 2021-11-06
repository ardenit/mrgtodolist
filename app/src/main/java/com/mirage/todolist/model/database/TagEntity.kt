package com.mirage.todolist.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Clock
import java.time.Instant
import java.util.*

@Entity(
    tableName = "tags",
    indices = [Index(value = arrayOf("account_name"), unique = false)]
)
data class TagEntity(
    @PrimaryKey
    @ColumnInfo(name = "tag_id")
    val tagId: UUID,
    @ColumnInfo(name = "account_name")
    val accountName: String = "",
    @ColumnInfo(name = "tag_index")
    val tagIndex: Int,
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "style_index")
    val styleIndex: Int = 0,
    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false,
    @ColumnInfo(name = "last_modified")
    val lastModified: Instant = Clock.systemUTC().instant()
)