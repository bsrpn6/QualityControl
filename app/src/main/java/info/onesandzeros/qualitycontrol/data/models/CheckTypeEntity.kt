package info.onesandzeros.qualitycontrol.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_types")
data class CheckTypeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "lineId") val lineId: String,
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "checks") val checks: List<String>?
)