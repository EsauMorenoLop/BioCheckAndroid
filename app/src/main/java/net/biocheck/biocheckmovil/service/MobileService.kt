package net.biocheck.biocheckmovil.service

import net.biocheck.biocheckmovil.model.ApiData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/**
 * Created by Gabriel Martinez on 09/01/2018.
 */
interface MobileService {
    @POST("login")
    fun login(@Body user:ApiData.User):Call<String>

    @GET("api/v1/employee/")
    fun getEmployee(@Header("authorization") token:String):Call<ApiData.Employee>

    @Multipart
    @POST("api/v1/photo/enroll")
    fun doEnroll(@Header("authorization") token:String, @Part("photo\"; filename=\"face.jpg\"; type=\"image/png\" ") photo:RequestBody):Call<ApiData.EnrollState>

    @Multipart
    @POST("api/v1/photo/validate")
    fun doValidate(@Header("authorization") token:String, @Part("photo\"; filename=\"face.jpg\"; type=\"image/png\" ") photo:RequestBody):Call<String>

    @GET("api/v1/employee/daySettings")
    fun getDaySettings(@Header("authorization") token:String,  @Query("date") date:Date):Call<ApiData.DaySettings>

    @PUT("api/v1/employee/record")
    fun putCheckRecord(@Header("authorization") token:String, @Body check:ApiData.Check):Call<ApiData.Check>

    @PUT("api/v1/employee/recordRoutes")
    fun putCheckRecordRoute(@Header("authorization") token:String, @Body check:ApiData.Check,
                            @Query("rlId") routeLocationId:Long,
                            @Query("locId") locationId:Long):Call<ApiData.Check>

    @GET("api/v1/employee/record/")
    fun getRecords(@Header("authorization") token:String , @Query("initDate") initDate:Date,
                   @Query("endDate") endDate:Date):Call<List<ApiData.Record>>
}