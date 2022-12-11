package com.android.myalarm

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.myalarm.database.Alarm
import com.android.myalarm.database.MyAlarmRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

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

    //private var alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var volumeLevel: Int = 0
    var vibrate: Boolean = true
    var title: String = "Wake Up"
    var ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    var ringtoneName: String = ""

    /**
     * delete an alarm
     */
    suspend fun deleteAlarm(alarm : Alarm) = repo.deleteAlarm(alarm)

    /**
     * get all alarms
     */
    fun getAllAlarms() = repo.getAllAlarms()

//    private fun updateAlarmManager(isOn: Boolean, alarm: Alarm, id: Long) {
//        val alarmIntent = Intent(getApplication(), AlarmReceiver::class.java)
//        alarmIntentExtras(alarmIntent)
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            getApplication(),
//            id.toInt(),
//            alarmIntent,
//            PendingIntent.FLAG_IMMUTABLE // setting the mutability flag
//        )
//        if (isOn) {
//            val calendar: Calendar = Calendar.getInstance()
//            calendar.set(Calendar.HOUR_OF_DAY, alarm.Hour)
//            calendar.set(Calendar.MINUTE, alarm.minute)
//            calendar.set(Calendar.SECOND, 0)
//
//            evaluateAlarmTrigger(calendar)
//
//            alarmManager.setExactAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                pendingIntent
//            )
//        } else {
//            alarmManager.cancel(pendingIntent)
//        }
//    }

    private fun alarmIntentExtras(alarmIntent: Intent) {
        alarmIntent.putExtra("volume", volumeLevel.toFloat())
        alarmIntent.putExtra("vibrate", vibrate)
        alarmIntent.putExtra("alarm_title", title)
        alarmIntent.putExtra("ringtone", ringtoneUri.toString())
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