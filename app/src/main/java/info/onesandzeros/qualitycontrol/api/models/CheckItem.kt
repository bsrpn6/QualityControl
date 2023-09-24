package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable

class CheckItem(
    val _id: String,
    val section: String,
    val type: String,
    val title: String,
    val description: String,
    val expectedValue: Any?,
    val images: List<Image>?,
    var result: Any? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readValue(Any::class.java.classLoader),
        parcel.createTypedArrayList(Image.CREATOR)
            ?: listOf(),  // Read the images list from the parcel
        parcel.readValue(Any::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(section)
        parcel.writeString(type)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeValue(expectedValue)
        parcel.writeTypedList(images)  // Write the images list to the parcel
        parcel.writeValue(result)
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

data class Image(
    val url: String,
    val title: String,  // New title property
    val description: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(title)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }
}
