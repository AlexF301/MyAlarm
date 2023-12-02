package com.android.myalarm.database

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.android.myalarm.R
import com.google.android.material.color.DynamicColors


/**
 * Base Android application class. Responsible for creating Repository to have access to Room
 * persistence library
 */
class MyAlarmApplication : Application() {

    /** Initialize MyAlarmRepository upon app start */
    override fun onCreate() {
        super.onCreate()
        MyAlarmRepository.initialize(this)

        createNotificationChannel()

        DynamicColors.applyToActivitiesIfAvailable(this)
    }


    /**
     * Before you can deliver the notification on Android 8.0 and higher, you must register your
     * app's notification channel with the system by passing an instance of NotificationChannel.
     * It's safe to call this repeatedly because creating an existing notification channel performs
     * no operation.
     */
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Notification.CATEGORY_ALARM
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("alarms_notification", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}