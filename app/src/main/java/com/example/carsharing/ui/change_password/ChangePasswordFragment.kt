package com.example.carsharing.ui.change_password

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carsharing.R
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private lateinit var changePasswordViewModel: ChangePasswordViewModel
    private lateinit var oldPasswordET: EditText
    private lateinit var newPasswordET: EditText
    private lateinit var newPasswordReTypeET: EditText
    private lateinit var saveButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        changePasswordViewModel =
            ViewModelProvider(this).get(ChangePasswordViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_change_password, container, false)
        requireActivity().setTitle(R.string.change_password)
        auth = FirebaseAuth.getInstance()

        oldPasswordET = root.findViewById(R.id.oldPasswordET)
        newPasswordET = root.findViewById(R.id.newPasswordET)
        newPasswordReTypeET = root.findViewById(R.id.newPasswordReTypeET)
        saveButton = root.findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val oldPassword = oldPasswordET.text.toString()
            val newPassword = newPasswordET.text.toString()
            val newPasswordReType = newPasswordReTypeET.text.toString()
            if (
                oldPassword.isNotEmpty() &&
                newPassword.isNotEmpty() &&
                newPasswordReType.isNotEmpty()
            ) {

                val user = auth.currentUser!!
                if (newPassword == newPasswordReType) {
                    auth.signInWithEmailAndPassword(user.email.toString(), oldPassword)
                        .addOnSuccessListener {
                            user.updatePassword(newPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.password_has_been_changed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Log.e(TAG, it.toString())
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.password_is_too_weak,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Log.e(TAG, it.toString())
                            Toast.makeText(
                                requireContext(),
                                R.string.wrong_password,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.passwordsDiffer,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.empty_fields,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return root
    }

    companion object {
        private const val TAG = "ChangePasswordFragment"
    }
}