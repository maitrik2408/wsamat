package com.example.wsamat.data

data class Contact(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val isPrimary: Boolean = false
)
