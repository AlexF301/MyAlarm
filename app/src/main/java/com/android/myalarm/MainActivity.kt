package com.android.myalarm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.myalarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewpager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO: alarm, timer, and stopwatch as a viewpager2?
        viewpager = binding.viewPager2
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val myAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

        //add Fragments in your ViewPagerFragmentAdapter class
        myAdapter.addFragment(AlarmsListFragment())
        myAdapter.addFragment(TimerFragment())
        myAdapter.addFragment(StopWatchFragment())

        // set Orientation in your ViewPager2
        viewpager.adapter = myAdapter
    }
}