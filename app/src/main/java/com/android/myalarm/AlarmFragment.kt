package com.android.myalarm

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import com.android.myalarm.databinding.DayPickerBinding
import com.android.myalarm.databinding.FragmentAlarmBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding: FragmentAlarmBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    private var _daypickerbinding: DayPickerBinding? = null
    private val dayPickerBinding: DayPickerBinding
        get() = checkNotNull(_daypickerbinding) { getString(R.string.binding_failed) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        _daypickerbinding = DayPickerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}