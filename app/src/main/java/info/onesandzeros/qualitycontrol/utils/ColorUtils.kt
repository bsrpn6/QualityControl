package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils

import android.graphics.Color
import android.graphics.Color.parseColor

class ColorUtils {
    companion object {
        fun determineWeightLimitColor(
            weight: Double,
            mavValue: Double,
            lslValue: Double,
            uslValue: Double
        ): Int {
            return when {
                weight < mavValue -> Color.RED
                weight < lslValue || weight > uslValue -> parseColor("#ffffbb33")
                else -> Color.BLACK  // default color (or any other color you want as default)
            }
        }
    }
}