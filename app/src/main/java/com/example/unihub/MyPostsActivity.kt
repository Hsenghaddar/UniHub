package com.example.unihub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityMyPostsBinding
import com.google.firebase.auth.FirebaseAuth

class MyPostsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPostsBinding
    private lateinit var db: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        loadCounts()
    }

    override fun onResume() {
        super.onResume()
        loadCounts()
    }

    private fun loadCounts() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return

        val marketplaceCount = db.getMarketplaceCountForUser(firebaseUser.uid)
        val rideCount = db.getRideCountForUser(firebaseUser.uid)

        binding.tvMarketplaceCount.text = marketplaceCount.toString()
        binding.tvRideCount.text = rideCount.toString()

        binding.tvMyPostsHint.text =
            if (marketplaceCount == 0 && rideCount == 0) {
                "You have no posts yet.\nYour future marketplace and ride posts will appear here."
            } else {
                "Your current posts summary is shown above."
            }
    }
}