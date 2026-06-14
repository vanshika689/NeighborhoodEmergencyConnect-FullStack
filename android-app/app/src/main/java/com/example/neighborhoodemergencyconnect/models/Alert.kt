package com.example.neighborhoodemergencyconnect.models

data class Alert(
    val _id: String,
    val title: String,
    val description: String,
    val location: String,
    val status:String,
    val imageUrl : String,
    val createdBy : UserInfo,
    val createdAt : String,
    val responders : List<UserInfo>
)
