package com.mirage.todolist.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TaskEntity::class, TagEntity::class, TaskTagEntity::class], version = 2)
@TypeConverters(UUIDConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getTaskDao(): TaskDao

    abstract fun getTagDao(): TagDao

    abstract fun getTaskTagDao(): TaskTagDao
}