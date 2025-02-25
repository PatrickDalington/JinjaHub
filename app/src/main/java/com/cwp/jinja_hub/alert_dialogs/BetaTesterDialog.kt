package com.cwp.jinja_hub.alert_dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class BetaTesterDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Beta Testing (Phase 1) 👋🏻")
            .setMessage("Thank you for taking your time to test Jinja-Hub v1.\nYour feedback will go a long way to improving the app.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}