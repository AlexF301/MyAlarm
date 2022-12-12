package com.android.myalarm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.android.myalarm.alarmSupport.RingtoneService
import com.android.myalarm.database.AlarmType
import com.android.myalarm.databinding.ActivityAlarmSetOffBinding
import kotlin.math.abs


/** The jerk/acceleration difference required to detect a shake, in m/s^2 */
const val SHAKE_THRESHOLD = 3f

/** The minimum amount of time allowed between shakes, in nanoseconds */
const val MIN_TIME_BETWEEN_SHAKES = 1000000000L


class AlarmSetOffActivity : AppCompatActivity(), SensorEventListener {

    /**
     *
     */
    private lateinit var binding: ActivityAlarmSetOffBinding

    /**
     *
     */
    private var ringtoneService: RingtoneService? = null

    /**
     *
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder: RingtoneService.LocalBinder = service as RingtoneService.LocalBinder
            ringtoneService = binder.getService()

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            ringtoneService = null
        }
    }

    /** Last recorded acceleration */
    private val lastAcceleration = floatArrayOf(0f, 0f, 0f)

    /** Last time a shake was detected */
    private var timestampOfLastChange: Long = 0

    /** If this is the first event or not since resuming */
    private var isFirstEvent = true


    // Variables for the sensor
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmSetOffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // code to acquire the sensor manager and accelerometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val alarmType = intent.extras?.getString("type", AlarmType.Regular.name)

        Log.w("here", alarmType.toString())

        if (alarmType == AlarmType.Shake.name) {
            binding.shakeMessage.isVisible = true

        } else {
            binding.turnOffAlarm.isVisible = false
            binding.turnOffAlarm.isEnabled = false
            binding.turnOffAlarm.setOnClickListener {
                ringtoneService?.stop()
                finish()
            }
        }

        // Bind to LocalService
        Intent(this, RingtoneService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

    }


    override fun onSensorChanged(event: SensorEvent) {
        if (isShake(event.values, event.timestamp)) {
            ringtoneService?.stop()
            finish()
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    /**
     * Checks if the acceleration values in x, y, and z represent a "shake"
     * operation: the jerk (difference in acceleration values) is greater than
     * the SHAKE_THRESHOLD in at least 2 dimensions.
     *
     * @param acceleration array of accelerations in the x, y, and z directions
     * @param timestamp timestamp of when the acceleration values were generated
     * @return true if the data represents a shake
     */
    private fun isShake(acceleration: FloatArray, timestamp: Long): Boolean {
        val isShake =
            (timestamp - timestampOfLastChange >= MIN_TIME_BETWEEN_SHAKES) &&
                    acceleration.zip(lastAcceleration)
                        .count { (a, b) -> abs(a - b) > SHAKE_THRESHOLD } >= 2
        // save for comparing to next time
        acceleration.copyInto(lastAcceleration)
        if (isShake) {
            timestampOfLastChange = timestamp
        }
        return isShake
    }
}
