package com.example.carsharing.ui.dashboard

import android.util.Log
import androidx.fragment.app.FragmentManager
import java.time.LocalDateTime

class DashboardController {

    private var currentState = DashboardState(
        null, null, null, null, State.MAP
    )

    companion object {
        private const val TAG = "DashboardController"

        @Volatile
        private var INSTANCE: DashboardController? = null

        fun getInstance(): DashboardController {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = DashboardController()
                INSTANCE = instance
                return instance
            }
        }
    }

    fun getCurrentState(): DashboardState {
        Log.i(TAG, "current state: $currentState")
        synchronized(currentState) {
            return currentState
        }
    }

    fun changeCurrentState(newState: DashboardState) {
        synchronized(currentState) {
            this.currentState = newState
        }
        Log.d(TAG, "Dashboard state changed to $newState")
    }

    data class DashboardState(
        var car: Car?,
        var id: String?,
        var rentUUID: String?,
        var startTime: LocalDateTime?,
        var state: State,
        var fragmentManager: FragmentManager? = null
    )

    enum class State {
        MAP,
        RESERVATION,
        RENT
    }
}