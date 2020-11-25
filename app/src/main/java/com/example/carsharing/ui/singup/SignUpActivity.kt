package com.example.carsharing.ui.singup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carsharing.R
import com.example.carsharing.driver_license.VerifyDriverLicenseTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailTV: TextView
    private lateinit var passwordTV: TextView
    private lateinit var reEnterPasswordTV: TextView
    private lateinit var auth: FirebaseAuth
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        emailTV = findViewById(R.id.emailTV)
        passwordTV = findViewById(R.id.passwordTV)
        reEnterPasswordTV = findViewById(R.id.reEnterPasswordTV)

        val singUpButton = findViewById<Button>(R.id.signUpButton)
        singUpButton.setOnClickListener {
            val email = emailTV.text.toString()
            val password = passwordTV.text.toString()
            val reEnterPassword = reEnterPasswordTV.text.toString()
            if (
                photoUri == null ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                password.isEmpty() ||
                reEnterPassword.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    R.string.attention_sign_up,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (password == reEnterPassword) {
                    auth
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) {
                            if (it.isSuccessful) {
                                Log.d(TAG, "createUserWithEmail:success")
                                Toast.makeText(
                                    this, R.string.sign_up_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                                auth.currentUser!!.sendEmailVerification()
                                VerifyDriverLicenseTask(
                                    this,
                                    photoUri!!,
                                    auth.currentUser!!.uid
                                ).execute()
                                auth.signOut()
                                finish()
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", it.exception)
                                if (it.exception is FirebaseAuthWeakPasswordException) {
                                    Toast.makeText(
                                        this, R.string.password_is_too_weak,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this, R.string.authentication_failed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                } else {
                    Toast.makeText(this, R.string.passwordsDiffer, Toast.LENGTH_LONG).show()
                }
            }
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val addPhotoButton = findViewById<Button>(R.id.addPhoto)
        addPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            photoUri = data!!.data!!
        }
    }

    companion object {
        private const val TAG = "SignUpActivity"
        private const val REQUEST_CODE = 2137
    }
}