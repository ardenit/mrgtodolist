package com.mirage.todolist.di

import android.content.Context
import androidx.room.Room
import com.mirage.todolist.model.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(@ApplicationContext val context: Context) {

    @DatabaseInfo
    val databaseName = "mirage_todolist_db"

    @Provides
    @Singleton
    fun provideDatabase() = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @DatabaseInfo
    fun provideDatabaseName() = databaseName

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase) = database.getTaskDao()

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase) = database.getTagDao()

    @Provides
    @Singleton
    fun provideRelationDao(database: AppDatabase) = database.getRelationDao()

    @Provides
    @Singleton
    fun provideMetaDao(database: AppDatabase) = database.getVersionDao()
}