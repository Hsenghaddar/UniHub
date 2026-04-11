package com.example.unihub

data class LocalUser(
    val firebaseUid: String,
    val fullName: String,
    val email: String,
    val universityId: Int,
    val imageUri: String? = null
)
