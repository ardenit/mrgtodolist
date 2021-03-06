package com.mirage.todolist.di

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.work.Configuration
import com.mirage.todolist.BuildConfig
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasAndroidInjector, Configuration.Provider {

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
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()

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