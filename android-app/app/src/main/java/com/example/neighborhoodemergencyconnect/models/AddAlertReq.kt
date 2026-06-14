package com.example.neighborhoodemergencyconnect.models

data class AddAlertReq(
    val title: String,
    val description: String,
    val location : String,
    val imageUrl : String
)
