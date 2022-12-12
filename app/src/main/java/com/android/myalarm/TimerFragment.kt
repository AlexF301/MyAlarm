package com.android.myalarm

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.myalarm.databinding.FragmentTimerBinding


/**
 * A fragment that allows a user to create a timer based on the hours, minutes, and seconds they
 * pick. A sound will play alerting the user their alarm is going off, in which case the user
 * can shake their phone and it will stop playing.
 */
class TimerFragment : Fragment() {

    /** Binding for the views of the fragment (nullable version) */
    private var _binding: FragmentTimerBinding? = null

    /** Binding for the views of the fragment (non-nullable accessor) */
    private val binding get() = _binding!!

    /** Logic tool to pause/resume timer */
    private var counter = 0

    /** How much time a timer has remaining in a nice format */
    private var countdownTime = 0L

    /** Interval in which the timer increments down in (in milliseconds) */
    private var countdownInterval: Long = 1000

    /** Used to resume the alarm and display time remaining */
    private var timeRemaining = 0L

    /** Countdown timer object */
    private lateinit var timer: CountDownTimer

    /** Hour the user chooses from the widget */
    private var hourPicker = 0L

    /** Minute the user chooses from the widget */
    private var minutePicker = 0L

    /** Second the user chooses from the widget */
    private var secondPicker = 0L

    /** Hours of the remaining time */
    private var hours = 0L

    /** Minutes of the remaining time */
    private var minutes = 0L

    /** Seconds of the remaining time */
    private var seconds = 0L


    /** Creates the binding view for this layout */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflates the layout for this fragment
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }


    /**
     * Starts a CountDownTimer object based on how much time a user specifies for an alarm. Once
     * time has expired, the user specified sounds / ringtone will play.
     */
    private fun start(userTime: Long) {
        timer = object : CountDownTimer((userTime * 1000), countdownInterval) {
            /**
             * Whenever a timer "ticks", the time variables are calculated and is then formatted
             * to display to the user in a nice view.
             */
            override fun onTick(millisUntilFinished: Long) {
                hours = (millisUntilFinished / 1000) / 3600
                minutes = (millisUntilFinished / 1000) % 3600 / 60
                seconds = (millisUntilFinished / 1000) % 60
                timeRemaining = millisUntilFinished / 1000
                binding.countdownView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            /**
             * When the timer reaches 0, resets the timer and all corresponding variables, ensures
             * the user cannot click the "stop" until a new timer has started, and plays a sounds
             * to let the user know their timer is going off.
             */
            override fun onFinish() {
                timeRemaining = 0
                Toast.makeText(activity, "Time is up", Toast.LENGTH_SHORT).show()
                binding.startCountdown.text = getString(R.string.start)
                timer.cancel()
                counter = 0
                setNumberPickerVisible(true)
                binding.stopCountdown.isEnabled = false

                // ADD CODE TO MAKE AN ALARM SOUND HAPPEN HERE!!!!!!
                // ADD CODE TO MAKE AN ALARM SOUND HAPPEN HERE!!!!!!
            }
        }
        timer.start()
    }

    /**
     * Sets the NumberPicker widget to be accessible or restricted during a timer event
     */
    private fun setNumberPickerVisible(visible : Boolean) {
        if (visible) {
            binding.hourPicker.isEnabled = true
            binding.minutePicker.isEnabled = true
            binding.secondPicker.isEnabled = true
        } else {
            binding.hourPicker.isEnabled = false
            binding.minutePicker.isEnabled = false
            binding.secondPicker.isEnabled = false
        }
    }

    /**
     * Creates the minimum and maximum values of the number picker widget, and listens for user
     * choices and changes to save the values in order to use them for the timer.
     */
    private fun numberPicker() {
        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = 23
        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = 59
        binding.secondPicker.minValue = 0
        binding.secondPicker.maxValue = 59

        binding.hourPicker.setOnValueChangedListener { _, _, _ ->
            hourPicker = binding.hourPicker.value.toLong()
        }
        binding.minutePicker.setOnValueChangedListener { _, _, _ ->
            minutePicker = binding.minutePicker.value.toLong()
        }
        binding.secondPicker.setOnValueChangedListener { _, _, _ ->
            secondPicker = binding.secondPicker.value.toLong()
        }


    }

    /**
     * Parses the hours, minutes, and seconds into a format that can be inserted into the
     * CountdownTimer
     */
    private fun parseTimeValues(): Long {
        val hoursToSeconds = (hourPicker * 60) * 60
        val minutesToSeconds = minutePicker * 60
        countdownTime = hoursToSeconds + minutesToSeconds + secondPicker
        return countdownTime
    }


    /**
     * Once the view is created, the user is able to interact with the start, pause, resume, and stop
     * button, and as a result, triggers different actions based on the time the user has chosen.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ensures user cannot start the timer without imputing values
        // FIX THIS: ERROR WITH STARTING TIMER AT 00:00:00

//        if ((hours == 0L) && (minutes == 0L) && (seconds == 0L)) {
//            binding.startCountdown.isEnabled = false
//        }

        // Triggered whenever the start button is clicked
        binding.startCountdown.setOnClickListener {
            binding.stopCountdown.isEnabled = true
            setNumberPickerVisible(false)
            counter += 1

            // initial start
            if (counter == 1) {
                start(parseTimeValues())
                binding.startCountdown.text = getString(R.string.pause)
            }

            if (counter % 2 == 0) {
                timer.cancel()
                binding.startCountdown.text = getString(R.string.resume)
            } else {
                if (counter != 1) {
                    start(timeRemaining)
                    binding.startCountdown.text = getString(R.string.pause)
                }
            }
        }

        // Triggered when the stop button is clicked
        binding.stopCountdown.setOnClickListener {
            timer.cancel()
            counter = 0
            binding.stopCountdown.isEnabled = false
            binding.countdownView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            binding.startCountdown.text = getString(R.string.start)
            setNumberPickerVisible(true)
        }
        numberPicker()
    }




}