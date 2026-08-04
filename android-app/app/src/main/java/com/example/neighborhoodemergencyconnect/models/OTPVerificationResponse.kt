package com.example.neighborhoodemergencyconnect.models

data class OTPVerificationResponse(
    val message: String?,
    val token: String?,
    val role: String?,
    val userId: String?
)
