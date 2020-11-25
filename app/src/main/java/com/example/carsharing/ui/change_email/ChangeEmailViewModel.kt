package com.example.carsharing.ui.change_email

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ChangeEmailViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _text = MutableLiveData<String>().apply {
        value = auth.currentUser!!.email
    }
    val text: LiveData<String> = _text
}