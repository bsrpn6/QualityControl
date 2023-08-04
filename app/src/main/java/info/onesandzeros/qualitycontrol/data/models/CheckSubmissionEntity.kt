package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line

@Entity(tableName = "check_submissions")
data class CheckSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checkStartTimestamp: Long?,
    val username: String,
    val department: Department?,
    val line: Line?,
    val idhNumber: IDHNumbers?,
    val checks: Map<String, List<CheckItem>>
)
