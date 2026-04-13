package com.example.unihub

/**
 * Data class representing a user in the local SQLite database.
 * 
 * While authentication is handled by Firebase, user profile details (name, university affiliation, 
 * and profile picture URI) are stored locally in the `UserDatabaseHelper` for fast access 
 * and offline support.
 *
 * @property firebaseUid The unique identifier provided by Firebase Auth, used as a primary key.
 * @property fullName The user's full display name.
 * @property email The user's email address.
 * @property universityId An integer ID representing the university the user belongs to.
 * @property imageUri A string representation of the Uri pointing to the user's profile picture.
 */
data class LocalUser(
    val firebaseUid: String,
    val fullName: String,
    val email: String,
    val universityId: Int,
    val imageUri: String? = null
)
