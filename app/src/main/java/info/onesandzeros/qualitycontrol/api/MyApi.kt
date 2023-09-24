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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Singleton

@Singleton
interface MyApi {
    @GET("GetDepartments")
    suspend fun getDepartmentsForSite(@Query("siteId") siteId: String): List<Department>

    @GET("GetLines")
    suspend fun getLinesForDepartment(@Query("departmentId") departmentId: String): List<Line>

    @GET("GetCheckTypes")
    suspend fun getCheckTypesForLine(@Query("lineId") lineId: String): List<CheckType>

    @GET("GetProducts")
    suspend fun getProductsForLine(@Query("lineId") lineId: String): List<IDHNumbers>

    @GET("GetProductSpecs")
    suspend fun getSpecsForProduct(@Query("productId") productId: String?): Response<ProductSpecsResponse>

    @GET("GetChecks")
    suspend fun getChecks(
        @Query("lineId") lineId: String,
        @Query("checkTypeId") checkTypeId: String,
        @Query("productId") productId: String
    ): List<CheckItem>

    @POST("results")
    fun submitChecks(@Body submissionData: ChecksSubmissionRequest): Call<SubmissionResult>
}
