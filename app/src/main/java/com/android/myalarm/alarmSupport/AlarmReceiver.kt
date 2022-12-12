package com.android.myalarm.alarmSupport

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import com.android.myalarm.AlarmSetOffActivity
import com.android.myalarm.R
import com.android.myalarm.database.AlarmType

class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {

        val volume = intent?.extras?.getFloat("volume")
        val vibrate = intent?.extras?.getBoolean("vibrate")!!
        val alarmTitle = intent.extras?.getString("alarm_title")!!
        val ringtone = Uri.parse(intent.extras?.getString("ringtone"))
        val alarmType = intent.extras!!.getString("type", AlarmType.Regular.name)

        var alarmUri = ringtone
        if (ringtone == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        val ringtoneServiceIntent = Intent(context, RingtoneService::class.java)
        ringtoneServiceIntent.putExtra("ringtone_selected", ringtone.toString())
        ringtoneServiceIntent.putExtra("volume_selected", volume)
        context?.startService(ringtoneServiceIntent)

        if (context != null) {
            // this don't work
            VibrationControl.vibrationConfiguration(context, vibrate)
            //AudioControl.playAudio(context, alarmUri)
            createAlarmNotification(context, alarmTitle, alarmType)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createAlarmNotification(context: Context, alarmTitle: String, alarmType: String) {

        // instead of launching an activity, you can do a variety of other things such as start
        // a BroadcastReceiver that performs a job in the background so the action does not
        // interrupt the app that's already open.
//        val snoozeIntent = Intent(context, NotificationResponseReceiver::class.java).apply {
//            action = context.getString(R.string.snooze)
//            putExtra(EXTRA_NOTIFICATION_ID, 0)
//        }

//        val snoozePendingIntent: PendingIntent =
//            PendingIntent.getBroadcast(context, 0, snoozeIntent, 0)

        // Full Screen Intent
        // When the notification is invoked, users see one of the following, depending on the device's lock status:
        // If the user's device is locked, a full-screen activity appears, covering the lockscreen.
        // If the user's device is unlocked, the notification appears in an expanded form that includes options for handling or dismissing the notification.
        val fullScreenIntent = Intent(context, AlarmSetOffActivity::class.java)
        fullScreenIntent.putExtra("type", alarmType)

        // Set the flags for the intent
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0,
            fullScreenIntent,PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(context, "alarms_notification")
            .setSmallIcon(R.drawable.ic_baseline_alarm_on_24)
            .setContentTitle(alarmTitle)
            // TODO: Provide Alarm time
            .setContentText(alarmType)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .addAction(
//                R.drawable.ic_baseline_snooze_24, context.getString(R.string.snooze),
//                snoozePendingIntent
//            )
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(VISIBILITY_PUBLIC)
            .setAutoCancel(true)

//        // Remember to save the notification ID that you pass to NotificationManagerCompat.notify()
//        // because you'll need it later if you want to update or remove the notification.
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(0, builder.build())
        }
    }
}