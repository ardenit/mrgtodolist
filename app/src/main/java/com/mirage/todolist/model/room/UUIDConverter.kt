package com.mirage.todolist.model.room

import androidx.room.TypeConverter
import java.util.*

/**
 * Class used by Room to store UUID as a string value in database
 */
class UUIDConverter {

    companion object {
        @TypeConverter
        @JvmStatic
        fun fromUUID(uuid: UUID): String = uuid.toString()

        @TypeConverter
        @JvmStatic
        fun uuidFromString(string: String): UUID = UUID.fromString(string)
    }
}