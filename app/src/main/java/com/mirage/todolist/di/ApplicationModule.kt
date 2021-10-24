package com.mirage.todolist.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.googledrive.SnapshotMerger
import com.mirage.todolist.model.repository.TodoRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: App) {

    @Provides
    @Singleton
    fun provideApplication(): App = application

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(): Context = application

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideResources(context: Context): Resources =
        context.resources

    @Provides
    @Singleton
    fun provideSnapshotMerger(): SnapshotMerger = SnapshotMerger()

    @Provides
    @Singleton
    fun provideGoogleDriveModel(): GoogleDriveModel = GoogleDriveModel()

    @Provides
    @Singleton
    fun provideTodolistModel() = TodoRepository()

    @Provides
    @Singleton
    fun provideDatabaseModel() = DatabaseModel()

    @Provides
    @Singleton
    fun provideTodoRepository() = TodoRepository()
}