package com.example.carsharing.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carsharing.R
import com.example.carsharing.map.MapWrapperLayout
import com.example.carsharing.map.OnInfoWindowElemTouchListener
import com.example.carsharing.ui.payment_methods.PaymentMethodsViewModel
import com.example.carsharing.utils.ResourcesUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapFragment : Fragment() {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var paymentMethodsViewModel: PaymentMethodsViewModel
    private lateinit var map: GoogleMap
    private lateinit var infoButtonListener: OnInfoWindowElemTouchListener
    private lateinit var mapWrapperLayout: MapWrapperLayout
    private lateinit var infoWindow: ViewGroup
    private lateinit var carModelTV: TextView
    private lateinit var fuelLevelTV: TextView
    private lateinit var rangeTV: TextView
    private lateinit var priceTV: TextView
    private lateinit var iconIV: ImageView
    private lateinit var mapFragment: SupportMapFragment
    private var paymentMethodsCount = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val currentState = DashboardController.getInstance().getCurrentState()
        when (currentState.state) {
            DashboardController.State.RESERVATION -> {
                val transaction = currentState.fragmentManager!!.beginTransaction()
                transaction.replace(
                    R.id.nav_host_fragment,
                    ReservationFragment.newInstance(currentState.id!!, currentState.car.toString())
                )
                transaction.addToBackStack(null)
                transaction.commit()
            }
            DashboardController.State.RENT -> {
                val transaction = currentState.fragmentManager!!.beginTransaction()
                transaction.replace(
                    R.id.nav_host_fragment,
                    RentFragment.newInstance(
                        currentState.id!!,
                        currentState.car.toString(),
                        currentState.rentUUID!!,
                        currentState.startTime!!
                    )
                )
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        paymentMethodsViewModel = ViewModelProvider(this).get(PaymentMethodsViewModel::class.java)
        paymentMethodsViewModel.getCards().observe(viewLifecycleOwner, {
            paymentMethodsCount = it.size
        })
        locationViewModel.init(requireActivity())
        val state = DashboardController.getInstance().getCurrentState()
        if (state.state == DashboardController.State.MAP) {
            DashboardController.getInstance().changeCurrentState(
                DashboardController.DashboardState(
                    null,
                    null,
                    null,
                    null,
                    DashboardController.State.MAP,
                    parentFragmentManager
                )
            )
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationViewModel.getCurrentLocation().observe(viewLifecycleOwner, {
                getCurrentLocation(it)
            })
            Log.d(TAG, "onCreateView - getCurrentLocation")
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_LOCATION
            )
        }

        infoWindow = layoutInflater.inflate(R.layout.layout_info_window, null) as ViewGroup
        carModelTV = infoWindow.findViewById(R.id.carModelTV)
        fuelLevelTV = infoWindow.findViewById(R.id.fuelLevelTV)
        rangeTV = infoWindow.findViewById(R.id.rangeTV)
        priceTV = infoWindow.findViewById(R.id.priceTV)
        iconIV = infoWindow.findViewById(R.id.carImageView)

        val infoButton = infoWindow.findViewById<Button>(R.id.bootItButton)
        infoButtonListener = object : OnInfoWindowElemTouchListener(infoButton) {
            override fun onClickConfirmed(view: View, marker: Marker) {
                if (paymentMethodsCount > 0) {
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(
                        R.id.nav_host_fragment,
                        ReservationFragment.newInstance(marker.title, marker.snippet)
                    )
                    transaction.addToBackStack(null)
                    transaction.commit()
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.no_payment_methods,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        infoButton.setOnTouchListener(infoButtonListener)
        Log.d(TAG, "onCreateView - end")
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapWrapperLayout = view.findViewById(R.id.map_relative_layout)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        Log.d(TAG, "onViewCreated - end")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationViewModel.getCurrentLocation().observe(viewLifecycleOwner, {
                    getCurrentLocation(it)
                })
            }
        }
    }

    private fun addCars(
        carsList: HashMap<String, Car>
    ) {
        map.clear()
        carsList.filter {
            !it.value.booked
        }.forEach { car ->
            Log.d(TAG, "Adding car id = ${car.key} car = $car")
            val carImage = ResourcesUtils.getIconDrawableFromName(car.value.car_model)
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(car.value.latitude, car.value.longitude))
                    .title(car.key)
                    .snippet(car.toString())
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            createCustomMarker(carImage, car.value.car_model)
                        )
                    )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(location: Location) {
        Log.d(TAG, "getCurrentLocation - location = $location")
        mapFragment.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(
                googleMap: GoogleMap
            ) {
                Log.d(TAG, "OnMapReadyCallback - start")
                map = googleMap
                map.isMyLocationEnabled = true
                mapWrapperLayout.init(map, getPixelsFromDp(requireContext()))

                map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                    override fun getInfoWindow(marker: Marker): View? {
                        val car = Car.stringToCar(marker.snippet)
                        carModelTV.text = getString(R.string.model_name, car.car_model)
                        fuelLevelTV.text = getString(R.string.fuel_level, car.fuel_level)
                        rangeTV.text =
                            getString(R.string.range_of_car, car.range.toString())
                        Log.d(TAG, "carprice: ${car.price_time}  ${car.price_distance}")
                        priceTV.text =
                            getString(
                                R.string.price_of_car,
                                String.format("%.2f", car.price_time),
                                String.format("%.2f", car.price_distance)
                            )
                        mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow)
                        iconIV.setImageResource(
                            ResourcesUtils.getImageDrawableFromName(car.car_model)
                        )
                        infoButtonListener.setMarker(marker)
                        return infoWindow
                    }

                    override fun getInfoContents(marker: Marker): View {
                        mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow)
                        infoButtonListener.setMarker(marker)
                        return infoWindow
                    }
                })

                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), 18F
                    )
                )
                try {
                    mapViewModel.getCars().observe(viewLifecycleOwner, {
                        addCars(it)
                    })
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "Error ", e)
                }
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun createCustomMarker(@DrawableRes resource: Int, name: String): Bitmap {
        val marker: View = (requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater).inflate(R.layout.layout_marker, null)
        val markerImage: ImageView = marker.findViewById(R.id.carImageView) as ImageView
        markerImage.setImageResource(resource)
        val nameTV = marker.findViewById<TextView>(R.id.carModelTV)
        nameTV.text = name

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        marker.layoutParams = ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT)
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        marker.buildDrawingCache()
        val bitmap =
            Bitmap.createBitmap(
                marker.measuredWidth,
                marker.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        marker.draw(canvas)
        return bitmap
    }

    private fun getPixelsFromDp(context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (59F * scale + 0.5F).toInt()
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val REQUEST_PERMISSION_LOCATION = 69

        @JvmStatic
        fun newInstance() =
            MapFragment().apply {
                arguments = Bundle()
            }
    }
}