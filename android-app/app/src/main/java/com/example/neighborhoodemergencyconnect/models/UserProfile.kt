package com.example.neighborhoodemergencyconnect.models

data class UserProfile(
    val _id: String,
    val name: String,
    val email: String,
    val role: String,
    val volunteerRequestStatus : String
)
