package com.android.myalarm.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Data class for Alarm objects.
 * @param alarmId Alarms have a unique id that is they their primary key when stored in the database (SQL Like)
 * @param hour hour for alarm to be set off
 * @param minute minute for alarm to be set off
 * @param daysSelected Alarms can be set on multiple days
 * @param alarmState can be either on or off
 * @param type two types normal or shake (enums)
 */
@Entity
data class Alarm(
    @PrimaryKey
    var alarmId: UUID = UUID.randomUUID(),
    var hour: Int,
    var minute: Int,
    var daysSelected: MutableList<DayOfTheWeek>,
    var alarmState: Boolean,
    var type : AlarmType
)

/**
 * The type of alarm that can be set. Regular alarms are generic alarms. An alarm goes off, user
 * presses a button to shut if off. With Shake alarms, a user shakes their phone to turn an alarm
 * off
 */
enum class AlarmType {
    Regular,
    Shake
}


