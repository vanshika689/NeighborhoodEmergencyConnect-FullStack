package com.example.neighborhoodemergencyconnect.models

data class ResolveRequest(
    val status : String,
    val volunteerLat: Double,
    val volunteerLng : Double
)
