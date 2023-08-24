package info.onesandzeros.qualitycontrol.api.models

import android.os.Parcel
import android.os.Parcelable

data class ProductSpecsResponse(
    val product: ProductItem,
    val formula: FormulaItem
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(ProductItem::class.java.classLoader)!!,
        parcel.readParcelable(FormulaItem::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(product, flags)
        parcel.writeParcelable(formula, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductSpecsResponse> {
        override fun createFromParcel(parcel: Parcel): ProductSpecsResponse {
            return ProductSpecsResponse(parcel)
        }

        override fun newArray(size: Int): Array<ProductSpecsResponse?> {
            return arrayOfNulls(size)
        }
    }
}

data class ProductItem(
    val name: String,
    val description: String,
    val caseBarcode: Int,
    val caseSku: String,
    val productBarcode: Int,
    val consPerCase: Int,
    val formulaId: String,
    val category: String,
    val volume: String,
    val productId: Int,
    val checks: List<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.createStringArrayList()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(caseBarcode)
        parcel.writeString(caseSku)
        parcel.writeInt(productBarcode)
        parcel.writeInt(consPerCase)
        parcel.writeString(formulaId)
        parcel.writeString(category)
        parcel.writeString(volume)
        parcel.writeInt(productId)
        parcel.writeStringList(checks)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductItem> {
        override fun createFromParcel(parcel: Parcel): ProductItem {
            return ProductItem(parcel)
        }

        override fun newArray(size: Int): Array<ProductItem?> {
            return arrayOfNulls(size)
        }
    }
}

data class FormulaItem(
    val _id: String,
    val description: String,
    val spg: Double,
    val tarWt: Double,
    val usl: Double,
    val lsl: Double,
    val mav: Double,
    val formulaId: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_id)
        parcel.writeString(description)
        parcel.writeDouble(spg)
        parcel.writeDouble(tarWt)
        parcel.writeDouble(usl)
        parcel.writeDouble(lsl)
        parcel.writeDouble(mav)
        parcel.writeInt(formulaId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FormulaItem> {
        override fun createFromParcel(parcel: Parcel): FormulaItem {
            return FormulaItem(parcel)
        }

        override fun newArray(size: Int): Array<FormulaItem?> {
            return arrayOfNulls(size)
        }
    }
}
