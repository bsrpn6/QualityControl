package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Department(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("abbreviation")
    val abbreviation: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("lines")
    val lines: List<String>? = null // This is nullable because some departments might not have lines.
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(abbreviation)
        parcel.writeString(description)
        parcel.writeStringList(lines)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Department

        if (id != other.id) return false
        if (name != other.name) return false
        if (abbreviation != other.abbreviation) return false
        if (description != other.description) return false
        if (lines != other.lines) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + abbreviation.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (lines?.hashCode() ?: 0)
        return result
    }

    companion object CREATOR : Parcelable.Creator<Department> {
        override fun createFromParcel(parcel: Parcel): Department {
            return Department(parcel)
        }

        override fun newArray(size: Int): Array<Department?> {
            return arrayOfNulls(size)
        }
    }
}

