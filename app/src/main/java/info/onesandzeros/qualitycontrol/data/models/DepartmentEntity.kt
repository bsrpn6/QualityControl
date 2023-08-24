package info.onesandzeros.qualitycontrol.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val departmentId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "abbreviation") val abbreviation: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "lines") val lines: List<String>?
)
