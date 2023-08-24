package info.onesandzeros.qualitycontrol.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "idh_numbers")
data class IDHNumbersEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "productId") val productId: Int,
    @ColumnInfo(name = "lineId") val lineId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String
)