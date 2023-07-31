package info.onesandzeros.qualitycontrol.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val department_id: Int,
    @ColumnInfo(name = "name") val name: String
)
