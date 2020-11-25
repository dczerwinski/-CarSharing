package com.example.carsharing.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class MapViewModel : ViewModel() {
    private var _cars = HashMap<String, Car>()
    private var cars: MutableLiveData<HashMap<String, Car>> = MutableLiveData()

    fun getCars(): LiveData<HashMap<String, Car>> {
        if (cars.value == null) {
            FirebaseDatabase.getInstance()
                .getReference("cars")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        Log.d(TAG, "onChildAdded $snapshot")
                        val car = snapshot.getValue(Car::class.java)!!
                        val key = snapshot.key!!
                        _cars[key] = car
                        cars.postValue(_cars)
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        Log.d(TAG, "onChildChanged $snapshot")
                        val car = snapshot.getValue(Car::class.java)!!
                        val key = snapshot.key!!
                        _cars[key] = car
                        cars.postValue(_cars)
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        Log.d(TAG, "onChildRemoved $snapshot")
                        val key = snapshot.key!!
                        _cars.remove(key)
                        cars.postValue(_cars)
                    }

                    override fun onChildMoved(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "$error")
                    }
                })
        }
        return cars
    }

    companion object {
        private const val TAG = "DashboardViewModel"
    }
}