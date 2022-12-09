package com.android.myalarm

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.android.myalarm.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    binding.navHostFragment.findNavController().navigate(R.id.nav_alarmsListFragment)
                    true
                }
                R.id.timer_fragment_tab -> {
                    //findNavController(binding.navHostFragment.id).navigateUp()
                    binding.navHostFragment.findNavController().navigate(R.id.nav_timerFragment)
                    true
                }
                R.id.stopwatch_fragment_tab -> {
                    binding.navHostFragment.findNavController().navigate(R.id.nav_stopWatchFragment)
                    true
                }
                else -> false
            }
        }
    }
}