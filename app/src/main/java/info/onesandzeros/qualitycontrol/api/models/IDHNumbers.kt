package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class IDHNumbers(
    @SerializedName("lineId") val lineId: Int,
    @SerializedName("idhNumbers") val idhNumbers: List<Int>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        mutableListOf<Int>().apply {
            parcel.readList(this, Int::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(lineId)
        parcel.writeList(idhNumbers)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IDHNumbers> {
        override fun createFromParcel(parcel: Parcel): IDHNumbers {
            return IDHNumbers(parcel)
        }

        override fun newArray(size: Int): Array<IDHNumbers?> {
            return arrayOfNulls(size)
        }
    }
}
