package com.example.neighborhoodemergencyconnect.models

data class RegisterResponse(
    val message: String,
    val email: String,
    val requiresOTP: Boolean
)
