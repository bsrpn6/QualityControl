package info.onesandzeros.qualitycontrol.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import info.onesandzeros.qualitycontrol.api.models.Image
import info.onesandzeros.qualitycontrol.data.Converters

@Entity(tableName = "check_items")
@TypeConverters(Converters::class)
data class CheckItemEntity(
    @PrimaryKey val id: String,
    val section: String,
    val type: String,
    val title: String,
    val description: String,
    val expectedValue: Any?,
    val images: List<Image>?,
    val lineId: String,
    val checkTypeId: String,
    val productId: String
)
