package com.android.myalarm.alarmSupport

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val audioManager: AudioManager =
            context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = intent?.extras?.getFloat("volume")!!
        val vibrate = intent.extras?.getBoolean("vibrate")!!
        val alarmTitle = intent.extras?.getString("alarm_title")!!
        val ringtone = Uri.parse(intent.extras!!.getString("ringtone"))

        var alarmUri = ringtone
        if (ringtone == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        // NOT VERIFIED WORKING, CHECK
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume.toInt(), 0
        )

        VibrationControl.vibrationConfiguration(context, vibrate)
        //AudioControl.playAudio(context, alarmUri)

        //launchFullscreenNotification(context, alarmTitle)

    }

//    private fun launchFullscreenNotification(context: Context, alarmTitle: String) {
//        val notificationBuilder = NotificationCompat.Builder(context, "notification_id")
//        notificationBuilder.setSmallIcon(R.drawable.ic_baseline_alarm_24)
//        notificationBuilder.setContentTitle(alarmTitle)
//        notificationBuilder.setContentText("This is a notification Text")
//        notificationBuilder.setLargeIcon(
//            BitmapFactory.decodeResource(
//                context.resources,
//                R.drawable.ic_launcher_foreground
//            )
//        )
//
//        val intent = Intent(context, NotifyAlarm::class.java)
//        intent.putExtra("alarm_message", alarmTitle)
//        val pendingIntent =
//            PendingIntent.getActivity(
//                context,
//                0,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//
//        notificationBuilder.setContentIntent(pendingIntent)
//        notificationBuilder.setAutoCancel(true)
//
//        notificationBuilder.setFullScreenIntent(pendingIntent, true)
//
//        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
//        manager!!.notify(0, notificationBuilder.build())
//    }
}