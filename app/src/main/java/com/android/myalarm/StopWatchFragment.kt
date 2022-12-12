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

    /** milliseconds of the stopwatch */
    private var millis : Double = 0.0

    /** Seconds of the current stopwatch  */
    private var seconds = 0L

    /** Minutes of the current stopwatch  */
    private var minutes = 0L

    /** Hours of the current stopwatch */
    private var hours = 0L

    /** Milliseconds of the current stopwatch  */
    private var milliseconds = 0L

    /** click counter to manipulate button strings  */
    private var clickCounter = 0

    /** milliseconds that are used to reformat stopwatch time */
    var accumulatedTime = 0L


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
        binding.stopWatchView.text = "$minutes:$seconds:$milliseconds"

        val handler = Handler(Looper.getMainLooper())
        val stopwatch = object: Runnable {
            override fun run() {
                millis += 2.15
                accumulatedTime = millis.toLong()

                minutes = TimeUnit.MILLISECONDS.toMinutes(accumulatedTime)
                seconds = TimeUnit.MILLISECONDS.toSeconds(accumulatedTime) - TimeUnit.MINUTES.toSeconds(minutes)
                milliseconds = accumulatedTime - TimeUnit.SECONDS.toMillis(seconds) - TimeUnit.MINUTES.toMillis(minutes)

                binding.stopWatchView.text = "$minutes:$seconds:$milliseconds"
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
            }
            if (clickCounter % 2 == 0) {
                handler.removeCallbacks(stopwatch)
                binding.startStopWatch.text = getString(R.string.resume)
            } else {
                if (clickCounter != 1) {
                    millis -= 2
                    binding.startStopWatch.text = getString(R.string.pause)
                }
            }
        }


        // Triggered when the stop button is clicked
        binding.stopStopWatch.setOnClickListener {
            binding.stopStopWatch.isEnabled = false
            millis = 0.0
            clickCounter = 0
            binding.startStopWatch.text = getString(R.string.start)
            binding.stopWatchView.text = millis.toString()
            handler.removeCallbacks(stopwatch)
        }

    }


}