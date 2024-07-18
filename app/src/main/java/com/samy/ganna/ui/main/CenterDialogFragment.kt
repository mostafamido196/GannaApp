package com.samy.ganna.ui.main

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.samy.ganna.R
import com.samy.ganna.ui.main.MainActivity.Companion.REQUEST_CODE_SETTINGS


class CenterDialogFragment : DialogFragment() {



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.custom_dialog, null)
        val titleTextView: TextView = dialogView.findViewById(R.id.dialog_title)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)

        // Set text in case you need to change it dynamically
        titleTextView.text = "الإشعارات معطلة"
        messageTextView.text = "الإشعارات معطلة لهذا التطبيق. يرجى تمكين الإشعارات من الإعدادات."

        return MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setView(dialogView)
            .setPositiveButton("اذهب إلى الإعدادات") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivityForResult(intent,REQUEST_CODE_SETTINGS)
            }
            .setNegativeButton("إلغاء", null)
            .create()
    }
}
