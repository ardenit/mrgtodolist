package com.mirage.todolist.model.database

import androidx.room.TypeConverter
import com.mirage.todolist.model.repository.TaskPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class UUIDConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromUUID(uuid: UUID): String = uuid.toString()

        @TypeConverter
        @JvmStatic
        fun toUUID(string: String): UUID = UUID.fromString(string)
    }
}

class LocalDateTimeConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromLocalDateTime(datetime: LocalDateTime): String = datetime.toString()

        @TypeConverter
        @JvmStatic
        fun toLocalDateTime(string: String): LocalDateTime = LocalDateTime.parse(string)
    }
}

class LocalDateConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromLocalDate(date: LocalDate): String = date.toString()

        @TypeConverter
        @JvmStatic
        fun toLocalDate(string: String): LocalDate = LocalDate.parse(string)
    }
}

class LocalTimeConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromLocalTime(time: LocalTime): String = time.toString()

        @TypeConverter
        @JvmStatic
        fun toLocalTime(string: String): LocalTime = LocalTime.parse(string)
    }
}

class InstantConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromInstant(instant: Instant): String = instant.toString()

        @TypeConverter
        @JvmStatic
        fun toInstant(string: String): Instant = Instant.parse(string)
    }
}

class TaskPeriodConverter {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromTaskPeriod(taskPeriod: TaskPeriod): String = taskPeriod.toString()

        @TypeConverter
        @JvmStatic
        fun toTaskPeriod(string: String): TaskPeriod = TaskPeriod.valueOf(string)
    }
}