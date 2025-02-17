package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Line(
    @SerializedName("_id") val id: String,
    @SerializedName("abbreviation") val abbreviation: String,
    @SerializedName("name") val name: String,
    @SerializedName("checkTypes") val checkTypes: List<String>?,
//    @SerializedName("departmentId") val departmentId: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(abbreviation)
        parcel.writeString(name)
        parcel.writeStringList(checkTypes)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Line

        if (id != other.id) return false
        if (abbreviation != other.abbreviation) return false
        if (name != other.name) return false
        if (checkTypes != other.checkTypes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + abbreviation.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (checkTypes?.hashCode() ?: 0)
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
