package com.android.myalarm

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import com.android.myalarm.databinding.FragmentTimerBinding
import java.io.IOException
import kotlin.math.abs


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

    /** The jerk/acceleration difference required to detect a shake, in m/s^2 */
    val SHAKE_THRESHOLD = 3f

    /** The minimum amount of time allowed between shakes, in nanoseconds */
    val MIN_TIME_BETWEEN_SHAKES = 1000000000L

    /** Last recorded acceleration */
    private val lastAcceleration = floatArrayOf(0f, 0f, 0f)

    /** Last time a shake was detected */
    private var timestampOfLastChange: Long = 0

    /** If this is the first event or not since resuming */
    private var isFirstEvent = true

    /** Media player instance */
    private var mediaPlayer: MediaPlayer? = null

    /** All of the known audio files */
//    private val audios = intArrayOf(
//        R.raw.train, R.raw.pew, R.raw.monkey, R.raw.kid_laugh, R.raw.dial_tone, R.raw.cow, R.raw.laugh
//    )

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null


    /** Creates the binding view for this layout */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflates the layout for this fragment
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return binding.root
    }


    /**
     *
     */
    fun onSensorChanged(event: SensorEvent) {
        if (isShake(event.values, event.timestamp)) {
            setAudioResource(R.raw.cow)
            togglePlayback()
        }

    }

    /**
     *
     */
    fun onAccuracyChanged(p0: Sensor?, p1: Int) { }


    /**
     * Checks if the acceleration values in x, y, and z represent a "shake"
     * operation: the jerk (difference in acceleration values) is greater than
     * the SHAKE_THRESHOLD in at least 2 dimensions.
     *
     * @param acceleration array of accelerations in the x, y, and z directions
     * @param timestamp timestamp of when the acceleration values were generated
     * @return true if the data represents a shake
     */
    private fun isShake(acceleration: FloatArray, timestamp: Long): Boolean {
        val isShake =
            (!isFirstEvent && timestamp - timestampOfLastChange >= MIN_TIME_BETWEEN_SHAKES) &&
                    acceleration.zip(lastAcceleration).count { (a, b) -> abs(a - b) > SHAKE_THRESHOLD } >= 2
        // save for comparing to next time
        acceleration.copyInto(lastAcceleration)
        isFirstEvent = false
        if (isShake) { timestampOfLastChange = timestamp }
        return isShake
    }


    /**
     * Toggles play back of the media player. If it is currently playing, it is
     * stopped. If it is not currently playing, it is started.
     */
    private fun togglePlayback() {
        mediaPlayer?.apply {
            start()
//            if (isPlaying) {
//                // if currently playing, reset to beginning and pause
//                seekTo(0)
//                pause()
//            } else {
//                // if not currently playing, start playing
//                start()
//            }
        }
    }


    /**
     * Sets the audio being played from a resource ID. This re-uses the current
     * media player object.
     * @param resourceId the resource audio for the audio, like R.raw.monkey.
     */
    private fun setAudioResource(@RawRes resourceId: Int) {
        val afd = resources.openRawResourceFd(resourceId)
        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                afd.close()
            }
        } catch (ex: IOException) {
            Log.e("MainActivity", "set audio resource failed", ex)
        }
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
                setAudioResource(R.raw.cow)
                togglePlayback()

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