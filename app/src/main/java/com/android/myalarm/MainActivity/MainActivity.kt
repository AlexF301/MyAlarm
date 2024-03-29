package com.android.myalarm.MainActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import com.android.myalarm.AlarmsListFragment
import com.android.myalarm.NotificationsContextDialogFragment
import com.android.myalarm.R
import com.android.myalarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** Shared Preference object. Being used to save attempts at asking for permissions */
    private lateinit var preferences: SharedPreferences

    private val viewModel : MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences =
            getSharedPreferences(getString(R.string.permission_pref), Context.MODE_PRIVATE)

        navigationBarSetup()

        setFragmentResultListenerForPermissionResponse(
            AlarmsListFragment.REQUEST_KEY_PERMISSION_PROMPT,
            AlarmsListFragment.BUNDLE_KEY_PERMISSION_PROMPT
        )

        // The post notifications permission is only available for sdk's 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requestNotificationPermission()
    }

    /**
     * Register the permissions callback, which handles the user's response to the
     * system permissions dialog. Save the return value, an instance of
     * ActivityResultLauncher.
     *
     * Calling updateRequestedPermissionPreference() in both to signify that the initial prompt
     * has been used
     */
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                updateRequestedPermissionPreference(R.string.requested_permission)
            } else {
                // This else means the permission has been denied, so if we've requested at least once
                // and the app is not requesting a rationale, then we confirm that user has denied
                // twice
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                    && preferences.getBoolean(getString(R.string.requested_permission), false)
                )
                    updateRequestedPermissionPreference(R.string.denied_twice)

                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))
                    updateRequestedPermissionPreference(R.string.requested_permission)
            }
        }

    /** Request the POST_NOTIFICATIONS permission that is required for android sdk 33+ */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (!viewModel.isDialogShown) {
            // permission dialog will only appear once during apps current lifecycle
            requestPermission(Manifest.permission.POST_NOTIFICATIONS)
            // dialog was shown to at least once in the current lifecycle
            viewModel.isDialogShown = true
        }
    }


    /** Generic method that takes the permission to request as a parameter */
    private fun requestPermission(permission: String) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined.

                // Android 11 and up, once a permissions is initially denied, future attempts are
                // prevented and the app can only try once more to display the permission prompt.
                // This is the second attempt
                val dialogFragment = NotificationsContextDialogFragment()
                dialogFragment.show(supportFragmentManager, NotificationsContextDialogFragment.TAG)

                // Second attempt to request the notification when app launches
                setFragmentResultListenerForPermissionResponse(
                    NotificationsContextDialogFragment.REQUEST_KEY_PERMISSION_REQUEST,
                    NotificationsContextDialogFragment.BUNDLE_KEY_PERMISSION_REQUEST
                )
            }

            else -> {
                // Directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    /**
     * Updates a boolean preference to true
     */
    private fun updateRequestedPermissionPreference(stringID: Int) {
        val editor = preferences.edit()
        editor.putBoolean(getString(stringID), true)
        editor.apply()
    }

    /**
     * Sets up a FragmentResultListener on the provided request key (since asking
     * can occur from two different spots) to get the response whether the user wants to allow
     * the POST_NOTIFICATIONS permission
     *
     * https://developer.android.com/static/images/training/permissions/workflow-runtime.svg)
     *
     * Only set up to listen when a user selects "allow" on the dialog which returns true. a return
     * value of true -> display the prompt to request the permission.
     *
     * If the user selected "cancel" on the dialog then we shouldn't do anything more at this time
     */
    @SuppressLint("InlinedApi")
    private fun setFragmentResultListenerForPermissionResponse(
        requestKey: String,
        bundleKey: String,
    ) {
        supportFragmentManager.setFragmentResultListener(requestKey, this) { _, bundle ->
            val result = bundle.getBoolean(bundleKey)
            // A return value of true -> display the prompt to request the permission.
            if (result) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
                    binding.navHostFragment.findNavController()
                        .navigate(R.id.nav_stopWatchFragment)
                    true
                }

                else -> false
            }
        }
    }
}