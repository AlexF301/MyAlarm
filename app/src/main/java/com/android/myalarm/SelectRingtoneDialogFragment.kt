package com.android.myalarm

import android.content.Intent
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.android.myalarm.alarmSupport.RingtoneService
import com.android.myalarm.database.AlarmType
import com.android.myalarm.databinding.FragmentListBinding
import com.android.myalarm.databinding.RingtoneBoxBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectRingtoneDialogFragment : BottomSheetDialogFragment() {
    companion object {
        /** The key used to send results back from fragment requests for event types */
        const val REQUEST_KEY = "SelectRingtoneDialogFragment.RINGTONE"

        /** The key used for the selected time in the result bundle */
        const val BUNDLE_KEY = "RINGTONE"
    }

    /** binding for the views of the fragment (nullable version) */
    private var _binding: FragmentListBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
    This property is only valid between onCreateView and onDestroyView
     */
    private val binding get() = _binding!!

    private val vm: AlarmViewModel by activityViewModels()

    /** The Ringtones on the device */
    private val ringtones: HashMap<String, Uri> = hashMapOf()

    /**
     *
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        container?.removeAllViews()

        //get ringtones saved on device
        accessRingtones()

        _binding = FragmentListBinding.inflate(inflater, container, false)

        binding.list.adapter = RingtoneAdapter(ringtones)

        return binding.root
    }

    /**
     * When Dialog is dismissed, stop any ringtone currently playing
     */
    override fun dismiss() {
        super.dismiss()
        stopRingtoneIfPlaying()
    }

    /**
     * Provides a Map of all the ringtones on the device with the name of the ringtone file being
     * the name and the URI of the ringtone to be the value. The URI is the necessary to be able to
     * play the ringtone sound
     */
    private fun accessRingtones(): HashMap<String, Uri> {
        val manager = RingtoneManager(activity)
        manager.setType(RingtoneManager.TYPE_RINGTONE)
        val cursor: Cursor = manager.cursor

        while (cursor.moveToNext()) {
            val ringtoneName = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            val uri =
                cursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/" + cursor.getString(
                    RingtoneManager.ID_COLUMN_INDEX
                )
            val ringtoneUri: Uri = Uri.parse(uri)
            ringtones[ringtoneName] = ringtoneUri
        }
        return ringtones
    }

    /**
     * Provide a reference to the type of views that you are using
     */
    private inner class RingtoneViewHolder(val binding: RingtoneBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.apply {
            }
        }
    }

    /**
     * The adapter for the alarms recycler view. Populates the UI with the alarms the user has
     * created
     * @param ringtones: all the available ringtones on the device. Ringtone name points to its uri
     * to be able to play for user
     */
    private inner class RingtoneAdapter(private var ringtones: HashMap<String, Uri>) :
        RecyclerView.Adapter<RingtoneViewHolder>() {

        var lastPosition: Int = -1

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder =
            // Create a new view, which defines the UI of the list item
            RingtoneViewHolder(
                RingtoneBoxBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )


        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: RingtoneViewHolder, position: Int) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            with(viewHolder){
                var ringtoneUri: Uri?
                for (key in ringtones.keys) {
                    ringtoneUri = ArrayList<Uri>(ringtones.values)[bindingAdapterPosition]
                    val ringtone: String = ArrayList<String>(ringtones.keys)[bindingAdapterPosition]
                    binding.ringtoneText.text = ringtone
                    binding.selectRingtone.isChecked = bindingAdapterPosition == lastPosition
                    binding.ringtonesCard.setOnClickListener {
                        stopRingtoneIfPlaying()
                        playRingtone(ringtoneUri)

                        val copyOfLastCheckedPosition: Int = lastPosition
                        lastPosition = bindingAdapterPosition
                        notifyItemChanged(copyOfLastCheckedPosition)
                        notifyItemChanged(lastPosition)
                    }
                }
            }
        }

        /** Return the size of your dataset (invoked by the layout manager)
        // Dummy code, Won't be used (I think) but necessary for recyclerView implementation
         **/
        override fun getItemCount() = ringtones.size
    }

    /**
     * Play the ringtone selected
     */
    private fun playRingtone(ringtone: Uri?) {
        Log.w("here", ringtone.toString())
        val playRingtone = Intent(context, RingtoneService::class.java)
        playRingtone.putExtra("ringtone_selected", ringtone.toString())
        requireActivity().startService(playRingtone)
    }

    /**
     * stop any previous ringtones that were selected from playing
     */
    private fun stopRingtoneIfPlaying() {
        val stopRingtone = Intent(context, RingtoneService::class.java)
        requireActivity().stopService(stopRingtone)
    }
}