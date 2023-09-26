package info.onesandzeros.qualitycontrol.api.models

import android.net.Uri

data class ChecksSubmissionRequest(
    val checkStartTimestamp: Long?,
    val username: String,
    val department: Department?,
    val line: Line?,
    val idhNumber: IDHNumbers?,
    val checkType: CheckType?,
    val checks: Map<String, List<CheckItem>>,
    val photos: Map<String, MutableList<Uri>>?
)
