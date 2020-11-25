package com.example.carsharing.ui.dashboard

import android.util.Log

data class Car(
    val car_model: String = "",
    val fuel_level: Int = 0,
    val price_time: Float = 0F,
    val price_distance: Float = 0F,
    val range: Int = 0,
    val latitude: Double = 0.toDouble(),
    val longitude: Double = 0.toDouble(),
    val booked: Boolean = false,
    val registration_number: String = ""
) {
    override fun toString(): String {
        return "$car_model:$fuel_level:$price_time:$price_distance:$range:$latitude:$longitude:$registration_number"
    }

    companion object {
        private const val TAG = "Car"

        fun stringToCar(arg: String): Car {
            Log.d(TAG, "car = $arg")
            val temp = arg
                .substring(arg.indexOf("=") + 1)
                .split(":")
                .toTypedArray()
            return Car(
                temp[0],
                temp[1].toInt(),
                temp[2].toFloat(),
                temp[3].toFloat(),
                temp[4].toInt(),
                temp[5].toDouble(),
                temp[6].toDouble(),
                registration_number = temp[7]
            )
        }
    }
}