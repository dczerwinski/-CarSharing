package com.example.carsharing.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.carsharing.R
import com.google.firebase.auth.FirebaseUser

class EmailNotVerifiedDialog(private val user: FirebaseUser) : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.alertDialog)
        val layoutInflater = activity?.layoutInflater
        val view = layoutInflater?.inflate(R.layout.dialog_email_not_verified, null)

        return builder.setView(view)
            .setTitle(R.string.dialogTitle)
            .setNegativeButton(R.string.ok, null)
            .setPositiveButton(R.string.send_verification_email_again) { _, _ ->
                user.sendEmailVerification()
            }.create()
    }
}