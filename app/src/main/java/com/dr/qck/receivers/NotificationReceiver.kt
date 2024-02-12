package com.dr.qck.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.dr.qck.application.QckApplication
import com.dr.qck.database.ExceptionDao
import com.dr.qck.database.ExceptionMessage
import com.dr.qck.datastore.DatastoreRepository
import com.dr.qck.utils.Constants
import com.dr.qck.utils.plain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    private lateinit var exceptionDao: ExceptionDao
    private lateinit var prefsRepo: DatastoreRepository
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as QckApplication
        exceptionDao = app.dao
        prefsRepo = app.repo
        val type = intent.getStringExtra(Reader.NOTIF_TYPE)
        val sender = intent.getStringExtra(Reader.SENDER)
        val id = intent.getIntExtra(Reader.NOTIF_ID, 0)
        NotificationManagerCompat.from(context).cancel(id)
        CoroutineScope(Dispatchers.IO).launch {
            when (type) {
                Reader.EXCEPTION -> {
                    addToException(context, sender ?: "")
                }

                Reader.NOTIFICATION -> {
                    turnOffNotifications(context)
                }
            }
        }
    }

    private suspend fun turnOffNotifications(context: Context) {
        prefsRepo.updateKey(Constants.NOTIFICATIONS_ENABLED, false)
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Turned off Notifications!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addToException(context: Context, senderName: String) {
        exceptionDao.getExceptionList().forEach {
            if (senderName.plain() == it.senderName) return
        }
        exceptionDao.addToException(
            ExceptionMessage(
                System.currentTimeMillis(), senderName
            )
        )
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Added to Exception!", Toast.LENGTH_SHORT).show()
        }
    }
}