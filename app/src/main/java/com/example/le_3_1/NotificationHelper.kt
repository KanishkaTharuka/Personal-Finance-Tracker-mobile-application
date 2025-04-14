package com.example.le_3_1

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {

    private const val CHANNEL_ID = "FinanceTrackerChannel"
    private const val BUDGET_NOTIFICATION_ID = 1
    private const val REMINDER_NOTIFICATION_ID = 2
    private const val DAILY_REMINDER_WORK_NAME = "DailyExpenseReminder"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Finance Tracker Notifications"
            val descriptionText = "Notifications for budget alerts and reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendBudgetNotification(context: Context, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(BUDGET_NOTIFICATION_ID, builder.build())
        }
    }

    fun scheduleDailyReminder(context: Context) {
        val prefs = context.getSharedPreferences("FinanceTrackerPrefs", Context.MODE_PRIVATE)
        val dailyReminderEnabled = prefs.getBoolean("daily_reminder", false)

        val workManager = WorkManager.getInstance(context)

        if (!dailyReminderEnabled) {
            workManager.cancelUniqueWork(DAILY_REMINDER_WORK_NAME)
            return
        }

        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(currentTime)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    class ReminderWorker(appContext: Context, workerParams: androidx.work.WorkerParameters) :
        androidx.work.Worker(appContext, workerParams) {

        override fun doWork(): Result {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }

            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Daily Expense Reminder")
                .setContentText("Don't forget to record your expenses for today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(REMINDER_NOTIFICATION_ID, builder.build())
            }

            return Result.success()
        }
    }
}