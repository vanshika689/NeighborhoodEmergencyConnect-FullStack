package com.example.neighborhoodemergencyconnect.models

data class AiAnalysis(
    val isFake: Boolean?,
    val fakeReason: String?,
    val severity: String?,
    val severityReason: String?,
    val confidence: String?
)


