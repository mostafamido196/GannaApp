package com.samy.ganna.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.samy.ganna.R
import com.samy.ganna.data.BookServices
import com.samy.ganna.ui.splashscreen.SplashScreenActivity
import com.samy.ganna.utils.Utils.myLog
import java.time.LocalDateTime
import java.util.Calendar

object NotificationUtils {
    private const val CHANNEL_ID = "daily_notification_channel"
    private const val NOTIFICATION_ID = 1
    const val EXTRA_NOTIFICATION = "com.example.EXTRA_NOTIFICATION"

    // Vibration pattern
    private val VIBRATION_PATTERN =
        longArrayOf(0, 500, 1000, 500) // Vibrate for 500ms, wait for 1000ms, vibrate for 500ms

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(context: Context, s: String) {
        val getPage: Pair<Int, String> = getTitlePage(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(getCustomSoundUri(context), audioAttributes)
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, SplashScreenActivity::class.java).apply {
            putExtra(EXTRA_NOTIFICATION, getPage.first)
        }
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(notificationIntent)
        val pendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.tree)
            .setContentTitle(s)
            .setContentText(getPage.second)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(getCustomSoundUri(context))
            .setVibrate(VIBRATION_PATTERN)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun getTitlePage(context: Context): Pair<Int, String> {
        var num =
            Utils.getSharedPreferencesInt(context, Constants.GANNA, Constants.LASTINDEXREAD, 0)
        Utils.setSharedPreferencesInt(context, Constants.GANNA, Constants.LASTINDEXREAD, num + 1)
        if (num >= 59) {
            Utils.setSharedPreferencesInt(context, Constants.GANNA, Constants.LASTINDEXREAD, 1)
            num = 1
        }
        var title = BookServices().getBook().arr[num + 1].title
        title = title.substring(0, title.length - 2)
        if (title[2] == '-') {
            title = title.substring(3, title.length)
        } else {
            title = title.substring(2, title.length)
        }
        //if title.lenth > 50 ---> 47+...
        if (title.length > 50) {
            title = title.substring(0, 47) + "..."
        }
        return Pair(num + 1, title)
    }

    private fun getCustomSoundUri(context: Context): Uri {
        return Uri.parse("android.resource://${context.packageName}/${R.raw.notifi}")
    }
}
