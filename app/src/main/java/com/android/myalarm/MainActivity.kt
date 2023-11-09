package com.android.myalarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import com.android.myalarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigationBarSetup()

        // The post notifications permission is only available for sdk's 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requestNotificationPermission()
    }

    /**
     * Register the permissions callback, which handles the user's response to the
     * system permissions dialog. Save the return value, an instance of
     * ActivityResultLauncher. You can use either a val, as shown in this snippet,
     * or a lateinit var in your onAttach() or onCreate() method.
     */
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

    /** Request the POST_NOTIFICATIONS permission that is required for android sdk 33+ */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        requestPermission(Manifest.permission.POST_NOTIFICATIONS)
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
                Log.w("here", "here")
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined.

                // Android 11 and up, once a permissions is initially denied, future attempts are
                // prevented and the app can only try once more to display the permission prompt.
                // This is the second attempt workflow
                val dialogFragment = NotificationsContextDialogFragment()
                dialogFragment.show(supportFragmentManager, NotificationsContextDialogFragment.TAG)

                setNotificationsContextDialogListener()
            }

            else -> {
                // Directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    /**
     * Sets up a FragmentResultListener on the NotificationsContextDialog to get the response
     * whether the user wants to allow the POST_NOTIFICATIONS permission
     *
     * This should only trigger as a second attempt to request the notification (following the
     * workflow for requesting runtime permissions):
     *
     * https://developer.android.com/static/images/training/permissions/workflow-runtime.svg)
     *
     * Only set up to listen when a user selects "allow" on the dialog which returns true. a return
     * value of true -> display the prompt to request the permission.
     *
     * If the user selected "cancel" on the dialog then we shouldn't do anything more at this time
     */
    @SuppressLint("InlinedApi")
    private fun setNotificationsContextDialogListener() {
        supportFragmentManager
            .setFragmentResultListener(
                NotificationsContextDialogFragment.REQUEST_KEY_PERMISSIONS,
                this
            ) { _, bundle ->
                val result = bundle.getBoolean(NotificationsContextDialogFragment.BUNDLE_KEY_)
                // a return value of true -> display the prompt to request the permission.
                if (result)
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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