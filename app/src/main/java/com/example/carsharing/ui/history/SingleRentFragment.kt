package com.example.carsharing.ui.history

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carsharing.R
import com.example.carsharing.ui.MainActivity
import com.example.carsharing.utils.ResourcesUtils
import java.util.*

class SingleRentFragment : Fragment() {

    private lateinit var viewModel: HistoryOfRentalsViewModel
    private lateinit var timeFromTV: TextView
    private lateinit var timeToTV: TextView
    private lateinit var carNameTV: TextView
    private lateinit var carNumberTV: TextView
    private lateinit var layoutRouteButton: LinearLayoutCompat
    private lateinit var carImageView: ImageView
    private lateinit var timeTV: TextView
    private lateinit var distanceTV: TextView
    private lateinit var priceTimeTV: TextView
    private lateinit var priceDistanceTV: TextView
    private lateinit var priceSumTV: TextView
    private var itemKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemKey = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProvider(this).get(HistoryOfRentalsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_single_rent, container, false)

        (requireActivity() as MainActivity).updateBackButtonAction(
            HistoryOfRentalsFragment(),
            parentFragmentManager
        )

        timeFromTV = root.findViewById(R.id.timeFromTV)
        timeToTV = root.findViewById(R.id.timeToTV)
        carNameTV = root.findViewById(R.id.carNameTV)
        carNumberTV = root.findViewById(R.id.carNumberTV)
        layoutRouteButton = root.findViewById(R.id.layoutRouteButton)
        carImageView = root.findViewById(R.id.carImageView)
        timeTV = root.findViewById(R.id.timeTV)
        distanceTV = root.findViewById(R.id.distanceTV)
        priceTimeTV = root.findViewById(R.id.priceTimeTV)
        priceDistanceTV = root.findViewById(R.id.priceDistanceTV)
        priceSumTV = root.findViewById(R.id.priceSumTV)

        viewModel.getRents().observe(viewLifecycleOwner, {
            setUpSingleRent(it)
        })

        return root
    }

    @SuppressLint("SetTextI18n")
    private fun setUpSingleRent(map: HashMap<String, HistoryOfRentalsRecyclerViewAdapter.Item>) {
        val item = map[itemKey]!!
        val startTime = Calendar.getInstance()
        val endTime = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        startTime.time = formatter.parse(item.start_time)
        endTime.time = formatter.parse(item.end_time)
        val dif = endTime.timeInMillis - startTime.timeInMillis
        val time = DateUtils.formatElapsedTime(dif / DateUtils.SECOND_IN_MILLIS)
        timeFromTV.text = "${item.start_time.take(16)} -"
        timeToTV.text = item.end_time.take(16)
        carNameTV.text = item.car!!.car_model
        carNumberTV.text = item.car.registration_number
        carImageView.setImageResource(ResourcesUtils.getImageDrawableFromName(item.car.car_model))
        timeTV.text = getString(R.string.time, time)
        distanceTV.text = getString(R.string.distance, item.distance)
        if (item.start_position == null || item.end_position == null) {
            layoutRouteButton.visibility = View.INVISIBLE
        } else {
            layoutRouteButton.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "http://maps.google.com/maps?" +
                                "saddr=${item.start_position.lat},${item.start_position.lng}" +
                                "&daddr=${item.end_position.lat},${item.end_position.lng}"
                    )
                )
                startActivity(intent)
            }
        }
        priceTimeTV.text = getString(
            R.string.price_time,
            "${String.format("%.2f", item.price_time.replace(",", ".").toFloat())} zł"
        )
        priceDistanceTV.text = getString(
            R.string.price_distance,
            "${String.format("%.2f", item.price_distance.replace(",", ".").toFloat())} zł"
        )
        priceSumTV.text = getString(
            R.string.price_sum,
            "${
                String.format(
                    "%.2f",
                    item.price_distance.replace(",", ".").toFloat() +
                            item.price_time.replace(",", ".").toFloat()
                )
            } zł"
        )
    }

    companion object {
        private const val ARG_PARAM1 = "param1"

        @JvmStatic
        fun newInstance(itemKey: String) =
            SingleRentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, itemKey)
                }
            }
    }
}