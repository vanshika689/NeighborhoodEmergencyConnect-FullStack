package com.example.neighborhoodemergencyconnect.models

data class OTPVerificationRequest(
    val email: String,
    val otp: String
)
