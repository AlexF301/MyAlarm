package com.android.myalarm

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.myalarm.databinding.FragmentStopWatchBinding
import java.util.concurrent.TimeUnit


/**
 * A fragment that allows a user to start, pause, resume, and stop a stopwatch.
 */
class StopWatchFragment : Fragment() {

    /** Binding for the views of the fragment (nullable version) */
    private var _binding: FragmentStopWatchBinding? = null

    /** Binding for the views of the fragment (non-nullable accessor) */
    private val binding get() = _binding!!

    /** click counter to manipulate button strings  */
    private var clickCounter = 0


    /** Creates the binding view for this layout */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStopWatchBinding.inflate(inflater, container, false)
        return binding.root
    }



    @SuppressLint("SetTextI18n")
    /**
     * Once the view is created, we create a handler with a runnable object to execute an incrementer
     * every millisecond to simulate stopwatch functionality. Proceeds to format the milliseconds
     * generated to be more user friendly. The user is then able to interact with the
     * start, pause, resume, and stop buttons.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.stopWatchView.text = getString(R.string.default_time)

        val handler = Handler(Looper.getMainLooper())
        val stopwatch = object: Runnable {
            var accumulatedTime: Long = 0L
            var started: Long = 0L

            override fun run() {
                val now = System.currentTimeMillis()
                if (started == 0L) {
                    started = now
                }

                val elapsed = now - started + accumulatedTime

                val minutes = (elapsed / 1000) / 60
                val seconds = (elapsed / 1000) % 60
                val milliseconds = elapsed % 1000

                binding.stopWatchView.text = getString(R.string.display_time, minutes, seconds, milliseconds)
                handler.postDelayed(this, 1)
            }
        }


        // Triggered when the start button is clicked
        binding.startStopWatch.setOnClickListener {
            binding.stopStopWatch.isEnabled = true
            clickCounter ++
            handler.post(stopwatch)

            // initial start
            if (clickCounter == 1) {
                binding.startStopWatch.text = getString(R.string.pause)
                stopwatch.started = 0
                stopwatch.accumulatedTime = 0
            } else if (clickCounter % 2 == 0) {
                handler.removeCallbacks(stopwatch)
                binding.startStopWatch.text = getString(R.string.resume)
                stopwatch.accumulatedTime += System.currentTimeMillis() - stopwatch.started
                stopwatch.started = 0
            } else {
                binding.startStopWatch.text = getString(R.string.pause)
            }
        }


        // Triggered when the stop button is clicked
        binding.stopStopWatch.setOnClickListener {
            binding.stopStopWatch.isEnabled = false
            stopwatch.started = 0
            stopwatch.accumulatedTime = 0
            clickCounter = 0
            binding.startStopWatch.text = getString(R.string.start)
            handler.removeCallbacks(stopwatch)
        }

    }


}