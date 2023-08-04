package info.onesandzeros.qualitycontrol.api.models

data class ChecksSubmissionRequest(
    val checkStartTimestamp: Long?,
    val username: String,
    val department: Department?, // Replace 'Department?' with the actual data type of Department
    val line: Line?, // Replace 'Line?' with the actual data type of Line
    val idhNumber: IDHNumbers?,
    val checks: Map<String, List<CheckItem>> // Replace 'CheckItem' with the actual data type of the check item
)
