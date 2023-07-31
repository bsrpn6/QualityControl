package info.onesandzeros.qualitycontrol

import android.os.Parcel
import android.os.Parcelable

class CheckItem(
    val checkId: Int,
    val checkType: String,
    val type: String,
    val title: String,
    val description: String,
    val value: Any?,
    var result: Any? = null // Add the property for storing user-inputted value
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt() ?: 0,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Any::class.java.classLoader),
        parcel.readValue(Any::class.java.classLoader) // Read the user input value from the parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(checkId)
        parcel.writeString(checkType)
        parcel.writeString(type)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeValue(value)
        parcel.writeValue(result) // Write the user input value to the parcel
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
