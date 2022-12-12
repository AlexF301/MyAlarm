package com.android.myalarm

import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import com.android.myalarm.databinding.ActivityMainBinding
import java.security.Permission

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //installSplashScreen()
        setContentView(binding.root)

        // TODO: AlerDialog or something to tell user to give app permssions

        verifySystemPermissionForSettingExactAlarms()
        navigationBarSetup()
    }

    /**
     * Responds to item navigation bar item clicks by navigating to the fragment that corresponds
     * with each item.
     */
    private fun navigationBarSetup() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.alarms_list_fragment_tab -> {
                    binding.navHostFragment.findNavController().navigateUp()
                    binding.navHostFragment.findNavController()
                        .navigate(R.id.nav_alarmsListFragment)
                    true
                }
                R.id.timer_fragment_tab -> {
                    binding.navHostFragment.findNavController().navigateUp()
                    binding.navHostFragment.findNavController().navigate(R.id.nav_timerFragment)
                    true
                }
                R.id.stopwatch_fragment_tab -> {
                    binding.navHostFragment.findNavController().navigateUp()
                    binding.navHostFragment.findNavController().navigate(R.id.nav_stopWatchFragment)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * To schedule exact alarms, user must provide system permissions to allow for setting Alarms
     * and Reminders. Redirects user to these settings if permission is not provided.
     */
    private fun verifySystemPermissionForSettingExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager =
                ContextCompat.getSystemService(applicationContext, AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }
    }

    private fun verifyPermissions() {
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher. You can use either a val, as shown in this snippet,
        // or a lateinit var in your onAttach() or onCreate() method.
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                }
            }
    }

    private fun getPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission_group.NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(
                android.Manifest.permission_group.NOTIFICATIONS
            ) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                //showInContextUI(...)
            }
            else -> {
                // You can directly ask for the permission.
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission_group.NOTIFICATIONS,
                        android.Manifest.permission.SCHEDULE_EXACT_ALARM
                    ),
                    0
                )
            }
        }
    }
}