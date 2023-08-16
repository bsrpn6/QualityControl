package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable

data class SpecsResponse(
    val Case: SpecsItem,
    val Package: SpecsItem,
    val Product: SpecsItem
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(SpecsItem::class.java.classLoader)!!,
        parcel.readParcelable(SpecsItem::class.java.classLoader)!!,
        parcel.readParcelable(SpecsItem::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(Case, flags)
        parcel.writeParcelable(Package, flags)
        parcel.writeParcelable(Product, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpecsResponse> {
        override fun createFromParcel(parcel: Parcel): SpecsResponse {
            return SpecsResponse(parcel)
        }

        override fun newArray(size: Int): Array<SpecsResponse?> {
            return arrayOfNulls(size)
        }
    }
}

data class SpecsItem(
    val barcode_value: String,
    val date_code_value: String,
    val weight_specs: WeightSpecs?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(WeightSpecs::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(barcode_value)
        parcel.writeString(date_code_value)
        parcel.writeParcelable(weight_specs, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpecsItem> {
        override fun createFromParcel(parcel: Parcel): SpecsItem {
            return SpecsItem(parcel)
        }

        override fun newArray(size: Int): Array<SpecsItem?> {
            return arrayOfNulls(size)
        }
    }
}

data class WeightSpecs(
    val tarWt: Int,
    val lsl: Int,
    val usl: Int,
    val mav: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(tarWt)
        parcel.writeInt(lsl)
        parcel.writeInt(usl)
        parcel.writeInt(mav)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WeightSpecs> {
        override fun createFromParcel(parcel: Parcel): WeightSpecs {
            return WeightSpecs(parcel)
        }

        override fun newArray(size: Int): Array<WeightSpecs?> {
            return arrayOfNulls(size)
        }
    }
}
