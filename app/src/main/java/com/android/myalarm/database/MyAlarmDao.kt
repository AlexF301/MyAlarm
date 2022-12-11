package com.android.myalarm.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * The data access object for performing queries involving events.
 */
@Dao
interface MyAlarmDao {

    /** Add an alarm to the database */
    @Insert
    suspend fun addAlarm(alarm: Alarm)

    /** update the alarm */
    @Update
    suspend fun updateAlarm(alarm: Alarm)

    /** delete the alarm from the database */
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    /** update the status of the alarm (enabled or disable
     * TODO: does this even have to be used since theres already an update method
     */
    @Query("UPDATE alarm SET alarmState=:enable WHERE alarmId=:alarmId")
    fun updateIsOn(enable: Boolean, alarmId: Long?)

    /** get all the alarms from the database */
    @Query("SELECT * FROM alarm")
    fun getAllAlarms(): Flow<List<Alarm>>

    /** get an Alarm by its id */
    @Query("SELECT * FROM alarm WHERE alarmId=(:alarmId) LIMIT 1")
    suspend fun getAlarmById(alarmId : UUID) : Alarm

    /** Gets an alarm based off the specified hour, minute, and days selected */
    @Query("SELECT EXISTS(SELECT alarmId FROM alarm where hour = :hour AND minute = :minute AND daysSelected = :days)")
    suspend fun getAlarm(hour: Int, minute: Int, days: MutableList<DayOfTheWeek>): Boolean
}