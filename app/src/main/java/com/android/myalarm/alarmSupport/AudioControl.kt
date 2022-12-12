package com.android.myalarm.alarmSupport

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object AudioControl {

    var mediaPlayer: MediaPlayer? = null
    var lastResource: Uri? = null

    fun playAudio(c: Context, id: Uri?, isLooping: Boolean = true) {
        createMediaPlayer(c, id)
        mediaPlayer?.let {
            it.isLooping = isLooping
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.stop()
    }

    private fun createMediaPlayer(c: Context, id: Uri?) {
        // in case it's already playing something
        mediaPlayer?.stop()
        mediaPlayer = MediaPlayer.create(c, id)
        lastResource = id
    }

    /*
    // usually used inside the Activity's onResume method
    fun continuePlaying(c: Context, specificResource: Uri? = null) {
        specificResource?.let {
            if (lastResource != specificResource) {
                createMediaPlayer(c, specificResource)
            }
        }

        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

     */
    fun pauseAudio() {
        mediaPlayer?.pause()
    }
}