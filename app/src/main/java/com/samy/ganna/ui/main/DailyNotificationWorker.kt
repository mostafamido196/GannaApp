package com.samy.ganna.ui.main

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.samy.ganna.R
import com.samy.ganna.utils.Constants
import com.samy.ganna.utils.NotificationUtils
import com.samy.ganna.utils.Utils.setSharedPreferencesString
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit


class DailyNotificationWorker(val context: Context, params: WorkerParameters) :
    Worker(context, params) {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        setSharedPreferencesString(
            context,
            Constants.TEST,
            Constants.WORKER,
            "doWork(): month:${Calendar.getInstance()[Calendar.MONTH]}/day:${Calendar.getInstance()[Calendar.DAY_OF_MONTH]}--> hour:${Calendar.getInstance()[Calendar.HOUR_OF_DAY]}/min:${Calendar.getInstance()[Calendar.MINUTE]}/millisecond:${Calendar.getInstance()[Calendar.MILLISECOND]}"
        )
//        myLog("DailyNotificationWorker:doWork()")
//        myLog("hour:$${Calendar.getInstance()[Calendar.HOUR_OF_DAY]}, min:$${Calendar.getInstance()[Calendar.MINUTE]}")
//        myLog("-------------------------------------------")

        NotificationUtils.generateNotification(applicationContext, context.getString(R.string.app_name))
//        scheduleNextNotification(applicationContext)
        return Result.success()
    }

    companion object {
        fun scheduleNextNotification(context: Context) {
            val notificationWork: PeriodicWorkRequest =
                PeriodicWorkRequestBuilder<DailyNotificationWorker>(6, TimeUnit.HOURS)
             .build()

            WorkManager.getInstance(context).enqueue(notificationWork)
        }
    }
}

