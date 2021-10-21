package com.mirage.todolist.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Entity for storing database snapshot metadata
 */
@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey
    @ColumnInfo(name = "meta_key")
    val key: String,
    @ColumnInfo(name = "meta_value")
    val value: String
) {
    companion object {
        /** UUID version of local database snapshot. Changed after each database update. */
        const val DATA_VERSION_KEY = "data_version"
        /**
         * True if this database version has been recently updated by sync worker and not yet processed
         * false if it has already been read into memory cache by the application,
         * */
        const val MUST_BE_PROCESSED_KEY = "must_be_processed"
    }
}