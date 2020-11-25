package com.example.carsharing.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carsharing.R
import com.example.carsharing.ui.MainActivity
import com.example.carsharing.ui.dashboard.DashboardController
import com.example.carsharing.ui.dialogs.EmailNotVerifiedDialog
import com.example.carsharing.ui.dialogs.ResetPasswordDialog
import com.example.carsharing.ui.singup.SignUpActivity
import com.example.carsharing.utils.AndroidUtils
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailTV: TextView
    private lateinit var passwordTV: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        DashboardController.getInstance()

        auth = FirebaseAuth.getInstance()
        emailTV = findViewById(R.id.emailTV)
        passwordTV = findViewById(R.id.passwordTV)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = emailTV.text.toString()
            val password = passwordTV.text.toString()
            if (email == "")
                Toast.makeText(this, "Email is empty!", Toast.LENGTH_SHORT).show()
            if (password == "")
                Toast.makeText(this, "password is empty!", Toast.LENGTH_SHORT).show()

            auth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        verifyUser()
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", it.exception)
                        Toast.makeText(
                            this, R.string.authentication_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        val singUpButton = findViewById<Button>(R.id.signUpButton)
        singUpButton.setOnClickListener {
            AndroidUtils.hideKeyboard(this)
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        val resetPasswordButton = findViewById<Button>(R.id.resetPasswordButton)
        resetPasswordButton.setOnClickListener {
            ResetPasswordDialog().show(supportFragmentManager, "ResetPasswordDialog")
        }
    }

    override fun onStart() {
        super.onStart()
        verifyUser()
    }

    private fun verifyUser() {
        val currentUser = auth.currentUser
        Log.d(TAG, "current user = $currentUser")
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                EmailNotVerifiedDialog(currentUser)
                    .show(supportFragmentManager, "EmailNotVerifiedDialog")
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}