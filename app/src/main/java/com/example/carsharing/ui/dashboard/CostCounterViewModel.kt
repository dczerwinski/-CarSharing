package com.example.carsharing.ui.dashboard

import android.icu.text.SimpleDateFormat
import android.location.Location
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import java.util.*

class CostCounterViewModel : ViewModel() {

    private var currentCost: MutableLiveData<Float> = MutableLiveData()
    private var currentDistance: MutableLiveData<String> = MutableLiveData()
    private var currentTime: MutableLiveData<String> = MutableLiveData()
    private var lastDistance: Float = 0F
    private var startLocation: Location = Location("")
    private var startTime = Calendar.getInstance()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun init(timePrice: Float, distancePrice: Float) {
        Log.d(TAG, "timePrice = $timePrice  distancePrice = $distancePrice")
        CostTimer.getInstance().start(timePrice, object : CostTimerChangeListener {
            override fun onCostChanged(cost: Float) {
                currentCost.postValue(cost)
            }
        }, distancePrice)
    }

    fun init(lastLocation: LatLng, startTime: String) {
        this.startTime.time = formatter.parse(startTime)
        this.startLocation.apply {
            longitude = lastLocation.longitude
            latitude = lastLocation.latitude
        }
        CostTimer.getInstance().changeListener(object : CostTimerChangeListener {
            override fun onCostChanged(cost: Float) {
                currentCost.postValue(cost)
            }
        })
    }

    fun stop() {
        CostTimer.getInstance().stop()
    }

    fun getCurrentCost(): LiveData<Float> {
        if (currentCost.value == null) {
            currentCost.postValue(CostTimer.getInstance().getCurrentCosts())
        }
        return currentCost
    }

    fun getCurrentDistance(): LiveData<String> {
        if (currentDistance.value == null) {
            currentDistance.postValue("${String.format("%.2f", lastDistance)} km")
        }
        return currentDistance
    }

    fun getCurrentTime(): LiveData<String> {
        if (currentTime.value == null) {
            postCurrentTime()
        }
        return currentTime
    }

    fun tick(currentLocation: LatLng) {
        postCurrentTime()
        val newLocation = Location("")
        newLocation.apply {
            latitude = currentLocation.latitude
            longitude = currentLocation.longitude
        }
        postNewDistanceCost(newLocation)
    }

    private fun postNewDistanceCost(newLocation: Location) {
        val oldDistance = lastDistance
        lastDistance = newLocation.distanceTo(startLocation) / 1000
        currentDistance.postValue("${String.format("%.2f", lastDistance)} km")
        val temp = lastDistance - oldDistance
        val costTimer = CostTimer.getInstance()
        val priceToAdd = temp * costTimer.getDistancePrice()
        Log.d(TAG, "TEmp = $temp  price2add = $priceToAdd")
        costTimer.addToCurrentCost(priceToAdd)
    }

    private fun postCurrentTime() {
        val currentTime = Calendar.getInstance()
        val dif = currentTime.timeInMillis - startTime.timeInMillis
        val time = DateUtils.formatElapsedTime(dif / DateUtils.SECOND_IN_MILLIS)
        this.currentTime.postValue(time)
    }

    private class CostTimer {

        private var currentCosts = 0F
        private var timer = Timer()
        private var listener: CostTimerChangeListener? = null
        private var distancePrice: Float = 0F

        fun start(price: Float, arg: CostTimerChangeListener, distancePrice: Float) {
            this.listener = arg
            this.distancePrice = distancePrice
            currentCosts += price
            listener?.onCostChanged(currentCosts)
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    addToCurrentCost(price)
                }
            }, 0, 60000)
        }

        fun addToCurrentCost(arg: Float) {
            synchronized(currentCosts) {
                currentCosts += arg
                listener?.onCostChanged(currentCosts)
            }
        }

        fun changeListener(arg: CostTimerChangeListener) {
            this.listener = arg
        }

        fun getCurrentCosts(): Float {
            return currentCosts
        }

        fun getDistancePrice(): Float {
            return distancePrice
        }

        fun stop() {
            timer.cancel()
            timer.purge()
            timer = Timer()
            currentCosts = 0F
            listener = null
        }

        companion object {

            @Volatile
            private var INSTANCE: CostTimer? = null

            fun getInstance(): CostTimer {
                val tempInstance = INSTANCE
                if (tempInstance != null) {
                    return tempInstance
                }
                synchronized(this) {
                    val instance = CostTimer()
                    INSTANCE = instance
                    return instance
                }
            }
        }
    }

    interface CostTimerChangeListener {
        fun onCostChanged(cost: Float)
    }

    companion object {
        private const val TAG = "CostCounterViewModel"
    }
}