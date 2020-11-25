package com.example.carsharing.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.carsharing.R
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordDialog : AppCompatDialogFragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.alertDialog)
        val layoutInflater = activity?.layoutInflater
        val view = layoutInflater?.inflate(R.layout.dialog_reset_password, null)

        auth = FirebaseAuth.getInstance()

        val emailET = view!!.findViewById<EditText>(R.id.emailET)

        return builder.setView(view)
            .setTitle(R.string.reset_password_dialog_title)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) { _, _ ->
                val email = emailET.text.toString()
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(
                                view.context,
                                R.string.reset_email_send,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Log.e(TAG, it.toString())
                            Toast.makeText(
                                view.context,
                                R.string.something_went_wrong,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        view.context,
                        R.string.wrong_email,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.create()
    }

    companion object {
        private const val TAG = "ResetPasswordDialog"
    }
}