package com.example.unihub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var db: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)

        loadUserInfo()
        setupClicks()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            Toast.makeText(this, "No logged in user found", Toast.LENGTH_SHORT).show()
            return
        }

        val localUser = db.getUserByFirebaseUid(firebaseUser.uid)

        if (localUser == null) {
            Toast.makeText(this, "User data not found in local database", Toast.LENGTH_SHORT).show()
            return
        }

        val firstName = localUser.fullName.split(" ").firstOrNull() ?: localUser.fullName
        val universityName = db.getUniversityNameById(localUser.universityId)

        binding.tvGreeting.text = "Hello, $firstName"
        binding.tvUniversity.text = universityName
    }

    private fun setupClicks() {
        binding.cardMarketplace.setOnClickListener {
            startActivity(Intent(this, Marketplace::class.java))
//            Toast.makeText(this, "Marketplace screen will be added later", Toast.LENGTH_SHORT).show()
        }

        binding.cardRides.setOnClickListener {
            Toast.makeText(this, "Ride sharing screen will be added later", Toast.LENGTH_SHORT).show()
        }

        binding.cardMyPosts.setOnClickListener {
            startActivity(Intent(this, MyPostsActivity::class.java))
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}