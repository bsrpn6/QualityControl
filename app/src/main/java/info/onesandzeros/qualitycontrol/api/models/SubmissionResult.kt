package info.onesandzeros.qualitycontrol.api.models

data class SubmissionResult(
    val success: Boolean,
    val message: String?,
    val uuid: String?
    // Add any additional properties as needed based on the server response
)
