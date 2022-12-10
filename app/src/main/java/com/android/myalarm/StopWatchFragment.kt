package com.android.myalarm

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.myalarm.databinding.FragmentStopWatchBinding


/**
 */
class StopWatchFragment : Fragment() {
    private var _binding: FragmentStopWatchBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStopWatchBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chronometerStopWatch.format = "%s:%S.%M"
        binding.chronometerStopWatch.base = SystemClock.elapsedRealtime()
        binding.chronometerStopWatch.start()


//        val textViewUpdaterThread = Thread {
//            while (true) {
//                runOnUiThread {
//                    // get the current time in milliseconds
//                    val currentTime = SystemClock.elapsedRealtime()
//
//                    // convert the time to seconds and milliseconds
//                    val seconds = currentTime / 1000
//                    val milliseconds = currentTime % 1000
//
//                    // update the text view with the current time
//                    textView.text = "$seconds.$milliseconds"
//                }
//                Thread.sleep(1)
//            }
//        }

        var pls = 0
        binding.startStopWatch.setOnClickListener { v ->
            val thread = Thread {
                while (true) {
                    activity?.runOnUiThread {
                        // get the current time in milliseconds
                        val currentTime = SystemClock.elapsedRealtime()

                        pls += 1

                        // convert the time to seconds and milliseconds
                        val seconds = currentTime / 1000
                        val milliseconds = currentTime % 1000

                        // update the text view with the current time
                        binding.stopWatchView.text = pls.toString()
                    }
                    Thread.sleep(1)
                }
            }
            // start the thread
            thread.start()
        }

//        binding.chronometerStopWatch.setOnChronometerTickListener {
//            // its only updating every second, not every millisecond
//            val milliseconds = SystemClock.elapsedRealtime()
//            binding.stopWatchView.text = milliseconds.toString()
//        }


//        stopWatch()
    }


    private fun stopWatch() {
        // On the first click create a variable: (START BUTTON)
        val startTime = System.currentTimeMillis()

        // MAYBE USE THIS INSTEAD
        // val startTime = SystemClock.elapsedRealtime()

        // Then on the second click you can calculate the difference: (STOP BUTTON)
        val difference = System.currentTimeMillis() - startTime
        binding.stopWatchView.text = startTime.toString()
        // difference / 1000 will give you the difference in seconds.

    }
}