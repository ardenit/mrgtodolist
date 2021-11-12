package com.mirage.todolist.model.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.mirage.todolist.model.repository.TaskPeriod
import com.mirage.todolist.util.OptionalDate
import com.mirage.todolist.util.OptionalTaskLocation
import com.mirage.todolist.util.OptionalTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUUID(string: String): UUID = UUID.fromString(string)
}

class LocationConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromLocation(location: OptionalTaskLocation): String =
        gson.toJson(location)

    @TypeConverter
    fun toLocation(string: String): OptionalTaskLocation =
        gson.fromJson(string, OptionalTaskLocation::class.java)
}

class DateConverter {
    @TypeConverter
    fun fromDate(date: OptionalDate): String =
        if (date.dateSet) date.date.toString() else "null"

    @TypeConverter
    fun toDate(string: String): OptionalDate =
        if (string == "null") OptionalDate.NOT_SET
        else OptionalDate(LocalDate.parse(string), true)
}

class TimeConverter {
    @TypeConverter
    fun fromTime(time: OptionalTime): String =
        if (time.timeSet) time.time.toString() else "null"

    @TypeConverter
    fun toTime(string: String): OptionalTime =
        if (string == "null") OptionalTime.NOT_SET
        else OptionalTime(LocalTime.parse(string), true)
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