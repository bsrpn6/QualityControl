package info.onesandzeros.qualitycontrol.api

import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.api.models.SpecsResponse
import info.onesandzeros.qualitycontrol.api.models.SubmissionResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface MyApi {
    @GET("departments")
    fun getDepartments(): Call<List<Department>>

    @GET("lines")
    fun getLinesForDepartment(@Query("departmentId") departmentId: Int): Call<List<Line>>

    @GET("idhNumbers")
    fun getIDHNumbersForLine(@Query("lineId") lineId: Int): Call<List<IDHNumbers>>

    @GET("specs")
    fun getSpecs(@Query("idhNumber") idhNumbers: Int?): Call<List<SpecsResponse>>

    @GET("checks")
    fun getChecksData(): Call<List<CheckItem>>

    @POST("results")
    fun submitChecks(@Body submissionData: ChecksSubmissionRequest): Call<SubmissionResult>
}
