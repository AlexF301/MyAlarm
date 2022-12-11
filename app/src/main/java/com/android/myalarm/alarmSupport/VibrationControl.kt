package com.android.myalarm.alarmSupport

import android.content.Context
import android.os.*

object VibrationControl {

    private var vibrator : Vibrator? = null
    private var vibratorManager : VibratorManager? = null

    fun vibrationConfiguration(context: Context, vibrate: Boolean) {
        val pattern = longArrayOf(1500, 1000, 1000, 1000)
        if (vibrate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager?.defaultVibrator
                vibratorManager?.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.createWaveform(
                            pattern,
                            0
                        )
                    )
                )
            } else {
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator!!.vibrate(150)
            }
        }
    }

    fun stopVibration(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            vibratorManager?.cancel()
        else
            vibrator?.cancel()
    }
}