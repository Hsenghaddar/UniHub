package com.example.unihub

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("unihub_session", Context.MODE_PRIVATE)

    fun saveUserSession(firebaseUid: String) {
        prefs.edit().putString("firebase_uid", firebaseUid).apply()
    }

    fun getSavedFirebaseUid(): String? {
        return prefs.getString("firebase_uid", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}