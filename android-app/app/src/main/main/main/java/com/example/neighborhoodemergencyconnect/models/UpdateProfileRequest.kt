package com.example.neighborhoodemergencyconnect.models

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val profileImage: String?
)
