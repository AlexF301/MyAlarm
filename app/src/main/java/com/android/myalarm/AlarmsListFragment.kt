package com.android.myalarm

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.myalarm.database.Alarm
import com.android.myalarm.database.DayOfTheWeek
import com.android.myalarm.databinding.AlarmItemBinding
import com.android.myalarm.databinding.FragmentAlarmsListBinding
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.util.*
import java.util.Collections.max
import java.util.Collections.min
import kotlin.math.max
import kotlin.math.min

/**
 * A simple [Fragment] subclass as the default destination in the navigation. Responsible for
 * displaying the list of alarms.
 */
class AlarmsListFragment : Fragment() {

    /** binding for the views of the fragment (nullable version) */
    private var _binding: FragmentAlarmsListBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
    This property is only valid between onCreateView and onDestroyView
     */
    private val binding: FragmentAlarmsListBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    /** The viewModel for the views of the fragment */
    private val viewModel: AlarmsViewModel by viewModels()

    /** the list of alarms */
    private var alarms: List<Alarm> = emptyList()

    /** inflate the binding view for this fragment */
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
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAlarms()

        val adapter = AlarmAdapter()
        binding.alarmsList.adapter = adapter
        ItemTouchHelper(SwipeToDeleteCallback()).attachToRecyclerView(binding.alarmsList)

        // Use coroutine to collect alarms from the database
        lifecycleScope.launch {
            viewModel.alarms.collect {
                alarms = it
                adapter.notifyDataSetChanged()
            }
        }

        // navigate to AlarmFragment when button is clicked
        binding.createAlarmButton.setOnClickListener {
            // Provide a random UUID, this is messy as this id doesn't get used but needed
            findNavController().navigate(AlarmsListFragmentDirections.createAlarm(UUID.randomUUID().toString()))
        }
    }

    /**
     * Clean up memory by clearing the binding
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Formats the days of the week to be user friendly
     */
    private fun formatDaysSelected(list: MutableList<DayOfTheWeek>): String {
        return list.toString().replace("[", "").replace("]", "")
    }

    /**
     * Converts time into a user friendly format
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
                alarmStatusButton.isChecked = alarm.alarmState
                daysSet.text = formatDaysSelected(alarm.daysSelected)
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

    /**
     * Delete the alarm from the calendar database. This requires a coroutine.
     * @param alarm the alarm to remove from the database
     */
    fun deleteAlarm(alarm: Alarm) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteAlarm(alarm)
        }
    }


    /**
     * A touch helping callback to add swipe action to the RecyclerView to
     * support deleting items.
     */
    private inner class SwipeToDeleteCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        private val icon = ContextCompat.getDrawable(requireContext(), R.drawable.delete)!!
        private val background = ColorDrawable(Color.RED)

        /** For up/down swipes we do nothing */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) =
            false

        /** For left/right swipes delete the item */
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
            deleteAlarm(alarms[viewHolder.bindingAdapterPosition])

        /**
         * Draw the element. This draws the child normally except for the red background with the
         * delete icon while swiping to show we are deleting it.
         */
        override fun onChildDraw(
            c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView

            // Draw the red tinted background
            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
            val iconMarginWidth = iconMargin + icon.intrinsicWidth
            val alpha =
                max(min(255f * 2 * (kotlin.math.abs(dX) - iconMarginWidth) / itemView.width, 255f), 0f).toInt()
            background.color = 0xFF0000 or (alpha / 2 shl 24)
            background.setBounds(itemView.left, itemView.top, itemView.right, itemView.bottom)
            background.draw(c)

            // Draw the regular view holder
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            // Draw the trash icon
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + icon.intrinsicHeight
            val iconLeft =
                if (dX > 0) itemView.left + iconMargin else itemView.right - iconMarginWidth
            val iconRight =
                if (dX > 0) itemView.left + iconMarginWidth else itemView.right - iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon.alpha = alpha
            icon.draw(c)
        }
    }


}