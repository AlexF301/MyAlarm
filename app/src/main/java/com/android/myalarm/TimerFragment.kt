package com.android.myalarm

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.android.myalarm.alarmSupport.RingtoneService
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

    /** The viewModel for the views of the fragment */
    private val viewModel: TimerViewModel by viewModels()

    private var ringtoneService : RingtoneService? = null

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

        viewModel.timer = object : CountDownTimer((userTime * 1000), 1000) {
            /**
             * Whenever a timer "ticks", the time variables are calculated and is then formatted
             * to display to the user in a nice view.
             */
            override fun onTick(millisUntilFinished: Long) {
                viewModel.hours = (millisUntilFinished / 1000) / 3600
                viewModel.minutes = (millisUntilFinished / 1000) % 3600 / 60
                viewModel.seconds = (millisUntilFinished / 1000) % 60
                viewModel.timeRemaining = millisUntilFinished / 1000
                binding.countdownView.text = String.format("%02d:%02d:%02d", viewModel.hours, viewModel.minutes, viewModel.seconds)
            }

            /**
             * When the timer reaches 0, resets the timer and all corresponding variables, ensures
             * the user cannot click the "stop" until a new timer has started, and plays a sounds
             * to let the user know their timer is going off.
             */
            override fun onFinish() {
                viewModel.timeRemaining = 0
                binding.startCountdown.text = getString(R.string.start)
                viewModel.timer.cancel()
                viewModel.counter = 0
                setNumberPickerVisible(true)
                binding.stopCountdown.isEnabled = false
                startSound()
            }
        }

        viewModel.timer.start()
    }


    /**
     * Alerts the user the timer is going off by playing an audio, and releases the resources when
     * it is done playing
     */
    fun startSound() {
        val playRingtone = Intent(context, RingtoneService::class.java)
        requireActivity().startService(playRingtone)

        binding.reset?.isEnabled = true
        binding.reset?.isVisible = true
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
            viewModel.hourPicker = binding.hourPicker.value.toLong()
        }
        binding.minutePicker.setOnValueChangedListener { _, _, _ ->
            viewModel.minutePicker = binding.minutePicker.value.toLong()
        }
        binding.secondPicker.setOnValueChangedListener { _, _, _ ->
            viewModel.secondPicker = binding.secondPicker.value.toLong()
        }
    }

    /**
     * Parses the hours, minutes, and seconds into a format that can be inserted into the
     * CountdownTimer
     */
    private fun parseTimeValues(): Long {
        val hoursToSeconds = (viewModel.hourPicker * 60) * 60
        val minutesToSeconds = viewModel.minutePicker * 60
        viewModel.countdownTime = hoursToSeconds + minutesToSeconds + viewModel.secondPicker
        return viewModel.countdownTime
    }


    /**
     * Once the view is created, the user is able to interact with the start, pause, resume, and stop
     * button, and as a result, triggers different actions based on the time the user has chosen.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reset?.setOnClickListener {
            val stopRingtone = Intent(context, RingtoneService::class.java)
            requireActivity().stopService(stopRingtone)
        }

        // Triggered whenever the start button is clicked
        binding.startCountdown.setOnClickListener {
            binding.stopCountdown.isEnabled = true
            setNumberPickerVisible(false)
            viewModel.counter += 1

            // initial start
            if (viewModel.counter == 1) {
                start(parseTimeValues())
                binding.startCountdown.text = getString(R.string.pause)
            }

            if (viewModel.counter % 2 == 0) {
                viewModel.timer.cancel()
                binding.startCountdown.text = getString(R.string.resume)
            } else {
                if (viewModel.counter != 1) {
                    start(viewModel.timeRemaining)
                    binding.startCountdown.text = getString(R.string.pause)
                }
            }
        }

        // Triggered when the stop button is clicked
        binding.stopCountdown.setOnClickListener {
            viewModel.timer.cancel()
            viewModel.counter = 0
            binding.stopCountdown.isEnabled = false
            binding.countdownView.text = String.format("%02d:%02d:%02d", viewModel.hours, viewModel.minutes, viewModel.seconds)
            binding.startCountdown.text = getString(R.string.start)
            setNumberPickerVisible(true)
        }
        numberPicker()
    }


}