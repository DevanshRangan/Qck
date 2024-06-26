package com.dr.qck.receivers

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dr.qck.R
import com.dr.qck.activities.MainActivity
import com.dr.qck.application.QckApplication
import com.dr.qck.utils.plain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class Reader : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val app = context.applicationContext as QckApplication
            val exceptionDao = app.dao
            val datastoreRepository = app.repo
            CoroutineScope(Dispatchers.IO).launch {
                val prefs = datastoreRepository.userPreferencesFlow.first()
                if (prefs.isEnabled) {
                    val bundle = intent.extras
                    bundle?.let { b ->
                        val pdus = b["pdus"] as Array<*>
                        for (pdu in pdus) {
                            val message = SmsMessage.createFromPdu(
                                pdu as ByteArray, bundle.getString("format")
                            )
                            Log.d("Message>>>", message.displayMessageBody.toString())
                            if (message.messageClass != SmsMessage.MessageClass.CLASS_0) {
                                extractOTP(message.displayMessageBody.lowercase())?.let { otp ->
                                    if (exceptionDao.getExceptionList().find { address ->
                                            address.senderName.plain().equals(message.originatingAddress, true)
                                        } == null) {
                                        copyOTP(otp, context).also {
                                            if (prefs.notificationsEnabled) {
                                                showNotification(
                                                    message.originatingAddress.toString(),
                                                    otp,
                                                    context
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showNotification(from: String, otp: String, context: Context) {
        Log.d("Sender & OTP", "$from $otp")
        val notifID = System.currentTimeMillis().toInt()
        Log.d("Generated", notifID.toString())
        val exceptionIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NOTIF_ID, notifID)
            putExtra(NOTIF_TYPE, EXCEPTION)
            putExtra(SENDER, from)
        }
        val exceptionPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context, notifID, exceptionIntent, PendingIntent.FLAG_MUTABLE
        )
        val notificationsIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NOTIF_ID, notifID)
            putExtra(NOTIF_TYPE, NOTIFICATION)
        }
        val notifPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context, notifID, notificationsIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent =
            PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE)
        val builder =
            NotificationCompat.Builder(context, "1.0").setContentTitle("OTP Detected from $from")
                .setSmallIcon(R.drawable.ic_notification_ic)
                .setContentText("$otp was copied to device's clipboard")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.forbidden_ic, "Add to Exception", exceptionPendingIntent)
                .addAction(R.drawable.bell_ic, "Disable Notifications", notifPendingIntent).build()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(notifID, builder)
    }

    private fun extractOTP(message: String): String? {
        if (message.contains("otp") || message.contains("verification") || message.contains("code")) {
            val regex = Regex("\\b\\d{4,8}\\b")
            val otp = regex.find(message)?.value
            otp?.let {
                message.indexOf(it).let { i ->
                    when {
                        i == 0 || message[i - 1] == ' ' && message[i - 1] == ' ' -> {
                            return it
                        }

                        else -> return null
                    }
                }
            } ?: return null
        } else {
            return null
        }
    }

    private fun copyOTP(otp: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(SIMPLE_TEXT, otp)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        const val SIMPLE_TEXT = "simple text"
        const val EXCEPTION = "exception_intent"
        const val NOTIFICATION = "notification_intent"
        const val SENDER = "message_sender"
        const val NOTIF_TYPE = "notification_type"
        const val NOTIF_ID = "notification_id"
    }
}
