package com.android.myalarm.database

import androidx.room.TypeConverter
import com.google.gson.Gson

class MyAlarmTypeConverter {
    @TypeConverter
    fun listToJson(value: List<String>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<String>::class.java).toList()
}