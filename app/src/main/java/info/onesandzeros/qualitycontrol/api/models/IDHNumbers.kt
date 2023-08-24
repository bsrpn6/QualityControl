package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class IDHNumbers(
    @SerializedName("_id") val id: String,
    @SerializedName("productId") val productId: Int,
    @SerializedName("lineId") val lineId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(productId)
        parcel.writeInt(lineId)
        parcel.writeString(name)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IDHNumbers

        if (id != other.id) return false
        if (productId != other.productId) return false
        if (lineId != other.lineId) return false
        if (name != other.name) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + productId
        result = 31 * result + lineId
        result = 31 * result + name.hashCode()
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
