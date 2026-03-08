package com.mty.exptools

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.annotation.RequiresPermission
import com.mty.exptools.coordinator.AutoContinueCoordinator
import com.mty.exptools.coordinator.LiveUpdateCoordinator
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExptoolsApp : Application() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        AutoContinueCoordinator.start()
        LiveUpdateCoordinator.start()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }
}