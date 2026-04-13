package com.example.unihub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.net.Uri

/**
 * HomeActivity serves as the main dashboard for the user after logging in.
 * It displays user information and provides navigation to various features like
 * Marketplace, Rides, Posts, Chats, and Profile.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var db: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use View Binding for UI interaction
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize local database helper
        db = UserDatabaseHelper(this)

        loadUserInfo()
        setupClicks()
    }

    /**
     * Reload user information whenever the activity is resumed to ensure data consistency.
     */
    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    /**
     * Fetches current user information from Firebase and the local database.
     * Updates the UI with the user's name, university, and profile image.
     */
    private fun loadUserInfo() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            Toast.makeText(this, "No logged in user found", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user details from the local SQLite database using Firebase UID
        val localUser = db.getUserByFirebaseUid(firebaseUser.uid)

        if (localUser == null) {
            Toast.makeText(this, "User data not found in local database", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract first name for a more personal greeting
        val firstName = localUser.fullName.split(" ").firstOrNull() ?: localUser.fullName
        val universityName = db.getUniversityNameById(localUser.universityId)

        // Update UI elements
        binding.tvGreeting.text = "Hello, $firstName"
        binding.tvUniversity.text = universityName

        // Load profile picture using ImageUtils helper
        if (localUser.imageUri != null) {
            try {
                ImageUtils.loadImage(this, Uri.parse(localUser.imageUri), binding.ivProfileImage)
            } catch (e: Exception) {
                // Fallback to default icon on error
                binding.ivProfileImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            binding.ivProfileImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    /**
     * Sets up click listeners for all dashboard cards to navigate to respective activities.
     */
    private fun setupClicks() {
        binding.cardMarketplace.setOnClickListener {
            startActivity(Intent(this, Marketplace::class.java))
        }

        binding.cardRides.setOnClickListener {
            startActivity(Intent(this, RidesActivity::class.java))
        }

        binding.cardMyPosts.setOnClickListener {
            startActivity(Intent(this, MyPostsActivity::class.java))
        }

        binding.cardChats.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}
