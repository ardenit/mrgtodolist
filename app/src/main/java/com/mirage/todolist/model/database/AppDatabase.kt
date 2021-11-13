package com.mirage.todolist.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TaskEntity::class, TagEntity::class, RelationEntity::class, VersionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    UUIDConverter::class,
    LocationConverter::class,
    DateConverter::class,
    TimeConverter::class,
    InstantConverter::class,
    TaskPeriodConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getTaskDao(): TaskDao

    abstract fun getTagDao(): TagDao

    abstract fun getRelationDao(): RelationDao

    abstract fun getVersionDao(): VersionDao
}