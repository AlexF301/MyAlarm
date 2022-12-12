package com.android.myalarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.myalarm.database.Alarm
import com.android.myalarm.database.AlarmType
import com.android.myalarm.database.DayOfTheWeek
import com.android.myalarm.database.MyAlarmRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

/**
 *
 */
class TimerViewModel() : ViewModel() {

    /** Logic tool to pause/resume timer */
    var counter = 0

    /** How much time a timer has remaining in a nice format */
    var countdownTime = 0L

    /** Interval in which the timer increments down in (in milliseconds) */
    var countdownInterval: Long = 1000

    /** Used to resume the alarm and display time remaining */
    var timeRemaining = 0L

    /** Countdown timer object */
    lateinit var timer: CountDownTimer

    /** Hour the user chooses from the widget */
    var hourPicker = 0L

    /** Minute the user chooses from the widget */
    var minutePicker = 0L

    /** Second the user chooses from the widget */
    var secondPicker = 0L

    /** Hours of the remaining time */
    var hours = 0L

    /** Minutes of the remaining time */
    var minutes = 0L

    /** Seconds of the remaining time */
    var seconds = 0L


//    /**
//     * Starts a CountDownTimer object based on how much time a user specifies for an alarm. Once
//     * time has expired, the user specified sounds / ringtone will play.
//     */
//    private fun start(userTime: Long) {
//        timer = object : CountDownTimer((userTime * 1000), 1000) {
//            /**
//             * Whenever a timer "ticks", the time variables are calculated and is then formatted
//             * to display to the user in a nice view.
//             */
//            override fun onTick(millisUntilFinished: Long) {
//                hours = (millisUntilFinished / 1000) / 3600
//                minutes = (millisUntilFinished / 1000) % 3600 / 60
//                seconds = (millisUntilFinished / 1000) % 60
//                timeRemaining = millisUntilFinished / 1000
//                binding.countdownView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
//            }
//
//            /**
//             * When the timer reaches 0, resets the timer and all corresponding variables, ensures
//             * the user cannot click the "stop" until a new timer has started, and plays a sounds
//             * to let the user know their timer is going off.
//             */
//            override fun onFinish() {
//                timeRemaining = 0
//                binding.startCountdown.text = getString(R.string.start)
//                timer.cancel()
//                counter = 0
//                setNumberPickerVisible(true)
//                binding.stopCountdown.isEnabled = false
//                startSound()
//            }
//        }
//
//        timer.start()
//    }



}