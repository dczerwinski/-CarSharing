package com.example.carsharing.ui.change_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChangePasswordViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is changePassword Fragment"
    }
    val text: LiveData<String> = _text
}