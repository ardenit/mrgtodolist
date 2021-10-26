package com.mirage.todolist.model.database

import androidx.room.TypeConverter
import com.mirage.todolist.model.repository.TaskPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUUID(string: String): UUID = UUID.fromString(string)
}

class LocalDateTimeConverter {
    @TypeConverter
    fun fromLocalDateTime(datetime: LocalDateTime): String = datetime.toString()

    @TypeConverter
    fun toLocalDateTime(string: String): LocalDateTime = LocalDateTime.parse(string)
}

class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(string: String): LocalDate = LocalDate.parse(string)
}

class LocalTimeConverter {
    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.toString()

    @TypeConverter
    fun toLocalTime(string: String): LocalTime = LocalTime.parse(string)
}

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant): String = instant.toString()

    @TypeConverter
    fun toInstant(string: String): Instant = Instant.parse(string)
}

class TaskPeriodConverter {
    @TypeConverter
    fun fromTaskPeriod(taskPeriod: TaskPeriod): String = taskPeriod.toString()

    @TypeConverter
    fun toTaskPeriod(string: String): TaskPeriod = TaskPeriod.valueOf(string)
}