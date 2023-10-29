package com.android.myalarm

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment


class NotificationsContextDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setView(R.layout.fragment_notifications_context_dialog)
            .create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // UI configuration to have shaped custom dialog fragment
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return super.onCreateView(inflater, container, savedInstanceState)
    }

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
//        dialog?.window?.attributes?.height = 300

    }


    companion object {
        const val TAG = "NotificationsRequestDialog"
    }

}