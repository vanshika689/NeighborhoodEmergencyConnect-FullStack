package com.example.neighborhoodemergencyconnect.api
import com.example.neighborhoodemergencyconnect.models.AddAlertReq
import com.example.neighborhoodemergencyconnect.models.AlertDetailsResponse
import com.example.neighborhoodemergencyconnect.models.AlertResponse
import com.example.neighborhoodemergencyconnect.models.DashboardResponse
import com.example.neighborhoodemergencyconnect.models.FcmTokenRequest
import com.example.neighborhoodemergencyconnect.models.LoginRequest
import com.example.neighborhoodemergencyconnect.models.LoginResponse
import com.example.neighborhoodemergencyconnect.models.MessageResponse
import com.example.neighborhoodemergencyconnect.models.ProfileResponse
import com.example.neighborhoodemergencyconnect.models.RegisterRequest
import com.example.neighborhoodemergencyconnect.models.RegisterResponse
import com.example.neighborhoodemergencyconnect.models.ReqVol
import com.example.neighborhoodemergencyconnect.models.UploadResponse
import com.example.neighborhoodemergencyconnect.models.VolReq
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register")
    suspend fun registerUser( ///suspend Fun(func may take time dont freeze ui)
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/alerts/")
    suspend fun getAlerts(
    ): Response<AlertResponse>

    @POST("api/alerts/")
    suspend fun AddAlert(
        @Header("Authorization") token: String,
        @Body request: AddAlertReq
    ): Response<AlertResponse>

    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("api/alerts/{id}")
    suspend fun getAlertById(
        @Path("id") alertId: String
    ): Response<AlertDetailsResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String,
    ): Response<ProfileResponse>

    @PATCH("api/auth/request-volunteer")
    suspend fun sendVolReq(
        @Header("Authorization") token: String,
    ): Response<ReqVol>

    @GET("api/alerts/my-alerts")
    suspend fun myalerts(
        @Header("Authorization") token: String,
    ): Response<AlertResponse>

    @GET("api/dashboard/")
    suspend fun getDashboard(
        @Header("Authorization") token: String,
    ): Response<DashboardResponse>

    @GET("api/auth/volunteer-requests")
    suspend fun getVolReq(
        @Header("Authorization") token: String,
    ): Response<VolReq>


    @PATCH("api/auth/approve-volunteer/{id}")
    suspend fun approveVolReq(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<VolReq>

    @PATCH("api/auth/reject-volunteer/{id}")
    suspend fun rejectVolReq(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<VolReq>

    @POST("api/alerts/{id}/respond")
    suspend fun respondToAlert(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<AlertResponse>

    @PATCH("api/auth/save-fcm-token")
    suspend fun saveFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): Response<MessageResponse>

}


