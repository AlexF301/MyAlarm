package com.android.myalarm.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * The data access object for performing queries involving events.
 * TODO: change livedata
 * TODO: Update some of these methods - some are uselesss i think
 */
@Dao
interface MyAlarmDao {
    /**
     * Add an alarm to the database
     */
    @Insert
    suspend fun insert(alarm: Alarm)

    /**
     * update the alarm
     */
    @Update
    fun update(alarm: Alarm)

    /**
     *
     */
    @Query("UPDATE alarms SET alarm_state=:alarmIsOn WHERE alarm_id=:id ")
    fun updateIsOn(alarmIsOn: Boolean, id: Long?)

    /**
     * delete the alarm from the database
     */
    @Delete
    fun delete(alarm: Alarm)

    /**
     * get all the alarms from the database
     */
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<Alarm>>

//    /**
//
//     */
//    @Query("SELECT alarm_id FROM alarms where hour = :hour AND minute = :minute AND days_selected = :days")
//    fun getAlarm(hour: Int, minute: Int, days: List<String>): Flow<Alarm>
}