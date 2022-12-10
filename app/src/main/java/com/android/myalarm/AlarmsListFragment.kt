package com.android.myalarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.myalarm.database.Alarm
import com.android.myalarm.databinding.AlarmItemBinding
import com.android.myalarm.databinding.FragmentAlarmsListBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AlarmsListFragment : Fragment() {

    /** binding for the views of the fragment (nullable version) */
    private var _binding: FragmentAlarmsListBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
    This property is only valid between onCreateView and onDestroyView
     */
    private val binding: FragmentAlarmsListBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    /**
     *
     */
    private val viewModel: AlarmViewModel by viewModels()

    /**
     * the list of alarms
     */
    private var alarms: List<Alarm> = emptyList()

    /**
     * inflate the binding view for this fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmsListBinding.inflate(inflater, container, false)

        return binding.root
    }

    /**
     * Navigates to AlarmFragment() when user clicks on the button to make alarms
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AlarmAdapter()
        binding.alarmsList.adapter = adapter

        // Use coroutine to collect alarms from the database
        lifecycleScope.launch {
            viewModel.alarms.collect {
            }
        }

        // navigate to AlarmFragment when button is clicked
        binding.createAlarmButton.setOnClickListener {
            findNavController().navigate(AlarmsListFragmentDirections.createAlarm())
        }
    }

    /**
     *
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     *
     */
    private fun formatDaysSelected(list: List<String>): String {
        return list.toString().replace("[", "").replace("]", "");
    }

    /**
     *
     */
    private fun convertTo12HrView(hour: Int, minute: Int): String {
        val formatMin = String.format("%02d", minute)
        return if (hour >= 12) {
            val amHour: Int = hour - 12
            if (hour == 12)
                "12:$formatMin PM"
            else
                "$amHour:$formatMin PM"
        } else {
            if (hour == 0)
                "12:$formatMin AM"
            else
                "$hour:$formatMin AM"
        }
    }

    /**
     * Provide a reference to the type of views that you are using
     */
    private inner class AlarmViewHolder(val binding: AlarmItemBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        fun bind(alarm: Alarm) {
            binding.apply {
                alarmTime.text = convertTo12HrView(alarm.hour, alarm.minute)
                alarmType.text = alarm.type.toString()
                alarmStatusButton.isChecked = alarm.isOn
                daysSet.text = formatDaysSelected(alarm.days)
            }
        }

        init {
            // Define click listener for the ViewHolder's View.
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
        }
    }

    /**
     * The adapter for the alarms recycler view. Populates the UI with the alarms the user has
     * created
     */
    private inner class AlarmAdapter : RecyclerView.Adapter<AlarmViewHolder>() {

        /** Creates a Viewholder by inflating the alarm_item layout */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder =
            AlarmViewHolder(
                AlarmItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        /** Replace the contents of a view with the data values at the position of the list */
        override fun onBindViewHolder(viewHolder: AlarmViewHolder, position: Int) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.bind(alarms[position])

//            viewHolder.alarmIsSet.setOnClickListener {
//                onDeleteCallback(alarm)
//            }
        }

        /** Return the size of the alarms list */
        override fun getItemCount() = alarms.size
//
//        fun removeAt(adapterPosition: Int,vm : AlarmViewModel) {
//            vm.delete(dataSet[adapterPosition])
//            notifyItemRemoved(adapterPosition)
//        }
    }
}