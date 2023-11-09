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


    @SuppressLint("NewApi")
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

//    /**
//     * Register the permissions callback, which handles the user's response to the
//     * system permissions dialog. Save the return value, an instance of
//     * ActivityResultLauncher. You can use either a val, as shown in this snippet,
//     * or a lateinit var in your onAttach() or onCreate() method.
//     */
//    val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                // Permission is granted. Continue the action or workflow in your
//                // app.
//            } else {
//                // Explain to the user that the feature is unavailable because the
//                // feature requires a permission that the user has denied. At the
//                // same time, respect the user's decision. Don't link to system
//                // settings in an effort to convince the user to change their
//                // decision.
//            }
//        }
//
//    /** Request the POST_NOTIFICATIONS permission that is required for android sdk 33+ */
//    private fun requestNotificationPermission() {
//        when {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                // You can use the API that requires the permission.
//            }
//
//            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
//                Log.w("here", "here3")
//
//                // In an educational UI, explain to the user why your app requires this
//                // permission for a specific feature to behave as expected, and what
//                // features are disabled if it's declined. In this UI, include a
//                // "cancel" or "no thanks" button that lets the user continue
//                // using your app without granting the permission.
//            }
//
//            else -> {
//                // Directly ask for the permission.
//                // The registered ActivityResultCallback gets the result of this request.
//                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//    }
}