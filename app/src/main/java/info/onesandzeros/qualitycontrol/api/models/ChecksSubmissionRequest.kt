package info.onesandzeros.qualitycontrol.api.models

data class ChecksSubmissionRequest(
    val checkStartTimestamp: Long?,
    val username: String,
    val department: Department?,
    val line: Line?,
    val idhNumber: IDHNumbers?,
    val checkType: CheckType?,
    val checks: Map<String, List<CheckItem>> // Replace 'CheckItem' with the actual data type of the check item
)
