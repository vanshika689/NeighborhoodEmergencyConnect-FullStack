package com.example.neighborhoodemergencyconnect.models

data class AddAlertReq(
    val title: String,
    val description: String,
    val shortAddress: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl : String
)
