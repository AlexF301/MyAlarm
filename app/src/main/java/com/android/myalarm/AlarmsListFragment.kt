package com.android.myalarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.myalarm.database.Alarm
import com.android.myalarm.databinding.FragmentAlarmsListBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AlarmsListFragment : Fragment() {

    private var _binding: FragmentAlarmsListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

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

    }

    /**
     *
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AlarmAdapter(
        private var dataSet: MutableList<Alarm>, val onDeleteCallback: (Alarm) -> Unit
    ) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            var alarmTime: TextView = itemView.findViewById(R.id.view_alarm_time)
            var daysSelected: TextView = itemView.findViewById(R.id.days_set)
            var alarmIsSet: CheckBox = itemView.findViewById(R.id.checkBox)
            var alarmType : TextView = itemView.findViewById(R.id.alarm_type_show)

            init {
                // Define click listener for the ViewHolder's View.
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {
            }
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.alarm_item, viewGroup, false)
            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            val alarm: Alarm = dataSet[position]

            viewHolder.alarmTime.text = convertTo12HrView(alarm.Hour, alarm.minute)
            viewHolder.alarmType.text = alarm.type.toString()
            viewHolder.alarmIsSet.isChecked = alarm.isOn
            viewHolder.daysSelected.text = formatDaysSelected(alarm.days)
            viewHolder.alarmIsSet.setOnClickListener {
                onDeleteCallback(alarm)
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

        private fun formatDaysSelected(list: List<String>): String {
            return list.toString().replace("[", "").replace("]", "");
        }

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
//
//        fun removeAt(adapterPosition: Int,vm : AlarmViewModel) {
//            vm.delete(dataSet[adapterPosition])
//            notifyItemRemoved(adapterPosition)
//        }
    }
}