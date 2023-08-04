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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Line) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (departmentId != other.departmentId) return false

        return true
    }

    // Override the hashCode method when overriding equals
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + departmentId
        return result
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
