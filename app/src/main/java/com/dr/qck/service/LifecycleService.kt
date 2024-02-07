package com.dr.qck.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.dr.qck.receivers.Reader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LifecycleService : Service() {


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        filter.priority = 1000
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}