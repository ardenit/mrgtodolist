package com.mirage.todolist.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.work.WorkManager
import com.google.gson.Gson
import com.mirage.todolist.R
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.database.testEmailOne
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.googledrive.SnapshotMerger
import com.mirage.todolist.model.repository.TodoRepository
import dagger.Module
import dagger.Provides
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import javax.inject.Singleton

@Module
class TestApplicationModule {

    @Provides
    @Singleton
    fun provideApplication(): App = App.instance

    @Provides
    @Singleton
    @ApplicationContext
    fun provideApplicationContext(application: App): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences = mock<SharedPreferences>().also {
        Mockito.`when`(it.getString(eq("sync_select_acc"), anyString()))
            .thenReturn(testEmailOne)
    }

    @Provides
    @Singleton
    fun provideResources(): Resources = mock<Resources>().also {
        Mockito.`when`(it.getString(eq(R.string.key_sync_select_acc)))
            .thenReturn("sync_select_acc")
        Mockito.`when`(it.getString(eq(R.string.task_default_title)))
            .thenReturn("Task")
        Mockito.`when`(it.getString(eq(R.string.task_default_description)))
            .thenReturn("Description")
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideGson() = Gson()

    @Provides
    @Singleton
    fun provideSnapshotMerger(): SnapshotMerger = SnapshotMerger()

    @Provides
    @Singleton
    fun provideGoogleDriveModel(): GoogleDriveModel = GoogleDriveModel()

    @Provides
    @Singleton
    fun provideDatabaseModel() = DatabaseModel()

    @Provides
    @Singleton
    fun provideTodoRepository() = TodoRepository()
}