package com.mirage.todolist.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.mirage.todolist.R
import com.mirage.todolist.model.database.DatabaseModel
import com.mirage.todolist.model.googledrive.GoogleDriveModel
import com.mirage.todolist.model.googledrive.SnapshotMerger
import com.mirage.todolist.model.repository.TodoRepository
import dagger.Module
import dagger.Provides
import org.mockito.Mockito
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
    fun provideSharedPreferences(): SharedPreferences = mock()

    @Provides
    @Singleton
    fun provideResources(): Resources = mock<Resources>().also {
        Mockito.`when`(it.getString(R.string.key_sync_select_acc))
            .thenReturn("test@example.com")
    }

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