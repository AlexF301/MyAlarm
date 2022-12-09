package com.android.myalarm.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import java.util.*

// Name of the Room Database Instance
private const val DATABASE_NAME = "calendar_database"

class MyAlarmRepository private constructor(context: Context) {

    /** The database. */
    private val database: MyAlarmDatabase = Room
        .databaseBuilder(
            context.applicationContext,  // the context to use to access the database with
            MyAlarmDatabase::class.java, // the class that will be created for the database access
            DATABASE_NAME  // the name of the database
        )
        // pre-create from the given assets file - default events for when app is first installed
        .createFromAsset(DATABASE_NAME)
        .build()  // create and return the database

    /** The data access object */
    private val dao = database.calendarDao()


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
