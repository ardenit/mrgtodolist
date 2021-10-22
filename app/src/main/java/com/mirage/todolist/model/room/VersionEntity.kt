package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entity for storing data version of tasklist for a given account email
 */
@Entity(tableName = "versions")
data class VersionEntity(
    @PrimaryKey
    @ColumnInfo(name = "account_name")
    val accountName: String,
    @ColumnInfo(name = "data_version")
    val dataVersion: UUID,
    @ColumnInfo(name = "must_be_processed")
    val mustBeProcessed: Boolean
)