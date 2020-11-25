package com.example.carsharing.ui.dashboard

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import kotlin.collections.HashMap

class ReservationFragment : Fragment() {

    private lateinit var endButton: Button
    private lateinit var rentButton: Button
    private lateinit var id: String
    private lateinit var _car: String
    private lateinit var db: FirebaseDatabase
    private lateinit var uid: String
    private lateinit var rentUUID: String
    private lateinit var car: Car
    private lateinit var costViewModel: CostCounterViewModel
    private lateinit var price: String
    private lateinit var startTime: String
    private lateinit var _startTime: LocalDateTime
    private lateinit var startPosition: LatLng
    private var isDataInitialized = false
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            try {
                id = it.getString(ID_KEY)!!
                _car = it.getString(CAR_KEY)!!
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
        return inflater.inflate(R.layout.fragment_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseDatabase.getInstance()
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        val state = DashboardController.getInstance().getCurrentState()
        if (isDataInitialized && state.state != DashboardController.State.RESERVATION) {
            Log.d(TAG, "enter reservation fragment first time")
            _startTime = LocalDateTime.now()
            rentUUID = UUID.randomUUID().toString()
            car = Car.stringToCar(_car)
            DashboardController
                .getInstance()
                .changeCurrentState(
                    DashboardController.DashboardState(
                        car,
                        id,
                        rentUUID,
                        _startTime,
                        DashboardController.State.RESERVATION,
                        parentFragmentManager
                    )
                )
        } else {
            Log.d(TAG, "enter reservation fragment")
            car = state.car!!
            id = state.id!!
            _startTime = state.startTime!!
            rentUUID = state.rentUUID!!
        }
        startTime = _startTime.format(formatter)
        startPosition = LatLng(car.latitude, car.longitude)
        endButton = requireView().findViewById(R.id.endButton)
        rentButton = requireView().findViewById(R.id.rentButton)

        costViewModel = ViewModelProvider(this).get(CostCounterViewModel::class.java)
        costViewModel.init(car.price_time, car.price_distance)
        costViewModel.getCurrentCost().observe(requireActivity(), {
            price = String.format("%.2f", it).replace(",", ".")
        })

        var map = HashMap<String, Any>()
        map["booked"] = true
        FirebaseDatabase.getInstance()
            .getReference("cars")
            .child(id)
            .updateChildren(map)
            .addOnSuccessListener {
                Log.d(TAG, "SUCCESS")
                endButton.visibility = View.VISIBLE
                rentButton.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Log.e(TAG, "FAIL")
            }

        endButton.setOnClickListener {
            map = HashMap()
            map["booked"] = false
            db.getReference("cars")
                .child(id)
                .updateChildren(map)
                .addOnSuccessListener {
                    Log.d(TAG, "SUCCESS")
                }
                .addOnFailureListener {
                    Log.e(TAG, "FAIL")
                }
            costViewModel.stop()
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

        rentButton.setOnClickListener {
            val intent = Intent("com.google.zxing.client.android.SCAN")
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
            startActivityForResult(intent, QR_CODE_SCAN_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == QR_CODE_SCAN_REQUEST_CODE) {
            val contents = data?.getStringExtra("SCAN_RESULT")
            if (contents != null && contents == id) {
                DashboardController.getInstance().changeCurrentState(
                    DashboardController.DashboardState(
                        car,
                        id,
                        rentUUID,
                        _startTime,
                        DashboardController.State.RENT,
                        parentFragmentManager
                    )
                )
                addToRentHistory()
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(
                    R.id.nav_host_fragment,
                    RentFragment.newInstance(id, _car, rentUUID, _startTime)
                )
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                Toast.makeText(requireContext(), R.string.wrong_qr_code, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error wrong qr code!")
            }
        }
    }

    private fun addToRentHistory() {
        val rent = HistoryOfRentalsRecyclerViewAdapter.Item(
            HistoryOfRentalsRecyclerViewAdapter.Item.Position(
                startPosition.latitude, startPosition.longitude
            ),
            null,
            price,
            "0",
            startTime,
            LocalDateTime.now().format(formatter),
            "0 km",
            true,
            canceled = true,
            car = car
        )
        Log.d(TAG, "rent = $rent")
        db.getReference("users_data")
            .child(uid)
            .child("rents")
            .child(rentUUID)
            .setValue(rent)
            .addOnSuccessListener {
                Log.i(TAG, "Success during adding rent!")
            }
            .addOnFailureListener {
                Log.e(TAG, "Error during adding rent", it)
            }
    }

    companion object {
        private const val ID_KEY = "param1"
        private const val CAR_KEY = "param2"
        private const val TAG = "ReservationFragment"
        private const val QR_CODE_SCAN_REQUEST_CODE = 4663

        @JvmStatic
        fun newInstance(id: String, car: String) =
            ReservationFragment().apply {
                arguments = Bundle().apply {
                    putString(ID_KEY, id)
                    putString(CAR_KEY, car)
                }
            }
    }
}