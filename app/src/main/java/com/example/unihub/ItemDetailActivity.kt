package com.example.unihub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        displayItemDetails()

        binding.btnBuy.setOnClickListener {
            buyItem()
        }

        binding.btnMessageSeller.setOnClickListener {
            messageSeller()
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
            
            if (it.imageUri != null) {
                try {
                    ImageUtils.loadImage(this, Uri.parse(it.imageUri), binding.ivItemImage)
                } catch (e: Exception) {
                    binding.ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                binding.ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (it.userUid == currentUser?.uid) {
                binding.btnMessageSeller.visibility = View.GONE
                binding.btnBuy.visibility = View.GONE
                binding.layoutQuantity.visibility = View.GONE
                
                // If the owner is viewing their own item, clear the notification
                if (it.hasNotification) {
                    db.setMarketplaceNotification(it.id, false)
                }
            }

            if (it.stock <= 0) {
                binding.btnBuy.isEnabled = false
                binding.btnBuy.text = "Out of Stock"
                binding.etQuantity.isEnabled = false
            } else {
                binding.btnBuy.isEnabled = true
                binding.btnBuy.text = "Buy Now"
                binding.etQuantity.isEnabled = true
            }
        }
    }

    private fun messageSeller() {
        val currentItem = item ?: return
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to message the seller", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("RECEIVER_UID", currentItem.userUid)
            putExtra("RECEIVER_NAME", currentItem.creatorName ?: "Seller")
            putExtra("ITEM_ID", currentItem.id) 
        }
        startActivity(intent)
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
        val totalPrice = currentItem.price * quantity
        
        // Record the sale in the sales table AND update stock (which sets notification)
        val saleRecorded = db.recordSale(currentItem.id, firebaseUser.uid, quantity, totalPrice)
        val stockUpdated = db.updateMarketplaceStock(currentItem.id, newStock)
        
        if (saleRecorded && stockUpdated) {
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
            .setMessage("The seller has been notified. You can also message them directly.")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("OK", null)
            .show()
    }
}
