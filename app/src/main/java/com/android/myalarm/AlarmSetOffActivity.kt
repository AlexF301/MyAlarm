package com.android.myalarm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.android.myalarm.alarmSupport.RingtoneService
import com.android.myalarm.databinding.ActivityAlarmSetOffBinding

class AlarmSetOffActivity : AppCompatActivity() {

    /**
     *
     */
    private lateinit var binding: ActivityAlarmSetOffBinding

    /**
     *
     */
    private var ringtoneService : RingtoneService? = null

    /**
     *
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder : RingtoneService.LocalBinder = service as RingtoneService.LocalBinder
            ringtoneService = binder.getService()

        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            ringtoneService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmSetOffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bind to LocalService
        Intent(this, RingtoneService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // This was working before, can't figure out where it went wrong
        binding.turnOffAlarm.setOnClickListener{
            unbindService(serviceConnection)
            ringtoneService?.stop()
            finish()
        }
    }
}
