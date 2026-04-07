package com.example.unihub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.unihub.databinding.ActivityItemDetailBinding
import com.google.firebase.auth.FirebaseAuth

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var db: UserDatabaseHelper
    private var item: MarketplaceItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)
        
        item = intent.getSerializableExtra("ITEM_DATA") as? MarketplaceItem

        if (item == null) {
            Toast.makeText(this, "Error loading item details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayItemDetails()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnBuy.setOnClickListener {
            buyItem()
        }
    }

    private fun displayItemDetails() {
        item?.let {
            binding.tvDetailTitle.text = it.title
            binding.tvDetailCategory.text = it.category
            binding.tvDetailPrice.text = "$${it.price}"
            binding.tvDetailStock.text = "Stock: ${it.stock}"
            binding.tvDetailDescription.text = it.description
            binding.tvDetailCreator.text = "Added by: ${it.creatorName ?: "Unknown"}"
            
            if (it.stock <= 0) {
                binding.btnBuy.isEnabled = false
                binding.btnBuy.text = "Out of Stock"
                binding.etQuantity.isEnabled = false
            } else {
                binding.btnBuy.isEnabled = true
                binding.btnBuy.text = "Buy Item"
                binding.etQuantity.isEnabled = true
            }
        }
    }

    private fun buyItem() {
        val currentItem = item ?: return
        val quantityStr = binding.etQuantity.text.toString()
        val quantity = quantityStr.toIntOrNull() ?: 1

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Toast.makeText(this, "Please log in to buy items", Toast.LENGTH_SHORT).show()
            return
        }

        if (quantity <= 0) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }

        if (quantity > currentItem.stock) {
            Toast.makeText(this, "Not enough stock! Max available: ${currentItem.stock}", Toast.LENGTH_SHORT).show()
            return
        }


        val buyerName = db.getUserByFirebaseUid(firebaseUser.uid)?.fullName ?: "A buyer"
        val newStock = currentItem.stock - quantity
        val success = db.updateMarketplaceStock(currentItem.id, newStock)
        
        if (success) {
            showSuccessDialog(buyerName)
            item = currentItem.copy(stock = newStock)
            displayItemDetails()
        } else {
            Toast.makeText(this, "Purchase failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessDialog(buyerName: String) {
        AlertDialog.Builder(this)
            .setTitle("Purchase Successful!")
            .setMessage("The buyer, $buyerName, will be in contact with you soon.")
            .setIcon(android.R.drawable.ic_menu_myplaces) // Using a built-in icon that looks somewhat like a bill/place
            .setPositiveButton("OK", null)
            .show()
    }
}
