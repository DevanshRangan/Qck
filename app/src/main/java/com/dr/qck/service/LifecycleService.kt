package com.dr.qck.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.Toast
import com.dr.qck.recievers.Reader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LifecycleService : Service() {

    @Inject
    lateinit var reader: Reader

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(applicationContext, "OTP Service Started", Toast.LENGTH_SHORT).show()
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        filter.priority = 1000
        registerReceiver(reader, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(applicationContext, "OTP Service Stopped", Toast.LENGTH_SHORT).show()
        unregisterReceiver(reader)
    }
}