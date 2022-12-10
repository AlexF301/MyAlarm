package com.android.myalarm.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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
    @Query("UPDATE alarms SET alarm_state=:enable WHERE alarm_id=:id ")
    fun updateIsOn(enable: Boolean, id: Long?)

    /** get all the alarms from the database */
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<Alarm>>

//    /**
//
//     */
//    @Query("SELECT alarm_id FROM alarms where hour = :hour AND minute = :minute AND days_selected = :days")
//    fun getAlarm(hour: Int, minute: Int, days: List<String>): Flow<Alarm>
}