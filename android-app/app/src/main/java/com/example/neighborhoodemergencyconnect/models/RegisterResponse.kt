package com.example.neighborhoodemergencyconnect.models

data class RegisterResponse(
    val message: String,
    val token: String,
    val role: String,
    val userId : String
)
