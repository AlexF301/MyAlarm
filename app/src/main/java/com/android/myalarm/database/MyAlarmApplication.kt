package com.android.myalarm.database

import android.app.Application

/**
 * Base Android application class. Responsible for creating Repository to have access to Room
 * persistence library
 */
class MyAlarmApplication : Application() {

    /** Initialize MyAlarmRepository upon app start */
    override fun onCreate() {
        super.onCreate()
        MyAlarmRepository.initialize(this)
    }
}