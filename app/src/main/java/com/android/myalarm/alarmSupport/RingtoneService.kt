package com.android.myalarm.alarmSupport

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder

class RingtoneService : Service() {

    var ringtonePlayer: Ringtone? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent? , flags : Int, startId : Int) : Int
    {
        //Receive alarm sound
        val ringtoneString : String? = intent?.extras?.getString("ringtone_selected")
        val ringtone = Uri.parse(ringtoneString)

        //activating alarm sound.
        //Sets universal scope for ringtonePlayer to be able to cancel later
        ringtonePlayer = RingtoneManager.getRingtone(baseContext, ringtone)

        //local scoop variable for a ringtone to play. Had to do due to some bullshit with null and
        //mutability. if need reminder remove this variable and replace with the ringtonePlayer var
        val localRingtonePlayerVar = ringtonePlayer
        //playing sound alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            localRingtonePlayerVar?.audioAttributes = audioAttributes
            localRingtonePlayerVar?.isLooping = true
            localRingtonePlayerVar?.play()

        } else {
            localRingtonePlayerVar?.setStreamType(AudioManager.STREAM_ALARM)
            localRingtonePlayerVar?.play()
        }
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        ringtonePlayer?.stop()
    }
}