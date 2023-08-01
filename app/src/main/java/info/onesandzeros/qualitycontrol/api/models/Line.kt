package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Line(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("departmentId") val departmentId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(departmentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Line> {
        override fun createFromParcel(parcel: Parcel): Line {
            return Line(parcel)
        }

        override fun newArray(size: Int): Array<Line?> {
            return arrayOfNulls(size)
        }
    }
}
