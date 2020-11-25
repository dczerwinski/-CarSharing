package com.example.carsharing.map

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import com.google.android.gms.maps.model.Marker

abstract class OnInfoWindowElemTouchListener(
    private val view: View
) : View.OnTouchListener {

    private var marker: Marker? = null
    private val handler = Handler()
    private var pressed = false

    fun setMarker(marker: Marker) {
        this.marker = marker
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (0 <= event!!.x && event.x <= view.width &&
            0 <= event.y && event.y <= view.height
        ) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> startPress()
                MotionEvent.ACTION_UP -> handler.postDelayed(confirmClickRunnable, 150)
                MotionEvent.ACTION_CANCEL -> endPress()
            }
        } else {
            endPress()
        }
        return false
    }

    private fun startPress() {
        if (!pressed) {
            pressed = true
            handler.removeCallbacks(confirmClickRunnable)
            marker?.showInfoWindow()
        }
    }

    private fun endPress(): Boolean {
        return if (pressed) {
            this.pressed = false
            handler.removeCallbacks(confirmClickRunnable)
            marker?.showInfoWindow()
            true
        } else {
            false
        }
    }

    private val confirmClickRunnable = Runnable {
        if (endPress()) {
            onClickConfirmed(view, marker!!)
        }
    }

    protected abstract fun onClickConfirmed(view: View, marker: Marker)
}