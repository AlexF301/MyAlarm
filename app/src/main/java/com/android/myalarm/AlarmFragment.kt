package com.android.myalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TimePicker
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.android.myalarm.alarmSupport.AlarmReceiver
import com.android.myalarm.database.Alarm
import com.android.myalarm.database.AlarmType
import com.android.myalarm.database.DayOfTheWeek
import com.android.myalarm.databinding.DayPickerBinding
import com.android.myalarm.databinding.FragmentAlarmBinding
import kotlinx.coroutines.launch
import java.util.*


/**
 * A simple [Fragment] subclass as the second destination in the navigation. Responsible for
 * creating an alarm.
 */
class AlarmFragment : Fragment(), OnClickListener {

    /** binding for the views of this fragments layout (nullable version) */
    private var _binding: FragmentAlarmBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
     * This property is only valid between onCreateView and onDestroyView
     */
    private val binding: FragmentAlarmBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    /** Used to pass the alarmId */
    private val args: AlarmFragmentArgs by navArgs()

    /** binding for the views of the day_picker layout (non-nullable accessor)
     * This property is only valid between onCreateView and onDestroyView. Reference for the
     * ToggleButton selection(s)
     */
    private lateinit var dayPickerBinding: DayPickerBinding

    /** The Alarm being accessed. Has default values but are changed later */
    private var alarm: Alarm? = null

    /** The viewModel for the current accessed Alarm */
    private val alarmViewModel: AlarmViewModel by viewModels {
        AlarmViewModelFactory(UUID.fromString(args.alarmId))
    }

    /** TimePicker widget which supplies user with time selection ability */
    private lateinit var timePicker: TimePicker

    /** Each button corresponds to a day of the weak */
    private var daysOfWeek: HashMap<ToggleButton, DayOfTheWeek> = hashMapOf()

    /** Alarm manager object*/
    private var alarmManager : AlarmManager? = null

    /** Volume of the alarm */
    var volumeLevel: Float = 0.0f

    /** If the user wants the alarm to vibrate when finished*/
    var vibrate: Boolean = true

    /** Dummy value for the title of the alarm*/
    var title: String = "hello"

    /** The ringtone */
    var ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    var ringtoneName : String = ""

    /** Seekbar used for user volume */
    lateinit var seekBar: SeekBar

    /** The audio to pass user volume information*/
    private lateinit var audioManager: AudioManager

    /**
     * Inflates the layouts for this fragment as well as the day_picker layout to access
     * ToggleButtons which will be used to select days a user wants an Alarm to repeat
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        dayPickerBinding = binding.dayPicker

        daysOfWeek = hashMapOf(
            dayPickerBinding.sunday to DayOfTheWeek.Sunday,
            dayPickerBinding.monday to DayOfTheWeek.Monday,
            dayPickerBinding.tuesday to DayOfTheWeek.Tuesday,
            dayPickerBinding.wednesday to DayOfTheWeek.Wednesday,
            dayPickerBinding.thursday to DayOfTheWeek.Thursday,
            dayPickerBinding.friday to DayOfTheWeek.Friday,
            dayPickerBinding.saturday to DayOfTheWeek.Saturday
        )
        return binding.root
    }

    /**
     * Sets the on click listeners for the Alarm options
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timePicker = binding.timePicker

        // Set onClickListeners for each ToggleButton
        for (dayButtons in daysOfWeek.keys)
            dayButtons.setOnClickListener(this)

        // The updates the event (using the view model) with a copy of the old event with the type
        // changed using the information from the bundle.
        // Listen for dialog fragment results
        setFragmentResultListener(AlarmTypeFragment.REQUEST_KEY_ALARM_TYPE) { _, bundle ->
            alarmViewModel.updateAlarm {
                it.copy(
                    type = bundle.getSerializable(AlarmTypeFragment.BUNDLE_KEY_ALARM_TYPE) as AlarmType
                )
            }
        }

        setFragmentResultListener(SelectRingtoneDialogFragment.REQUEST_KEY) { _, bundle ->
            alarmViewModel.updateAlarm {
                it.copy(
                    ringTone = bundle.getString(SelectRingtoneDialogFragment.BUNDLE_KEY) as String
                )
            }
        }

        // Create alarm
        binding.createAlarm.setOnClickListener {
            getAlarmTimesAndDates()
            lifecycleScope.launch {
                title = binding.alarmName.text.toString()
                addAlarm()
            }
        }

        // use coroutine to collect alarm from database
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmViewModel.alarm.collect {
                    alarm = it
                    alarm?.let { it1 -> updateUI(it1) }
                }
            }
        }

        //instantiate seekBar
        seekBar = binding.volume
        seekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            /**
            * Checks the progress (position) of the seekBar
            * @param seekBar the seekbar
            * @param progress where the seekbar is located
            * @param fromUser which user it is coming from
            */
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                volumeLevel = progress.toFloat() / 10
            }

            /**
            * Needed but not used
            * @param seekBar the seekbar
            */
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            /**
             * Needed but not used
             * @param seekBar the seekbar
             */
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    /**
     * Updates the ui
     * Sets on OnClickListeners for Alarm Type and Ringtone Selection
     */
    private fun updateUI(alarm: Alarm) {
        binding.selectAlarmType.setOnClickListener {
            findNavController().navigate(AlarmFragmentDirections.navSelectAlarmType())
        }
        binding.alarmType.text = alarm.type.toString()

        binding.ringtone.setOnClickListener {
            findNavController().navigate(AlarmFragmentDirections.navSelectRingtone())
        }
        binding.ringtoneName.text = alarm.ringTone
    }

    /**
     * add an alarm to the database. If there is an existing alarm that has the same hour, minute,
     * and days that a user is trying to create. Notifies a user that Alarm already exists by
     * displaying a Toast
     */
    private suspend fun addAlarm() {
        alarm?.hour = alarmViewModel.hour
        alarm?.minute = alarmViewModel.minute
        alarm?.type = alarmViewModel.type
        alarm?.daysSelected = alarmViewModel.daysSelected

        if (!alarmViewModel.getAlarm()) {
            alarm?.let { alarmViewModel.addAlarm(it) }
            updateAlarmManager(alarm)
            findNavController().popBackStack()
        } else
            Toast.makeText(requireActivity(), R.string.alarm_exists, Toast.LENGTH_LONG).show()
    }

    /** Clear up memory */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Acquire some the specification for the Alarm being created from the UI selections
     */
    private fun getAlarmTimesAndDates() {
        alarmViewModel.hour = timePicker.hour
        alarmViewModel.minute = timePicker.minute
        alarmViewModel.alarmState = binding.vibrationSwitch.isChecked
    }

    /** updates the Alarm selected days based off the ToggleButton Clicked
     * @param view: the view subclass that was clicked
     */
    override fun onClick(view: View?) {
        if (view != null)
            updateDaysSelectedList(view as ToggleButton)
    }

    /**
     * Adds and Removes selected days of the week from the list of days the Alarm is set to go off for.
     * Sorts the days of the week so will always appear in order
     * @param dayButton: the ToggleButton pertaining to a day that was clicked
     */
    private fun updateDaysSelectedList(dayButton: ToggleButton) {
        val day: DayOfTheWeek? = daysOfWeek[dayButton]

        if (alarmViewModel.daysSelected.contains(day))
            alarmViewModel.daysSelected.remove(day)
        else
            day?.let { alarmViewModel.daysSelected.add(it) }

        alarmViewModel.daysSelected.sort()
    }

    /**
     * Updates the alarm based on user input.
     */
    private fun updateAlarmManager(alarm: Alarm?) {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        alarmIntentExtras(alarmIntent)

        val pendingIntent = PendingIntent.getBroadcast(context, 1, alarmIntent, PendingIntent.FLAG_IMMUTABLE // setting the mutability flag
        )
        if (alarm != null) {
            if (alarm.alarmState) {
                val calendar: Calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
                calendar.set(Calendar.MINUTE, alarm.minute)
                calendar.set(Calendar.SECOND, 0)

                evaluateAlarmTrigger(calendar)

                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager?.cancel(pendingIntent)
            }
        }
    }

    /**
     * Puts users intents to be used in other functions
     */
    private fun alarmIntentExtras(alarmIntent: Intent) {
        alarmIntent.putExtra("volume", volumeLevel)
        alarmIntent.putExtra("vibrate", vibrate)
        alarmIntent.putExtra("alarm_title", title)
        alarmIntent.putExtra("ringtone", ringtoneUri.toString())
        alarmIntent.putExtra("type", alarm?.type)
    }

    /**
    AlarmManager would trigger if time for the alarm set has been passed on that day.
    For example. If the time today was 11:00pm and an alarm was set for 10:50am, alarmManager
    would trigger right away. Code below prevents that and sets any previous time to the
    next day
     **/
    private fun evaluateAlarmTrigger(calendar: Calendar) {
        if (System.currentTimeMillis() > calendar.timeInMillis)
            calendar.add(Calendar.DATE, 1)
    }

}