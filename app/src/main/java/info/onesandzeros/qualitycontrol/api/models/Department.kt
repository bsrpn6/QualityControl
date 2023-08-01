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

    companion object CREATOR : Parcelable.Creator<Department> {
        override fun createFromParcel(parcel: Parcel): Department {
            return Department(parcel)
        }

        override fun newArray(size: Int): Array<Department?> {
            return arrayOfNulls(size)
        }
    }
}

