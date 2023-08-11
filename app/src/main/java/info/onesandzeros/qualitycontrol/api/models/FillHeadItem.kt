package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable

data class FillHeadItem(
    val fillHead: Int,
    val weight: Double?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(fillHead)
        parcel.writeValue(weight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FillHeadItem> {
        override fun createFromParcel(parcel: Parcel): FillHeadItem {
            return FillHeadItem(parcel)
        }

        override fun newArray(size: Int): Array<FillHeadItem?> {
            return arrayOfNulls(size)
        }
    }
}
