package com.example.neighborhoodemergencyconnect.models

data class SafetyTip(
    val title: String,
    val category: String,
    val shortDescription: String,
    val description: String,
    val dos: List<String>,
    val donts: List<String>,
    val emergencyNumber: String
)
