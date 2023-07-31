package info.onesandzeros.qualitycontrol.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "idh_numbers",
    foreignKeys = [ForeignKey(
        entity = LineEntity::class,
        parentColumns = ["line_id"],
        childColumns = ["lineId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("lineId")] // Add this line to create an index for lineId
)
data class IDHNumbersEntity(
    @PrimaryKey val lineId: Int,
    @ColumnInfo(name = "idh_numbers") val idhNumbers: List<Int>
)
