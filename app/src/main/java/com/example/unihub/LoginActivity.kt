package com.example.unihub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * LoginActivity handles user authentication.
 * It allows users to log in using their email and password via Firebase Authentication.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: UserDatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use View Binding for UI interaction
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and local helpers
        auth = FirebaseAuth.getInstance()
        db = UserDatabaseHelper(this)
        sessionManager = SessionManager(this)

        // Set up click listener for login button
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        // Navigate to Registration screen
        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Attempts to log in the user using the provided credentials.
     * Validates input, disables the button during the process, and handles Firebase Auth callbacks.
     */
    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Simple validation for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Prevent multiple simultaneous login attempts
        binding.btnLogin.isEnabled = false

        // Firebase Sign-In
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser

                    if (firebaseUser != null) {
                        // Save session locally and redirect to Home
                        sessionManager.saveUserSession(firebaseUser.uid)

                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    // Show error message if login fails
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}