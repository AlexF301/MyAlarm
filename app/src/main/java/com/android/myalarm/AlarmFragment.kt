package com.android.myalarm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.myalarm.database.Alarm
import com.android.myalarm.database.AlarmType
import com.android.myalarm.database.DayOfTheWeek
import com.android.myalarm.databinding.DayPickerBinding
import com.android.myalarm.databinding.FragmentAlarmBinding
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AlarmFragment : Fragment(), OnClickListener {

    /** binding for the views of this fragments layout (nullable version) */
    private var _binding: FragmentAlarmBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
     * This property is only valid between onCreateView and onDestroyView
     */
    private val binding: FragmentAlarmBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    /** binding for the views of the day_picker layout (non-nullable accessor)
     * This property is only valid between onCreateView and onDestroyView. Reference for the
     * ToggleButton selection(s)
     */
    private lateinit var dayPickerBinding: DayPickerBinding

    /** The Alarm being accessed. Default values but are changed later */
    private var alarm: Alarm = Alarm(
        hour = 0,
        minute = 0,
        daysSelected = mutableListOf(),
        alarmState = true,
        type = AlarmType.Regular
    )

    /** The viewModel for the current accessed Alarm */
    private val alarmViewModel: AlarmViewModel by viewModels {
        AlarmViewModelFactory(alarm.alarmId)
    }

    /** TimePicker widget which supplies user with time selection ability */
    private lateinit var timePicker: TimePicker

    /** Each button corresponds to a day of the weak */
    private var daysOfWeek: HashMap<ToggleButton, DayOfTheWeek> = hashMapOf()

    // TODO: fix alarmState

    /**
     * Inflates the layouts for this fragment as well as the day_picker layout to access
     * ToggleButtons which will be used to select days a user wants an Alarm to repeat
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

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
     * Sets on click listeners for Alarm options
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timePicker = binding.timePicker

        // Set onClickListeners for each ToggleButton
        for (dayButtons in daysOfWeek.keys)
            dayButtons.setOnClickListener(this)

        // When user selects on the text view to select an Alarm Type, navigates to the AlarmTypeFragment
        binding.selectAlarmType.setOnClickListener {
            findNavController().navigate(AlarmFragmentDirections.navSelectAlarmType())
        }

        // Create alarm
        binding.createAlarm.setOnClickListener {
            getAlarmTimesAndDates()
            lifecycleScope.launch {
                addAlarm()
            }
        }
    }

    /**
     * add an alarm to the database. If there is an existing alarm that has the same hour, minute,
     * and days that a user is trying to create. Notifies a user that Alarm already exists by
     * displaying a Toast
     */
    private suspend fun addAlarm() {
        alarm.hour = alarmViewModel.hour
        alarm.minute = alarmViewModel.minute
        alarm.alarmState = alarmViewModel.alarmState
        alarm.type = alarmViewModel.type
        alarm.daysSelected = alarmViewModel.daysSelected

        if (!alarmViewModel.getAlarm()) {
            alarmViewModel.addAlarm(alarm)
            findNavController().popBackStack()
        } else
            Toast.makeText(requireActivity(), R.string.alarm_exists, Toast.LENGTH_LONG).show()
        Log.w("here", alarm.toString())
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

//    /**
//     * Whenever in SelectAlarmTypeFragment and SelectRingtoneFragment, the createAlarm button becomes
//     * invisible and cannot be used for UI decisions. When pressing back from those fragments, button
//     * becomes usable again by setting isVisible to true
//     *
//     * Also turns off any ringtone sound that was playing when exiting the SelectRingtoneFragment
//     */
//    override fun onBackPressed() {
//        super.onBackPressed()
//        // Returns visibility and usability to create alarm button
//        createAlarm.isVisible = true
//
//        // Creates intent to access RingtoneHelper service class to stop ringtone sound
//        val playRingtone = Intent(applicationContext, RingtoneHelper::class.java)
//        applicationContext.stopService(playRingtone)
//    }
}