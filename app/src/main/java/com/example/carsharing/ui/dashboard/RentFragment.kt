package com.example.carsharing.ui.dashboard

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carsharing.R
import com.example.carsharing.ui.history.HistoryOfRentalsRecyclerViewAdapter
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RentFragment : Fragment() {

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var endButton: Button
    private lateinit var id: String
    private lateinit var startTime: LocalDateTime
    private lateinit var _car: String
    private lateinit var car: Car
    private lateinit var price: String
    private lateinit var rentUUID: String
    private lateinit var timeTV: TextView
    private lateinit var distanceTV: TextView
    private lateinit var priceTV: TextView
    private lateinit var timer: Timer
    private var costViewModel: CostCounterViewModel? = null
    private var isDataInitialized = false
    private val db = FirebaseDatabase.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private var currentLocation = LatLng(0.0, 0.0)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private var returnAreas = ArrayList<LocationViewModel.ReturnArea>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            try {
                id = it.getString(ID_KEY)!!
                _car = it.getString(CAR_KEY)!!
                rentUUID = it.getString(UUID_KEY)!!
                startTime = it.get(TIME_KEY)!! as LocalDateTime
                isDataInitialized = true
            } catch (e: Exception) {
                isDataInitialized = false
                Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show()
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(
                    R.id.nav_host_fragment,
                    MapFragment.newInstance()
                )
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        locationViewModel.init(requireActivity())
        locationViewModel.getCurrentLocation().observe(viewLifecycleOwner, {
            getCurrentLocation(it)
        })
        locationViewModel.getReturnAres().observe(viewLifecycleOwner, {
            returnAreas = ArrayList(it.values)
        })
        return inflater.inflate(R.layout.fragment_rent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val state = DashboardController.getInstance().getCurrentState()
        if (isDataInitialized && state.state != DashboardController.State.RENT) {
            car = Car.stringToCar(_car)
            DashboardController
                .getInstance()
                .changeCurrentState(
                    DashboardController.DashboardState(
                        car,
                        id,
                        rentUUID,
                        startTime,
                        DashboardController.State.RENT,
                        parentFragmentManager
                    )
                )
        } else {
            car = state.car!!
            id = state.id!!
            startTime = state.startTime!!
            rentUUID = state.rentUUID!!
        }
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                costViewModel?.tick(currentLocation)
            }
        }, 0, 1000)

        Log.d(TAG, "onViewCreated")
        endButton = requireView().findViewById(R.id.endButton)
        timeTV = requireView().findViewById(R.id.timeTV)
        distanceTV = requireView().findViewById(R.id.distanceTV)
        priceTV = requireView().findViewById(R.id.priceTV)

        costViewModel = ViewModelProvider(this).get(CostCounterViewModel::class.java)

        endButton.setOnClickListener {
            if (checkArea()) {
                val map = HashMap<String, Any>()
                map["booked"] = false
                map["latitude"] = currentLocation.latitude
                map["longitude"] = currentLocation.longitude
                db.getReference("cars")
                    .child(id)
                    .updateChildren(map)
                    .addOnSuccessListener {
                        Log.d(TAG, "SUCCESS")
                        costViewModel!!.stop()
                        DashboardController.getInstance().changeCurrentState(
                            DashboardController.DashboardState(
                                null,
                                null,
                                null,
                                null,
                                DashboardController.State.MAP
                            )
                        )
                        addToRentHistory()
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(
                            R.id.nav_host_fragment,
                            MapFragment.newInstance()
                        )
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "FAIL")
                    }
            } else {
                Toast.makeText(requireContext(), R.string.area_error, Toast.LENGTH_LONG).show()
            }
        }

        costViewModel!!.init(LatLng(car.latitude, car.longitude), startTime.format(formatter))
        costViewModel!!.getCurrentCost().observe(viewLifecycleOwner, {
            priceTV.text = getString(R.string.price, "${String.format("%.2f", it)} zł")
            price = "$it"
        })
        costViewModel!!.getCurrentDistance().observe(viewLifecycleOwner, {
            distanceTV.text = it
        })
        costViewModel!!.getCurrentTime().observe(viewLifecycleOwner, {
            timeTV.text = getString(R.string.time_text_view, it)
        })
    }

    private fun addToRentHistory() {
        val map = HashMap<String, Any>()
        map["end_position"] = HistoryOfRentalsRecyclerViewAdapter.Item.Position(
            currentLocation.latitude, currentLocation.longitude
        )
        val distance = countDistance(
            car.latitude, car.longitude, currentLocation.latitude, currentLocation.longitude
        )
        map["price_distance"] = String.format("%.2f", distance * car.price_distance)
        map["end_time"] = LocalDateTime.now().format(formatter)
        map["distance"] = "${String.format("%.2f", distance)} km"
        map["canceled"] = false
        map["price_time"] = price
        FirebaseDatabase.getInstance()
            .getReference("users_data")
            .child(uid)
            .child("rents")
            .child(rentUUID)
            .updateChildren(map)
            .addOnSuccessListener {
                Log.i(TAG, "Success during adding rent!")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error during adding rent", it)
            }
    }

    private fun countDistance(
        startLat: Double, startLng: Double, endLat: Double, endLng: Double
    ): Float {
        val startLocation = Location("").apply {
            latitude = startLat
            longitude = startLng
        }
        val endLocation = Location("").apply {
            latitude = endLat
            longitude = endLng
        }

        return endLocation.distanceTo(startLocation) / 1000
    }

    private fun getCurrentLocation(location: Location) {
        Log.d(TAG, "getCurrentLocation")
        currentLocation = LatLng(location.latitude, location.longitude)
    }

    private fun checkArea(): Boolean {
        for (area in returnAreas) {
            if (area.checkIfCarIsInArea(currentLocation.latitude, currentLocation.longitude))
                return true
        }
        return false
    }

    companion object {
        private const val ID_KEY = "param1"
        private const val CAR_KEY = "param2"
        private const val UUID_KEY = "param3"
        private const val TIME_KEY = "param4"
        private const val TAG = "RentFragment"

        @JvmStatic
        fun newInstance(id: String, car: String, rentUUID: String, startTime: LocalDateTime) =
            RentFragment().apply {
                arguments = Bundle().apply {
                    putString(ID_KEY, id)
                    putString(CAR_KEY, car)
                    putString(UUID_KEY, rentUUID)
                    putSerializable(TIME_KEY, startTime)
                }
            }
    }
}