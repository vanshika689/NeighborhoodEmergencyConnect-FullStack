package com.example.neighborhoodemergencyconnect.api

import com.example.neighborhoodemergencyconnect.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>


    @POST("api/auth/resend-otp")
    suspend fun resendOtp(
        @Body body: Map<String, String>
    ): Response<MessageResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<EditProfileResponse>

    @PUT("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordReq
    ): Response<ChangePasswordResponse>

    @PATCH("api/auth/save-fcm-token")
    suspend fun saveFcmToken(
        @Header("Authorization") token: String,
        @Body request: FcmTokenRequest
    ): Response<MessageResponse>


    @PATCH("api/auth/request-volunteer")
    suspend fun sendVolReq(
        @Header("Authorization") token: String
    ): Response<ReqVol>

    @GET("api/auth/volunteer-requests")
    suspend fun getVolReq(
        @Header("Authorization") token: String
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


    @GET("api/alerts/")
    suspend fun getAlerts(): Response<AlertResponse>

    @POST("api/alerts/")
    suspend fun addAlert(
        @Header("Authorization") token: String,
        @Body request: AddAlertReq
    ): Response<AlertResponse>

    @GET("api/alerts/my-alerts")
    suspend fun getMyAlerts(
        @Header("Authorization") token: String
    ): Response<AlertResponse>

    @GET("api/alerts/my-responses")
    suspend fun getMyResponses(
        @Header("Authorization") token: String
    ): Response<AlertResponse>

    @GET("api/alerts/{id}")
    suspend fun getAlertById(
        @Path("id") alertId: String
    ): Response<AlertDetailResponse>

    @POST("api/alerts/{id}/respond")
    suspend fun respondToAlert(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<AlertResponse>

    @PATCH("api/alerts/{id}/resolve")
    suspend fun updateAlertStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ResolveRequest
    ): Response<AlertDetailsResponse>


    @GET("api/dashboard/")
    suspend fun getDashboard(
        @Header("Authorization") token: String
    ): Response<DashboardResponse>

    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>
}
