package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Department(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Department) return false

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    // Override the hashCode method when overriding equals
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
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

