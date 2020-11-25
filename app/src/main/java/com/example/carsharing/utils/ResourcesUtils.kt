package com.example.carsharing.utils

import android.util.Log
import com.example.carsharing.R
import java.util.*

class ResourcesUtils {

    companion object {

        private const val TAG = "ResourcesUtils"

        private fun getTypeFromName(name: String): CarType {
            Log.d(TAG, "name = $name")
            for (type in CarType.values()) {
                if (name
                        .toLowerCase(Locale.getDefault())
                        .contains(
                            type
                                .name
                                .toLowerCase(Locale.getDefault())
                                .replace("_", " ")
                        )
                ) {
                    return type
                }
            }
            return CarType.UNKNOWN
        }

        fun getIconDrawableFromName(name: String): Int {
            return getTypeFromName(name).iconResId
        }

        fun getImageDrawableFromName(name: String): Int {
            return getTypeFromName(name).imageResId
        }

        fun getResourcesFromRenStatus(payed: Boolean, canceled: Boolean): Pair<Int, Int> {
            return if (payed && !canceled) {
                Pair(R.drawable.icon_ok, R.string.finished)
            } else if (payed && canceled) {
                Pair(R.drawable.icon_cancel, R.string.canceled)
            } else {
                Pair(R.drawable.icon_attention, R.string.paying)
            }
        }
    }

    private enum class CarType(
        val iconResId: Int = -1,
        val imageResId: Int = -1
    ) {

        // BMW
        BMW_M6(R.drawable.logo_bmw, R.drawable.bmw_m6),

        // AUDI
        AUDI_RS6(R.drawable.logo_audi, R.drawable.audi_rs6),
        AUDI_RS3(R.drawable.logo_audi, R.drawable.audi_rs3),

        // UNKNOWN
        UNKNOWN
    }
}