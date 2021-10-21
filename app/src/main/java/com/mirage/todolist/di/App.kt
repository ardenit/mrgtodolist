package com.mirage.todolist.di

import android.app.Application
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
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this
        DaggerAppComponent.builder()
            .withApplication(this)
            .build()
            .inject(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    companion object {
        @JvmStatic
        lateinit var instance: App
    }
}