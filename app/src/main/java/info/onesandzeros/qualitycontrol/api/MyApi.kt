package info.onesandzeros.qualitycontrol.api

import info.onesandzeros.qualitycontrol.CheckItem
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import retrofit2.Call
import retrofit2.http.GET
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

    @GET("checks")
    fun getChecksData(): Call<List<CheckItem>>
}
