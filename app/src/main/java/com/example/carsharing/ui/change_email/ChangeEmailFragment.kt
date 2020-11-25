package com.example.carsharing.ui.change_email

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carsharing.R
import com.example.carsharing.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ChangeEmailFragment : Fragment() {

    private lateinit var changeEmailViewModel: ChangeEmailViewModel
    private lateinit var currentEmailString: String
    private lateinit var saveButton: Button
    private lateinit var newEmailET: EditText
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        changeEmailViewModel =
            ViewModelProvider(this).get(ChangeEmailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_change_email, container, false)
        val currentEmailTV: TextView = root.findViewById(R.id.currentEmailTV)
        requireActivity().setTitle(R.string.change_email)
        changeEmailViewModel.text.observe(viewLifecycleOwner, {
            currentEmailTV.text = it
        })

        newEmailET = root.findViewById(R.id.newEmailET)

        saveButton = root.findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            val newEmail = newEmailET.text.toString()
            if (Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                auth.currentUser!!.updateEmail(newEmail)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this.requireContext(),
                            R.string.email_has_been_changed,
                            Toast.LENGTH_LONG
                        ).show()
                        auth.currentUser!!.sendEmailVerification()
                        auth.signOut()
                        val intent = Intent(this.requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, it.toString())
                        Toast.makeText(
                            this.requireContext(),
                            R.string.wrong_email,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    this.requireContext(),
                    R.string.wrong_email,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return root
    }

    companion object {
        private const val TAG = "ChangeEmailFragment"
    }
}