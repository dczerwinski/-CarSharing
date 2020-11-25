package com.example.carsharing.ui.history

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carsharing.R
import com.example.carsharing.ui.dashboard.Car
import com.example.carsharing.utils.ResourcesUtils

class HistoryOfRentalsRecyclerViewAdapter(
    private val parentFragmentManager: FragmentManager,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<HistoryOfRentalsRecyclerViewAdapter.HistoryOfRentalsViewHolder>() {

    companion object {
        private const val TAG = "HistoryOfRentalsRVA"
    }

    private var itemsList = ArrayList<Item>()
    private var keysList = ArrayList<String>()

    fun setList(map: HashMap<String, Item>) {
        Log.d(TAG, "setList")
        val list = ArrayList(map.values)
        val keys = ArrayList(map.keys)
        keysList = keys
        itemsList = ArrayList(list)
        notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryOfRentalsViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val inflater: LayoutInflater? = LayoutInflater.from(parent.context)
        val view = inflater!!.inflate(R.layout.item_rent_history, parent, false)
        return HistoryOfRentalsViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryOfRentalsViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder")
        holder.setUp(itemsList[position])
        holder.itemView.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.replace(
                R.id.nav_host_fragment,
                SingleRentFragment.newInstance(keysList[position])
            )
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount() = itemsList.size

    class HistoryOfRentalsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val carIconIV =
            itemView.findViewById<ImageView>(R.id.carIconIV)
        private val registrationNumberTV =
            itemView.findViewById<TextView>(R.id.registrationNumberTV)
        private val dateTV = itemView.findViewById<TextView>(R.id.dateTV)
        private val distanceTV = itemView.findViewById<TextView>(R.id.distanceTV)
        private val priceTV = itemView.findViewById<TextView>(R.id.priceTV)
        private val successIV = itemView.findViewById<ImageView>(R.id.successIV)
        private val resultTV = itemView.findViewById<TextView>(R.id.resultTV)

        @SuppressLint("SetTextI18n")
        fun setUp(item: Item) {
            carIconIV.setImageResource(
                ResourcesUtils.getImageDrawableFromName(item.car!!.car_model)
            )
            registrationNumberTV.text = item.car.registration_number
            dateTV.text = item.start_time.take(16)
            distanceTV.text = item.distance
            val res = ResourcesUtils.getResourcesFromRenStatus(item.payed, item.canceled)
            priceTV.text =
                if (item.payed) "${
                    String.format(
                        "%.2f", item.price_distance.replace(",", ".").toFloat() +
                                item.price_time.replace(",", ".").toFloat()
                    )
                } zł - zapłacona"
                else "${
                    String.format(
                        "%.2f", item.price_distance.replace(",", ".").toFloat() +
                                item.price_time.replace(",", ".").toFloat()
                    )
                } zł - nie zapłacona"
            successIV.setImageResource(res.first)
            resultTV.text = itemView.context.getString(res.second)
        }

    }

    data class Item(
        val start_position: Position? = null,
        val end_position: Position? = null,
        val price_time: String = "",
        val price_distance: String = "",
        val start_time: String = "",
        val end_time: String = "",
        val distance: String = "",
        val payed: Boolean = false,
        val canceled: Boolean = false,
        val car: Car? = null
    ) {
        data class Position(
            val lat: Double = 0.toDouble(),
            val lng: Double = 0.toDouble()
        )
    }
}