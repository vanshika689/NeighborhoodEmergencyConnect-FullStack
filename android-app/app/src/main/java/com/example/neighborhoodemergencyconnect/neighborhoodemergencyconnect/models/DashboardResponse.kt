package com.example.neighborhoodemergencyconnect.models

data class DashboardResponse(
    val role: String? = null,

    val totalusers: Int? = null,
    val totalCitizens: Int? = null,
    val totalvolunteers: Int? = null,

    val totalalerts: Int? = null,
    val totalactivealerts: Int? = null,
    val totalresolvedalerts: Int? = null,

    val totalalertsbyme: Int? = null,
    val totalactivealertsbyme: Int? = null,

    val alertsrespondedbyme: Int? = null,
    val activealertsrespondedbyme: Int? = null,
    val resolvedalertsrespondedbyme: Int? = null,

    val totalvolunteerreq : Int? = null,
    val totalapproved: Int? = null,
    val totalrejected: Int? = null,
    val totalpending: Int? = null
)
