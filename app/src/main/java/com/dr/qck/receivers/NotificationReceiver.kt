package com.dr.qck.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.dr.qck.database.ExceptionDao
import com.dr.qck.database.ExceptionMessage
import com.dr.qck.datastore.DatastoreRepository
import com.dr.qck.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
        exceptionDao.addToException(
            ExceptionMessage(
                System.currentTimeMillis(), senderName
            )
        )
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Added to Exception!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        lateinit var exceptionDao: ExceptionDao
        lateinit var prefsRepo: DatastoreRepository
    }
}