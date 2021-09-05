package com.mirage.todolist.model.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, TagEntity::class, TaskTagEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getTaskDao(): TaskDao

    abstract fun getTagDao(): TagDao

    abstract fun getTaskTagDao(): TaskTagDao
}