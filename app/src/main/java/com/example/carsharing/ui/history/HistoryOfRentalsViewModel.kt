package com.example.carsharing.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class HistoryOfRentalsViewModel : ViewModel() {
    private val _rents = HashMap<String, HistoryOfRentalsRecyclerViewAdapter.Item>()
    private val rents = MutableLiveData<HashMap<String, HistoryOfRentalsRecyclerViewAdapter.Item>>()
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid

    fun getRents(): LiveData<HashMap<String, HistoryOfRentalsRecyclerViewAdapter.Item>> {
        if (rents.value == null) {
            FirebaseDatabase.getInstance()
                .getReference("users_data")
                .child(uid)
                .child("rents")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "onChildAdded $snapshot")
                        val rent = snapshot.getValue(
                            HistoryOfRentalsRecyclerViewAdapter.Item::class.java
                        )!!
                        val key = snapshot.key!!
                        _rents[key] = rent
                        rents.postValue(_rents)
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        Log.d(TAG, "onChildChanged $snapshot")
                        val rent = snapshot.getValue(
                            HistoryOfRentalsRecyclerViewAdapter.Item::class.java
                        )!!
                        val key = snapshot.key!!
                        _rents[key] = rent
                        rents.postValue(_rents)
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        Log.d(TAG, "onChildRemoved $snapshot")
                        val key = snapshot.key!!
                        _rents.remove(key)
                        rents.postValue(_rents)
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }
        return rents
    }

    companion object {
        private const val TAG = "HistoryOfRentalsViewModel"
    }
}