package com.mty.exptools

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.mty.exptools.coordinator.AutoContinueCoordinator
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExptoolsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        AutoContinueCoordinator.start()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }
}