package com.android.myalarm

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.android.myalarm.databinding.FragmentNotificationsContextDialogBinding


class NotificationsContextDialogFragment : DialogFragment(), View.OnClickListener {

    companion object {
        const val TAG = "NotificationsRequestDialog"

        /** The key used to send results back from fragment requests */
        const val REQUEST_KEY_PERMISSIONS = "NotificationsContextDialogFragment.RESPONSE"

        /** The key used for the selected time in the result bundle */
        const val BUNDLE_KEY_ = "RESPONSE"
    }

    /** binding for the views of the fragment (nullable version) */
    private var _binding: FragmentNotificationsContextDialogBinding? = null

    /** binding for the views of this fragment (non-nullable accessor)
    This property is only valid between onCreateView and onDestroyView
     */
    private val binding: FragmentNotificationsContextDialogBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsContextDialogBinding.inflate(inflater, container, false)

        // UI configuration to have shaped custom dialog fragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.allow.setOnClickListener(this)
        binding.cancel.setOnClickListener(this)

        return binding.root
    }

    /** Set layout params after the view has been created */
    override fun onStart() {
        super.onStart()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            setDialogLayout(50)
        else
            setDialogLayout(80)
    }

    /**
     * Call this method (in onStart) to set
     * the width of the dialog to a percentage of the current
     * screen width.
     */
    private fun setDialogLayout(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            // dismiss the dialog
            binding.cancel.id -> dialog?.dismiss()
            // return to MainActivity that the use wants to allow the notification permission
            // only care if the user wants to accept the permission, can't really do anything at this
            // moment if they select to not allow the permission.
            binding.allow.id -> {
                setFragmentResult(REQUEST_KEY_PERMISSIONS, bundleOf(BUNDLE_KEY_ to true))
                dialog?.dismiss()
            }
        }
    }
}