package com.example.unihub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Entry point activity that displays a splash screen.
 *
 * This activity serves as the initial landing page, providing a brief brand presence
 * while determining the user's authentication state. It checks if a user is already
 * logged in and if their profile exists in the local database to decide whether
 * to navigate to the Home screen or the Login screen.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Delay navigation to allow the splash animation/logo to be seen
        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 1400)
    }

    /**
     * Determines the next destination based on Firebase Auth and local profile availability.
     */
    private fun navigateNext() {
        val auth = FirebaseAuth.getInstance()
        val db = UserDatabaseHelper(this)
        val currentUser = auth.currentUser

        // If user is authenticated in Firebase AND has a local profile record, go to Home
        val intent = if (currentUser != null && db.getUserByFirebaseUid(currentUser.uid) != null) {
            Intent(this, HomeActivity::class.java)
        } else {
            // Otherwise, prompt for login/registration
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        // Finish splash so the user cannot navigate back to it
        finish()
    }
}