package com.dr.qck.application

import android.app.Application
import android.content.Intent
import android.util.Log
import com.dr.qck.service.LifecycleService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QckApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var isThemeSwitched = false
        private lateinit var instance: QckApplication
        fun smsReceiver(enabled: Boolean) {
            when (enabled) {
                true -> {
                    startService()
                }

                else -> {
                    stopService()
                }
            }
        }

        private fun stopService() {
            instance.applicationContext.stopService(
                Intent(
                    instance.applicationContext, LifecycleService::class.java
                )
            )
        }

        fun startService() {
            Log.d("Service", "Started")
            instance.applicationContext.startService(
                Intent(
                    instance.applicationContext, LifecycleService::class.java
                )
            )
        }
    }
}