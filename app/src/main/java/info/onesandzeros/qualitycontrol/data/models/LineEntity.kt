package info.onesandzeros.qualitycontrol.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lines",
    foreignKeys = [ForeignKey(
        entity = DepartmentEntity::class,
        parentColumns = ["department_id"],
        childColumns = ["departmentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("departmentId")] // Add this line to create an index for departmentId
)
data class LineEntity(
    @PrimaryKey val line_id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "departmentId") val departmentId: Int
)
