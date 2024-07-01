package com.samy.ganna.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.samy.ganna.R
import com.samy.ganna.data.BookServices
import com.samy.ganna.ui.main.MainActivity
import com.samy.ganna.ui.splashscreen.SplashScreenActivity
import com.samy.ganna.utils.Utils.myLog
import com.samy.ganna.utils.Utils.replaceArabicNumbers
import com.samy.ganna.utils.Utils.setSharedPreferencesInt
import java.util.Calendar

object NotificationUtils {
    private const val CHANNEL_ID = "daily_notification_channel"
    private const val CHANNEL_NAME = "Advice"
    private const val NOTIFICATION_ID = 500
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 111

    const val EXTRA_NOTIFICATION = "com.example.EXTRA_NOTIFICATION"

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateNotification(context: Context, s: String) {
        createNotification(context)
        val builder = handleNotification(context, s)
        showNotification(context, builder)
    }


    private fun createNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "descriptionText"
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleNotification(context: Context, s: String): NotificationCompat.Builder {
        val page = getTitlePage(context)
        // Create an explicit intent for an Activity in your app.
        val intent = Intent(context, SplashScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION, page.first)
//            setSharedPreferencesInt(context,Constants.NOTIFICATION,Constants.ONCLICK_NOTIFICATION_ID,page.first)
            myLog("NotificationUtils:handleNotification:Intent:page.first: ${page.first} ")
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.tree)
            .setCustomContentView(prepareCustomView(context, s, page.second))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        return builder
    }

    private fun showNotification(context: Context, builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(NOTIFICATION_ID, builder.build())
        }


    }

    private fun prepareCustomView(context: Context, s: String, getPageTitle: String): RemoteViews? {

        val contentView =
            RemoteViews(context.packageName, R.layout.notification_custom_layout) as RemoteViews
        contentView.setTextViewText(R.id.textTitle, s)
        contentView.setTextViewText(R.id.textMessage, getPageTitle)
        contentView.setTextViewText(
            R.id.time,
            "${
                (Calendar.getInstance()[Calendar.HOUR_OF_DAY] % 12).toString()
                    .replaceArabicNumbers()
            }:${(Calendar.getInstance()[Calendar.MINUTE]).toString().replaceArabicNumbers()}"
        )
        return contentView
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
        if (title[title.length-1] == '.')
            title = title.substring(0,title.length-1)
        if (title[title.length-1] == ' ')
            title = title.substring(0,title.length-1)

        if (title[2] == '-') {
            title = title.substring(3, title.length)
        } else {
            title = title.substring(2, title.length)
        }
        //if title.lenth > 50 ---> 47+...
        if (title.length > 45) {
            title = title.substring(0, 42) + "..."
        }
        return Pair(num + 1, title)
    }

}
