package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable

data class WeightCheckItem(
    val tarWt: Double,
    val lsl: Double,
    val usl: Double,
    val mav: Double,
    val fillHeads: List<Int>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.createIntArray()?.toList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(tarWt)
        parcel.writeDouble(lsl)
        parcel.writeDouble(usl)
        parcel.writeDouble(mav)
        parcel.writeIntArray(fillHeads.toIntArray())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WeightCheckItem> {
        override fun createFromParcel(parcel: Parcel): WeightCheckItem {
            return WeightCheckItem(parcel)
        }

        override fun newArray(size: Int): Array<WeightCheckItem?> {
            return arrayOfNulls(size)
        }
    }
}


