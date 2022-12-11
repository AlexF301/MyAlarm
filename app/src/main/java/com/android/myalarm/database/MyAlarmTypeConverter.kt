package com.android.myalarm.database

import androidx.room.TypeConverter

class MyAlarmTypeConverter {

    @TypeConverter
    // Convert a mutable list of enum values to a string
    fun fromEnumList(list: MutableList<DayOfTheWeek>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    // Convert a string to a mutable list of enum values
    fun toEnumList(string: String): MutableList<DayOfTheWeek> {
        return string.split(",").mapTo(mutableListOf()) { value ->
            // Use the valueOf() method to convert the string to an enum value
            DayOfTheWeek.valueOf(value)
        }
    }
}