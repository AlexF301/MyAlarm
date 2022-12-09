package com.android.myalarm.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "alarms")
data class Alarm(
    @ColumnInfo(name = "hour")
    var Hour: Int,
    @ColumnInfo(name = "minute")
    var minute: Int,
    @ColumnInfo(name = "days_selected")
    var days: List<String>,
    @ColumnInfo(name = "alarm_state")
    var isOn: Boolean,
    @ColumnInfo(name = "type")
    var type : AlarmType
) {
    @ColumnInfo(name = "alarm_id")
    @PrimaryKey(autoGenerate = true)
    var id: UUID = UUID.randomUUID()
}


enum class AlarmType(name : String) {
    Generic("Normal"),
    Shake("Shake")
}


