package com.mirage.todolist.model.dagger

import android.app.Application

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = buildComponent()
    }

    private fun buildComponent(): AppComponent =
        DaggerAppComponent.builder()
            .withApplication(this)
            .build()
}