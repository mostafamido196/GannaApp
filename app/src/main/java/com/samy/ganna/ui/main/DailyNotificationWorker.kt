package com.samy.ganna.ui.main

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.samy.ganna.utils.Utils.myLog
import java.util.concurrent.TimeUnit
import java.util.Calendar
import androidx.work.OneTimeWorkRequest
import com.samy.ganna.utils.NotificationUtils
import com.samy.ganna.utils.NotificationUtils.createNotification


class DailyNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        createNotification(applicationContext,"إقرأ سبباً من أسباب دخول الجنة")
        scheduleNextNotification(applicationContext)
        return Result.success()
    }

    companion object {
        var date = ""
        fun scheduleNextNotification(context: Context) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, Calendar.getInstance()[Calendar.HOUR_OF_DAY])
                set(Calendar.MINUTE, Calendar.getInstance()[Calendar.MINUTE]+1)
                set(Calendar.SECOND, 0)
            }

            var initialDelay = calendar.timeInMillis - System.currentTimeMillis()
            if (initialDelay < 0) {
                initialDelay += TimeUnit.DAYS.toMillis(1)
            }

            val notificationWork = OneTimeWorkRequest.Builder(DailyNotificationWorker::class.java)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(notificationWork)
        }
    }
}

