package com.mirage.todolist

import android.app.Application
import com.mirage.todolist.di.AppComponent
import com.mirage.todolist.di.DaggerAppComponent
import timber.log.Timber

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = buildComponent()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun buildComponent(): AppComponent =
        DaggerAppComponent.builder()
            .withApplication(this)
            .build()
}