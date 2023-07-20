package com.android.myalarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.myalarm.database.Alarm
import com.android.myalarm.database.MyAlarmRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmsViewModel : ViewModel() {
    /** The repository we are using to perform queries */
    private val repo = MyAlarmRepository.get()

    /** The internal, mutable, list of events (as a flow) */
    private val _alarms: MutableStateFlow<List<Alarm>> = MutableStateFlow(emptyList())

    /** The external list of events (as a flow) */
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private var job : Job? = null

    fun getAlarms(){
        job?.cancel()
        job = viewModelScope.launch {
            repo.getAllAlarms().collect {
                _alarms.value = it
            }
        }
    }

    /**
     * delete an alarm
     */
    suspend fun deleteAlarm(alarm : Alarm) = repo.deleteAlarm(alarm)

    /**
     * get all alarms
     */
    fun getAllAlarms() = repo.getAllAlarms()

}