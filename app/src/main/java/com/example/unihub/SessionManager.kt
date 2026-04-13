package com.example.unihub

import android.content.Context

/**
 * SessionManager handles persistent storage of user session data using SharedPreferences.
 * It primarily tracks the logged-in user's Firebase UID to maintain state across app restarts.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("unihub_session", Context.MODE_PRIVATE)

    /**
     * Saves the Firebase UID to SharedPreferences when a user logs in.
     */
    fun saveUserSession(firebaseUid: String) {
        prefs.edit().putString("firebase_uid", firebaseUid).apply()
    }

    /**
     * Retrieves the saved Firebase UID from SharedPreferences.
     * Returns null if no user is logged in.
     */
    fun getSavedFirebaseUid(): String? {
        return prefs.getString("firebase_uid", null)
    }

    /**
     * Clears all session data, effectively logging the user out locally.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}