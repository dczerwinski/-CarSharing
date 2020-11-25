package com.example.carsharing.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class MainActivityViewModel : ViewModel() {

    private var _fullName = String()
    private val fullName = MutableLiveData<String>()
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid

    fun getFullName(): LiveData<String> {
        if (fullName.value == null) {
            FirebaseDatabase.getInstance()
                .getReference("users_data")
                .child(uid)
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        if (snapshot.key == "fullName") {
                            _fullName = snapshot.getValue(String::class.java) as String
                            fullName.postValue(_fullName)
                        }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        if (snapshot.key == "fullName") {
                            _fullName = snapshot.getValue(String::class.java) as String
                            fullName.postValue(_fullName)
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {}

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
        return fullName
    }
}