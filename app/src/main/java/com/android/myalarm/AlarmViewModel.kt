package com.android.myalarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
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

class AlarmViewModel(private val alarmId: UUID) : ViewModel() {
    /** The repository we are using to perform queries */
    private val repo = MyAlarmRepository.get()

    /** The internal, mutable, list of events (as a flow) */
    private val _alarm = MutableStateFlow<Alarm?>(null)

    /** The external list of events (as a flow) */
    var alarm: StateFlow<Alarm?> = _alarm.asStateFlow()

    var hour: Int = 0
    var minute: Int = 0
    var daysSelected: MutableList<DayOfTheWeek> = mutableListOf(DayOfTheWeek.NONE)
    var alarmState: Boolean = true
    var type: AlarmType = AlarmType.Regular

    /** load up the alarm from the database when viewModel is initiated */
    init {
        viewModelScope.launch {
            // if Alarm doesn't exists, create one
            if (!repo.doesAlarmExistWithId(alarmId))
                _alarm.value = Alarm()
            else
                _alarm.value = repo.getAlarmByID(alarmId) }
    }

    /** add an alarm */
    suspend fun addAlarm(alarm: Alarm) = repo.addAlarm(alarm)

    /** update an alarm */
    fun updateAlarm(onUpdate: (Alarm) -> Alarm) {
        _alarm.update { it?.let { onUpdate(it) } }
    }

    /** updates the event in the database when the view model is done */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            _alarm.value?.let {
                repo.updateAlarm(it)
            }
        }
    }

    /**
     *
     */
    suspend fun getAlarm() : Boolean = repo.getAlarm(hour, minute, daysSelected)

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

/**
 * Creates the viewModel for an Alarm Object.
 * @param alarmId of alarm to get from database
 */
class AlarmViewModelFactory(private val alarmId: UUID) : ViewModelProvider.Factory {
    /**
     * Create a new instance of the AlarmViewModel class with the alarmId provided
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmViewModel(alarmId) as T
    }
}