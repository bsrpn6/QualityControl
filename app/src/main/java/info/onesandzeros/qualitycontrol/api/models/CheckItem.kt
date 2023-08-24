package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable

class CheckItem(
    val id: String,
    val section: String,
    val type: String,
    val title: String,
    val description: String,
    val expectedValue: Any?,
    var result: Any? = null // Add the property for storing user-inputted value
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Any::class.java.classLoader),
        parcel.readValue(Any::class.java.classLoader) // Read the user input value from the parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(section)
        parcel.writeString(type)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeValue(expectedValue)
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
