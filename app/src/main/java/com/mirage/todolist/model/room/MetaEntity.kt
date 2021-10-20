package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entity for storing database metadata (i.e. data version ID)
 */
@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey
    @ColumnInfo(name = "meta_name")
    val name: String,
    @ColumnInfo(name = "meta_value")
    val value: UUID
)