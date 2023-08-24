package info.onesandzeros.qualitycontrol.api

import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.ChecksSubmissionRequest
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.api.models.ProductSpecsResponse
import info.onesandzeros.qualitycontrol.api.models.SubmissionResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface MyApi {
    @GET("GetDepartments")
    fun getDepartments(@Query("siteId") siteId: String): Call<List<Department>>

    @GET("GetLines")
    fun getLinesForDepartment(@Query("departmentId") departmentId: String): Call<List<Line>>

    @GET("GetCheckTypes")
    fun getCheckTypesForLine(@Query("lineId") lineId: String): Call<List<CheckType>>

    @GET("GetProducts")
    fun getIDHNumbersForLine(@Query("lineId") lineId: String): Call<List<IDHNumbers>>

    @GET("GetProductSpecs")
    fun getSpecs(@Query("productId") productId: String?): Call<ProductSpecsResponse>

    @GET("GetChecks")
    fun getChecks(
        @Query("lineId") lineId: String,
        @Query("checkTypeId") checkTypeId: String,
        @Query("productId") productId: String
    ): Call<List<CheckItem>>

    @POST("results")
    fun submitChecks(@Body submissionData: ChecksSubmissionRequest): Call<SubmissionResult>
}
