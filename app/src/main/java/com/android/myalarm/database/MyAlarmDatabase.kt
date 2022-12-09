package com.android.myalarm.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The database for the Calendar Events.
 * This database has a single table that is reflected in the Event class.
 * The data access object is an instance of the CalendarDao class.
 */
// This database has a single table that is reflected in the Event class.
@Database(entities = [ Alarm::class ], version=0)

// The values in this table are converted using the CalendarTypeConverter class.
@TypeConverters(MyAlarmTypeConverter::class)
abstract class MyAlarmDatabase : RoomDatabase() {
    // The data access object is an instance of the CalendarDao class.
    abstract fun calendarDao(): MyAlarmDao
}
