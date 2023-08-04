package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class IDHNumbers(
    @SerializedName("idhNumber") val idhNumber: Int,
    @SerializedName("lineId") val lineId: Int,
    @SerializedName("description") val description: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(idhNumber)
        parcel.writeInt(lineId)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    // Override the equals method to compare the content of two IDHNumbers objects
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IDHNumbers) return false

        if (lineId != other.lineId) return false
        if (idhNumber != other.idhNumber) return false
        if (description != other.description) return false

        return true
    }

    // Override the hashCode method when overriding equals
    override fun hashCode(): Int {
        var result = lineId
        result = 31 * result + idhNumber
        result = 31 * result + description.hashCode()
        return result
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
