package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CheckType(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("lineId") val lineId: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("checks") val checks: List<String>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(lineId)
        parcel.writeString(displayName)
        parcel.writeStringList(checks)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckType

        if (id != other.id) return false
        if (name != other.name) return false
        if (lineId != other.lineId) return false
        if (displayName != other.displayName) return false
        if (checks != other.checks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + lineId.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + (checks?.hashCode() ?: 0)
        return result
    }

    companion object CREATOR : Parcelable.Creator<CheckType> {
        override fun createFromParcel(parcel: Parcel): CheckType {
            return CheckType(parcel)
        }

        override fun newArray(size: Int): Array<CheckType?> {
            return arrayOfNulls(size)
        }
    }

}