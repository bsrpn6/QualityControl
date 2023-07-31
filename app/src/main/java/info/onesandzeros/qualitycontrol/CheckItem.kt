package info.onesandzeros.qualitycontrol

import android.os.Parcel
import android.os.Parcelable

class CheckItem(
    val checkType: String,
    val type: String,
    val title: String,
    val description: String,
    val value: Any?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Any::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(checkType)
        parcel.writeString(type)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeValue(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CheckItem> {
        override fun createFromParcel(parcel: Parcel): CheckItem {
            return CheckItem(parcel)
        }

        override fun newArray(size: Int): Array<CheckItem?> {
            return arrayOfNulls(size)
        }
    }
}
