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
        childColumns = ["line_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(
            value = ["line_id", "idh_number"],
            unique = true
        ) // Create a unique index for lineId and idhNumber combination
    ]
)
data class IDHNumbersEntity(
    @ColumnInfo(name = "idh_number") val idhNumber: Int,
    @ColumnInfo(name = "line_id") val lineId: Int,
    @ColumnInfo(name = "description") val description: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}