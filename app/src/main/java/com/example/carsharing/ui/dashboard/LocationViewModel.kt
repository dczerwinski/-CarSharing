package com.example.carsharing.ui.dashboard

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class
LocationViewModel : ViewModel() {

    private var _location = Location("").apply {
        latitude = 0.toDouble()
        longitude = 0.toDouble()
    }
    private var location = MutableLiveData<Location>()
    private lateinit var client: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener? = null
    private lateinit var activity: FragmentActivity
    private var _returnAreas = HashMap<String, ReturnArea>()
    private var returnAreas = MutableLiveData<HashMap<String, ReturnArea>>()

    fun init(activity: FragmentActivity) {
        this.activity = activity
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): LiveData<Location> {
        Log.d(TAG, "getCurrentLocation")
        if (location.value == null) {
            client = LocationServices.getFusedLocationProviderClient(activity)
            val task = client.lastLocation
            task.addOnSuccessListener { arg ->
                _location = arg
                location.postValue(_location)
            }
            locationListener = object : LocationListener {
                override fun onLocationChanged(arg: Location?) {
                    if (arg != null) {
                        _location = arg
                        location.postValue(_location)
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                override fun onProviderEnabled(provider: String?) {}

                override fun onProviderDisabled(provider: String?) {}
            }
            locationManager = activity
                .getSystemService(androidx.appcompat.app.AppCompatActivity.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE,
                locationListener!!
            )
        }
        return location
    }

    fun getReturnAres(): LiveData<HashMap<String, ReturnArea>> {
        if (returnAreas.value == null) {
            FirebaseDatabase.getInstance()
                .getReference("return_areas")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val area = snapshot.getValue(ReturnArea::class.java)!!
                        val key = snapshot.key!!
                        _returnAreas[key] = area
                        returnAreas.postValue(_returnAreas)
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        val area = snapshot.getValue(ReturnArea::class.java)!!
                        val key = snapshot.key!!
                        _returnAreas[key] = area
                        returnAreas.postValue(_returnAreas)
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val key = snapshot.key!!
                        _returnAreas.remove(key)
                        returnAreas.postValue(_returnAreas)
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
        return returnAreas
    }

    data class ReturnArea(
        val maxLat: Double = 0.0,
        val minLat: Double = 0.0,
        val maxLng: Double = 0.0,
        val minLng: Double = 0.0
    ) {
        fun checkIfCarIsInArea(lat: Double, lng: Double): Boolean {
            return (lat in minLat..maxLat && lng in minLng..maxLng)
        }
    }

    companion object {
        private const val TAG = "LocationViewModel"
        private const val LOCATION_REFRESH_TIME = 2000L
        private const val LOCATION_REFRESH_DISTANCE = 1F
    }
}