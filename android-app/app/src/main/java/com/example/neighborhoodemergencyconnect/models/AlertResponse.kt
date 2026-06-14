package com.example.neighborhoodemergencyconnect.models

data class AlertResponse(
    val message: String,
    val alerts: List<Alert>
)

