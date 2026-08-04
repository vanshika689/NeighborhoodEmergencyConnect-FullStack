package com.example.neighborhoodemergencyconnect.storage

object TokenManager {
    // Store current token (e.g., "Bearer <token>")
    @Volatile
    var token: String? = null
}
