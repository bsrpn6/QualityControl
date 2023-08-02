package info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.Department

@Entity(tableName = "check_submissions")
data class CheckSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val department: Department?,
    val line: String?,
    val idhNumber: Int?,
    val checks: Map<String, List<CheckItem>>
)
