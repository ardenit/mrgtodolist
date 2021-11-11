package com.mirage.todolist.di

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.work.Configuration
import androidx.work.WorkManager
import com.mirage.todolist.BuildConfig
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var workerFactory: WorkerFactory

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent = DaggerAppComponent.create()
        appComponent.inject(this)
        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    @VisibleForTesting
    fun setTestComponent(testComponent: AppComponent) {
        appComponent = testComponent
        appComponent.inject(this)
    }

    companion object {
        @JvmStatic
        lateinit var instance: App
    }
}