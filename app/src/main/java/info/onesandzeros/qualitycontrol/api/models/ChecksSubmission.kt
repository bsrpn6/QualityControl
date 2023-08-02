package info.onesandzeros.qualitycontrol.api.models

import info.onesandzeros.qualitycontrol.api.models.Department

data class ChecksSubmissionRequest(
    val username: String,
    val department: Department?, // Replace 'Department?' with the actual data type of Department
    val line: String?, // Replace 'Line?' with the actual data type of Line
    val idhNumber: Int,
    val checks: Map<String, List<CheckItem>> // Replace 'CheckItem' with the actual data type of the check item
)
