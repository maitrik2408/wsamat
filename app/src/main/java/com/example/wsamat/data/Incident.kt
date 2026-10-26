package com.example.wsamat.data

data class Incident(
    val id: Long = 0,
    val timestamp: Long,
    val eventType: String,
    val locationText: String,
    val mapUrl: String,
    val status: String,
    val photoPath: String? = null,
    val audioPath: String? = null
)
