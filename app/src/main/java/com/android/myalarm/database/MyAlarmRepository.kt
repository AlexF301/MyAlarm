package com.android.myalarm.database

import android.content.Context
import androidx.room.Room
import java.util.*

// Name of the Room Database Instance
private const val DATABASE_NAME = "alarms_database"

class MyAlarmRepository private constructor(context: Context) {

    /** The database */
    private val database: MyAlarmDatabase = Room
        .databaseBuilder(
            context.applicationContext,  // the context to use to access the database with
            MyAlarmDatabase::class.java, // the class that will be created for the database access
            DATABASE_NAME  // the name of the database
        )
        // pre-create from the given assets file - default events for when app is first installed
        .build()  // create and return the database

    /** The data access object to interact with our database*/
    private val dao = database.calendarDao()

    /** add an alarm to the database
     * @param alarm: The Alarm being added into the database
     */
    suspend fun addAlarm(alarm: Alarm) = dao.addAlarm(alarm)

    /** update an alarm in the database
     * @param alarm: The alarm to update
     */
    suspend fun updateAlarm(alarm: Alarm) = dao.updateAlarm(alarm)

    /** delete an alarm in the database
     * @param alarm: Alarm to delete from the database
     */
    suspend fun deleteAlarm(alarm: Alarm) = dao.deleteAlarm(alarm)

    /** get all alarms from the database */
    fun getAllAlarms() = dao.getAllAlarms()

    /** Gets an Alarm based off the specified hour, minute, and days selected. References the dao
     * object which queries the database with the specified attributes. If an alarm with the
     * specified attributes exists, returns true. Otherwise returns false.
     * @param hour: hour the alarm is set to
     * @param minute: minute the alarm is set to
     * @param daysSelected: the days the alarm is set to
     * @return true if the alarm with the specified attributes exists in the database
     */
    suspend fun getAlarm(hour: Int, minute: Int, daysSelected: MutableList<DayOfTheWeek>): Boolean = dao.getAlarm(hour, minute, daysSelected)

    /** get an Alarm by its id
     * @param alarmId: The id of an alarm to query for in the database
     * @return The Alarm associated with the provided alarmId
     */
    suspend fun getAlarmByID(alarmId : UUID) : Alarm = dao.getAlarmById(alarmId)

    /**
     *
     */
    suspend fun doesAlarmExistWithId(alarmId: UUID) : Boolean = dao.doesAlarmWithIdExists(alarmId)

    companion object {
        /** The singleton instance of the repository. */
        private var INSTANCE: MyAlarmRepository? = null

        /** Initialize the database using the given context. */
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = MyAlarmRepository(context)
            }
        }

        /** Get the single repository instance */
        fun get(): MyAlarmRepository {
            return INSTANCE ?: throw IllegalStateException("Repository must be initialized")
        }
    }
}
