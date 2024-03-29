package com.android.myalarm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import java.util.*
import kotlin.math.max
import kotlin.math.min


/**
 * A simple [Fragment] subclass as the default destination in the navigation. Responsible for
 * displaying the list of alarms.
 */
class AlarmsListFragment : Fragment() {

    companion object {
        /** The key used to send results back from fragment requests */
        const val REQUEST_KEY_PERMISSION_PROMPT = "AlarmsListFragment.REQUEST_KEY"

        /** The key used for the selected time in the result bundle */
        const val BUNDLE_KEY_PERMISSION_PROMPT = "AlarmsListFragment.RESPONSE"
    }

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

    /** Shared Preference object. Being used to save attempts at asking for permissions */
    private lateinit var preferences: SharedPreferences

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

        preferences = requireActivity().getSharedPreferences(
            getString(R.string.permission_pref),
            Context.MODE_PRIVATE
        )

        // Use coroutine to collect alarms from the database
        lifecycleScope.launch {
            viewModel.alarms.collect {
                alarms = it
                adapter.notifyDataSetChanged()
            }
        }

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // navigate to AlarmFragment when button is clicked
        binding.createAlarmButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // check whether the POST_NOTIFICATIONS permission is enabled
                if (notificationManager.areNotificationsEnabled())
                // Provide a random UUID, this is messy as this id doesn't get used but needed
                    findNavController().navigate(
                        AlarmsListFragmentDirections.createAlarm(
                            UUID.randomUUID().toString()
                        )
                    )
                else
                    // if POST_NOTIFICATIONS not enabled, prevent the user from creating an alarm
                    // and display a helpful IU to do so
                    displayNotificationPermissionContext()
            }
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
     * Displays to the user the reasoning for why the POST_NOTIFICATIONS permission need to be enabled
     * and listens for a response. The permission workflow suggest asking direct permission from
     * the user with the permission prompt dialog provided by the system
     *
     * Using SharedPreferences, we check if the user has already denied that prompt twice to either
     * show them the prompt again if they haven't denied it twice, Or if they have, send the user
     * to the app notification settings on the users device to request the permission to continue
     * using the feature to set alarms (which require notification so a user can actually receive them)
     */
    private fun displayNotificationPermissionContext() {
        setFragmentResultListener(
            NotificationsContextDialogFragment.REQUEST_KEY_PERMISSION_REQUEST,
        ) { _, bundle ->
            val result = bundle.getBoolean(NotificationsContextDialogFragment.BUNDLE_KEY_PERMISSION_REQUEST)

            if (result) {
                if (preferences.getBoolean(getString(R.string.denied_twice), false))
                    launchAppNotificationSettings()
                else {
                    requireActivity().supportFragmentManager.setFragmentResult(REQUEST_KEY_PERMISSION_PROMPT, bundleOf(BUNDLE_KEY_PERMISSION_PROMPT to true))
                }
            }
        }

        findNavController().navigate(AlarmsListFragmentDirections.enableNotificationsAnnouncement())
    }

    /**
     * Launches an intent to the device settings where the applications notifications settings are
     * located for the user to enable the permission.
     * This is the last resort of the permission workflow of the app
     */
    private fun launchAppNotificationSettings() {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // SDk 26 +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
        } else { // SDK 24 - 25 (since current min supported sdk is 24)
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context?.packageName)
            intent.putExtra("app_uid", context?.applicationInfo?.uid)
        }

        startActivity(intent)
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
                max(
                    min(255f * 2 * (kotlin.math.abs(dX) - iconMarginWidth) / itemView.width, 255f),
                    0f
                ).toInt()
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