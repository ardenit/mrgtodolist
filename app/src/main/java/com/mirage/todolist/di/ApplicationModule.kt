package com.mirage.todolist.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.google.gson.Gson
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.googledrive.SnapshotMerger
import com.mirage.todolist.model.repository.TodoRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Provides
    @Singleton
    fun provideApplication(): App = App.instance

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(application: App): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    fun provideResources(@ApplicationContext context: Context): Resources =
        context.resources

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideGson() = Gson()

    @Provides
    @Singleton
    fun provideSnapshotMerger() = SnapshotMerger()

    @Provides
    @Singleton
    fun provideGoogleDriveModel() = GoogleDriveModel()

    @Provides
    @Singleton
    fun provideDatabaseModel() = DatabaseModel()

    @Provides
    @Singleton
    fun provideTodoRepository() = TodoRepository()
}