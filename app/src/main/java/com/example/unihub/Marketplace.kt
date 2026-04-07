package com.example.unihub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityMarketplaceBinding
import com.google.firebase.auth.FirebaseAuth

class Marketplace : AppCompatActivity() {

    private lateinit var binding: ActivityMarketplaceBinding

    private lateinit var db: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMarketplaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAddItem.setOnClickListener {
            addItem()
        }

        updateUI()
    }

    private fun addItem() {
        val title = binding.etItemTitle.text.toString().trim()
        val description = binding.etItemDescription.text.toString().trim()
        val category = binding.etItemCategory.text.toString().trim()
        val priceStr = binding.etItemPrice.text.toString().trim()
        val stockStr = binding.etItemStock.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (price == null || stock == null) {
            Toast.makeText(this, "Invalid price or stock", Toast.LENGTH_SHORT).show()
            return
        }

        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val success = db.insertMarketplaceItem(title, description, category, price, stock, userUid)

        if (success) {
            Toast.makeText(this, "Item posted successfully", Toast.LENGTH_SHORT).show()
            clearFields()
            updateUI()
        } else {
            Toast.makeText(this, "Failed to post item", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        binding.etItemTitle.text?.clear()
        binding.etItemDescription.text?.clear()
        binding.etItemCategory.text?.clear()
        binding.etItemPrice.text?.clear()
        binding.etItemStock.text?.clear()
    }

    private fun updateUI() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val count = db.getMarketplaceCountForUser(userUid)
        if (count > 0) {
            binding.tvMarketPlaceHint.text = "You have $count active post(s)."
        } else {
            binding.tvMarketPlaceHint.text = "You have no posts yet."
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}