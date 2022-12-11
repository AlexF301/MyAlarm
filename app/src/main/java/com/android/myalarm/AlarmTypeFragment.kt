package com.android.myalarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.android.myalarm.database.AlarmType
import com.android.myalarm.databinding.AlarmTypeItemBinding
import com.android.myalarm.databinding.FragmentAlarmTypeListBinding

/**
 * A fragment representing a list of Items.
 */
class AlarmTypeFragment : Fragment() {

    /** binding for the views of the fragment (nullable version) */
    private var _binding: FragmentAlarmTypeListBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
    This property is only valid between onCreateView and onDestroyView
     */
    private val binding: FragmentAlarmTypeListBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    /** The viewModel for the current accessed Alarm */
    private val alarmViewModel: AlarmViewModel by viewModels()

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
     * Provide a reference to the type of views that you are using
     */
    private inner class AlarmTypeViewHolder(val binding: AlarmTypeItemBinding) :
        RecyclerView.ViewHolder(binding.root){

        fun bind(alarmType : AlarmType, description: Int) {
            binding.apply {
                alarmTypeName.text = alarmType.name
                alarmTypeDesc.text = getString(description)
                typeCard.setOnClickListener{
                    alarmViewModel.type = alarmType
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


//    private fun returnScrollingFragment(alarmType: AlarmType) {
//        vm.type = alarmType
//        val scrollingFragment = ScrollingFragment()
//        activity?.supportFragmentManager?.beginTransaction()!!
//            .replace(((view as ViewGroup).parent as View).id, scrollingFragment)
//            .commit()
//        val button = requireActivity().findViewById<Button>(R.id.create_alarm)
//        button.isVisible = true
//    }
