package com.android.myalarm

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.android.myalarm.database.AlarmType
import com.android.myalarm.databinding.AlarmTypeItemBinding
import com.android.myalarm.databinding.FragmentAlarmTypeListBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class AlarmTypeFragment : BottomSheetDialogFragment() {

    companion object {
        /** All event types */
        private val ALARM_TYPES = AlarmType.values()

        /** The key used to send results back from fragment requests for event types */
        const val REQUEST_KEY_ALARM_TYPE = "AlarmTypeFragment.ALARM_TYPE"

        /** The key used for the selected time in the result bundle */
        const val BUNDLE_KEY_ALARM_TYPE = "ALARM_TYPE"
    }

    /** binding for the views of the fragment (nullable version) */
    private var _binding: FragmentAlarmTypeListBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
    This property is only valid between onCreateView and onDestroyView
     */
    private val binding: FragmentAlarmTypeListBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    /**
     * List of available Alarm Types
     */
    private val alarmTypes: List<AlarmType> = listOf(AlarmType.Regular, AlarmType.Shake)

    /**
     * Description for each type of Alarm Type. Purposely in same order as alarmTypes. When providing
     * to the recyclerview adapter, descriptions needs to coincide with the Alarm types they are
     * supposed to define (recyclerview adapter position)
     */
    private val alarmTypesDescriptions: List<Int> =
        listOf(R.string.regular_description, R.string.shake_description)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmTypeListBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.list.adapter = AlarmTypeAdapter(alarmTypes, alarmTypesDescriptions)
    }

    /**
     * Clean up memory by clearing the binding
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Need to call onCreate in order to make a callback for whenever back button on device is pressed
     * OnBackPress cannot be by itself in modal bottom sheet Fragment.
     * dismisses the fragment on back press key
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dismiss()
            }
        })
    }

    /**
     * layout configuration for the bottom sheet fragment to display correctly
     */
    override fun onStart() {
        super.onStart()

        val mBottomBehavior = BottomSheetBehavior.from(requireView().parent as View)
        mBottomBehavior.maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
        mBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    /**
     * When fragment is dismissed, flags get cleared along with it
     */
    override fun dismiss() {
        super.dismiss()
        dialog?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    /**
     * Provide a reference to the type of views that you are using
     */
    private inner class AlarmTypeViewHolder(val binding: AlarmTypeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alarmType: AlarmType, description: Int) {
            binding.apply {
                alarmTypeName.text = alarmType.name
                alarmTypeDesc.text = getString(description)
                typeCard.setOnClickListener {
                    setFragmentResult(
                        REQUEST_KEY_ALARM_TYPE, bundleOf(
                            BUNDLE_KEY_ALARM_TYPE to ALARM_TYPES[bindingAdapterPosition]
                        )
                    )
                    findNavController().popBackStack()
                }
            }
        }
    }

    /**
     * The adapter for the alarms recycler view. Populates the UI with the alarms the user has
     * created
     */
    private inner class AlarmTypeAdapter(
        private var alarmTypes: List<AlarmType>,
        private val descriptions: List<Int>
    ) : RecyclerView.Adapter<AlarmTypeViewHolder>() {

        /** Creates a View holder by inflating the alarm_type_item layout */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmTypeViewHolder =
            AlarmTypeViewHolder(
                AlarmTypeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        /** Replace the contents of a view with the data values at the position of the list */
        override fun onBindViewHolder(viewHolder: AlarmTypeViewHolder, position: Int) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.bind(alarmTypes[position], descriptions[position])
        }

        /** Return the size of the alarmTypes list */
        override fun getItemCount() = alarmTypes.size
    }
}