package com.example.neighborhoodemergencyconnect.models

data class LoginResponse(
    val message: String,
    val token: String,
    val role: String
)
