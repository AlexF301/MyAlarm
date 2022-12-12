package com.android.myalarm.alarmSupport

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log

class RingtoneService : Service() {

    var ringtonePlayer: Ringtone? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Receive alarm sound
        val ringtoneString: String? = intent?.extras?.getString("ringtone_selected")
        val volumeFloat: Float? = intent?.extras?.getFloat("volume_selected", 0.5f)

        val ringtone =
            if (ringtoneString == null) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            else Uri.parse(ringtoneString)

        //activating alarm sound.
        //Sets universal scope for ringtonePlayer to be able to cancel later
        ringtonePlayer = RingtoneManager.getRingtone(baseContext, ringtone)

        //local scope variable for a ringtone to play. Had to do due to some bullshit with null and
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
            if (volumeFloat != null) {
                localRingtonePlayerVar?.volume = volumeFloat
            }
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

    fun stop() {
        ringtonePlayer?.stop()
        //stopSelf()
    }

    ////////// Support binding this service and an activity - required to have service and activity interact //////////
    inner class LocalBinder : Binder() {
        fun getService(): RingtoneService = this@RingtoneService
    }

    private val binder: IBinder = LocalBinder()
    override fun onBind(intent: Intent?): IBinder = binder
}