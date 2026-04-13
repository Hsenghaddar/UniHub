package com.example.unihub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity serves as the entry point of the application.
 * It checks the user's login status and redirects them to either the HomeActivity or LoginActivity.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SessionManager to check for an active user session
        sessionManager = SessionManager(this)
        
        // Check if user is already logged in via Firebase UID
        if (sessionManager.getSavedFirebaseUid() != null) {
            // User is logged in, navigate to Home Screen
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        // User is not logged in, show the landing page
        setContentView(R.layout.activity_main)

        // Set up "Get Started" button to navigate to Login screen
        findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}