package com.example.carsharing.map

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MapWrapperLayout : RelativeLayout {

    private var map: GoogleMap? = null
    private var marker: Marker? = null
    private var infoWindow: View? = null
    private var bottomOffsetPixels: Int = 0

    constructor(context: Context?)
            : super(context)

    constructor(context: Context?, attrs: AttributeSet)
            : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle)

    fun init(map: GoogleMap, bottomOffsetPixels: Int) {
        this.map = map
        this.bottomOffsetPixels = bottomOffsetPixels
    }

    fun setMarkerWithInfoWindow(marker: Marker, infoWindow: View) {
        this.marker = marker
        this.infoWindow = infoWindow
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        var ret = false
        if (marker != null && marker!!.isInfoWindowShown && map != null && infoWindow != null) {
            val point = map!!.projection.toScreenLocation(marker!!.position)
            val copyEvent = MotionEvent.obtain(ev)
            copyEvent.offsetLocation(
                (-point.x + (infoWindow!!.width / 2)).toFloat(),
                (-point.y + infoWindow!!.height + bottomOffsetPixels).toFloat()
            )
            ret = infoWindow!!.dispatchTouchEvent(copyEvent)
        }
        return ret || super.dispatchTouchEvent(ev)
    }
}