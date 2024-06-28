//package com.samy.ganna.utils
//
//
//import android.Manifest
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.TaskStackBuilder
//import android.content.ContentResolver
//import android.content.Context
//import android.content.Context.NOTIFICATION_SERVICE
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.media.AudioAttributes
//import android.media.AudioManager
//import android.media.RingtoneManager
//import android.net.Uri
//import android.os.Build
//import android.provider.Settings
//import android.widget.RemoteViews
//import androidx.annotation.RequiresApi
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.content.ContextCompat.getSystemService
//import androidx.core.content.ContextCompat.startActivity
//import com.samy.ganna.R
//import com.samy.ganna.data.BookServices
//import com.samy.ganna.ui.main.MainActivity
//import com.samy.ganna.ui.splashscreen.SplashScreenActivity
//import com.samy.ganna.utils.Utils.replaceArabicNumbers
//import java.util.Calendar
//
//
//object Test {
//    private val CHANNEL_ID = "default_notification_channel"
//    private val NOTIFICATION_ID = 1000
//
//    // Vibration pattern
//    private val VIBRATION_PATTERN =
//        longArrayOf(0, 500, 1000, 500) // Vibrate for 500ms, wait for 1000ms, vibrate for 500ms
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun createNotification(context: Context, s: String) {
//
//        val getPage: Pair<Int, String> = getTitlePage(context)
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        // Create the notification channel for API 26+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val audioAttributes = AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
//
//            val channel = NotificationChannel(
//                CHANNEL_ID, "Daily Notification",
//                NotificationManager.IMPORTANCE_DEFAULT
//            ).apply {
//                setSound(getCustomSoundUri(context), audioAttributes)
//                enableVibration(true)
//                vibrationPattern = VIBRATION_PATTERN
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//        val notificationIntent = Intent(context, SplashScreenActivity::class.java).apply {
//            putExtra(NotificationUtils.EXTRA_NOTIFICATION, getPage.first)
//        }
//
//
//        val stackBuilder = TaskStackBuilder.create(context)
//        stackBuilder.addNextIntentWithParentStack(notificationIntent)
//        val pendingIntent = stackBuilder.getPendingIntent(
//            0,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.drawable.tree)  // require
//            .setCustomContentView(prepareCustomView(context,s,getPage.second))
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)// delete when clicked
//            .setVibrate(VIBRATION_PATTERN)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setSound(getCustomSoundUri(context), AudioManager.STREAM_NOTIFICATION)
//
//        builder.notification.flags.and(Notification.FLAG_AUTO_CANCEL)// delete when clicked
//        notificationManager.notify(NOTIFICATION_ID, builder.build())
//    }
//
//    private fun prepareCustomView(context: Context, s: String,getPageTitle: String): RemoteViews? {
//
//        val contentView =  RemoteViews(context.packageName, R.layout.notification_custom_layout) as RemoteViews
//        contentView.setTextViewText(R.id.textTitle, s)
//        contentView.setTextViewText(R.id.textMessage, getPageTitle)
//        contentView.setTextViewText(
//            R.id.time,
//            "${
//                (Calendar.getInstance()[Calendar.HOUR_OF_DAY] % 12).toString()
//                    .replaceArabicNumbers()
//            }:${(Calendar.getInstance()[Calendar.MINUTE]).toString().replaceArabicNumbers()}"
//        )
//        return contentView
//    }
//
//    private fun getTitlePage(context: Context): Pair<Int, String> {
//
//        var num =
//            Utils.getSharedPreferencesInt(context, Constants.GANNA, Constants.LASTINDEXREAD, 0)
//        Utils.setSharedPreferencesInt(context, Constants.GANNA, Constants.LASTINDEXREAD, num + 1)
//        if (num >= 59) {
//            Utils.setSharedPreferencesInt(context, Constants.GANNA, Constants.LASTINDEXREAD, 1)
//            num = 1
//        }
//        var title = BookServices().getBook().arr[num + 1].title
//        title = title.substring(0, title.length - 2)
//        if (title[2] == '-') {
//            title = title.substring(3, title.length)
//        } else {
//            title = title.substring(2, title.length)
//        }
//        //if title.lenth > 50 ---> 47+...
//        if (title.length > 45) {
//            title = title.substring(0, 42) + "..."
//        }
//        return Pair(num + 1, title)
//    }
//    private fun getCustomSoundUri(context: Context): Uri {
//        return Uri.parse("android.resource://${context.packageName}/" + R.raw.notify)
//    }
//
//}