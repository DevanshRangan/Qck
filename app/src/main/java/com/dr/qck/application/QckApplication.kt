package com.dr.qck.application

import android.app.Application
import android.graphics.Bitmap
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QckApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var isThemeSwitched = Pair(false, "light")
        private lateinit var instance: QckApplication
        var snapshot: Bitmap? = null
    }
}