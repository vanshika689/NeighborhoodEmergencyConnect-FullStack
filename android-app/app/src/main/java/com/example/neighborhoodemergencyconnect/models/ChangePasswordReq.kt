package com.example.neighborhoodemergencyconnect.models

data class ChangePasswordReq(
    val oldPassword: String,
    val newPassword: String
)
