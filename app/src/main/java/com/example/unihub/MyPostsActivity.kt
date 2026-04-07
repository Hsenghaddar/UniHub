package com.example.unihub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unihub.databinding.ActivityMyPostsBinding
import com.example.unihub.databinding.DialogEditItemBinding
import com.google.firebase.auth.FirebaseAuth

class MyPostsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPostsBinding
    private lateinit var db: UserDatabaseHelper
    private lateinit var adapter: MyPostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)

        setupRecyclerView()

        binding.btnBack.setOnClickListener {
            if (adapter.isSelectionMode()) {
                adapter.exitSelectionMode()
            } else {
                finish()
            }
        }

        binding.btnDelete.setOnClickListener {
            deleteSelectedItems()
        }

        binding.btnSelect.setOnClickListener {
            if (adapter.isSelectionMode()) {
                adapter.exitSelectionMode()
            } else {
                adapter.enterSelectionMode()
            }
        }

        loadData()

        // Modern way to handle back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isSelectionMode()) {
                    adapter.exitSelectionMode()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = MyPostsAdapter(emptyList(), { item ->
            showEditDialog(item)
        }, {
            updateDeleteButtonVisibility()
        })
        binding.rvMyPosts.layoutManager = LinearLayoutManager(this)
        binding.rvMyPosts.adapter = adapter
    }

    private fun loadData() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val userUid = firebaseUser.uid

        val items = db.getMarketplaceItemsByUser(userUid)
        adapter.updateItems(items)

        val marketplaceCount = items.size
        val rideCount = db.getRideCountForUser(userUid)

        binding.tvMarketplaceCount.text = marketplaceCount.toString()
        binding.tvRideCount.text = rideCount.toString()

        if (marketplaceCount == 0 && rideCount == 0) {
            binding.tvMyPostsHint.visibility = View.VISIBLE
            binding.rvMyPosts.visibility = View.GONE
        } else {
            binding.tvMyPostsHint.visibility = View.GONE
            binding.rvMyPosts.visibility = View.VISIBLE
        }
    }

    private fun updateDeleteButtonVisibility() {
        if (adapter.isSelectionMode()) {
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.GONE
        }
    }

    private fun deleteSelectedItems() {
        val selectedIds = adapter.getSelectedIds()
        if (selectedIds.isEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Delete Posts")
            .setMessage("Are you sure you want to delete ${selectedIds.size} post(s)?")
            .setPositiveButton("Delete") { _, _ ->
                var successCount = 0
                for (id in selectedIds) {
                    if (db.deleteMarketplaceItem(id)) {
                        successCount++
                    }
                }
                Toast.makeText(this, "Deleted $successCount posts", Toast.LENGTH_SHORT).show()
                adapter.exitSelectionMode()
                loadData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(item: MarketplaceItem) {
        val dialogBinding = DialogEditItemBinding.inflate(LayoutInflater.from(this))
        
        dialogBinding.etEditTitle.setText(item.title)
        dialogBinding.etEditDescription.setText(item.description)
        dialogBinding.etEditCategory.setText(item.category)
        dialogBinding.etEditPrice.setText(item.price.toString())
        dialogBinding.etEditStock.setText(item.stock.toString())

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val title = dialogBinding.etEditTitle.text.toString()
                val desc = dialogBinding.etEditDescription.text.toString()
                val cat = dialogBinding.etEditCategory.text.toString()
                val price = dialogBinding.etEditPrice.text.toString().toDoubleOrNull() ?: item.price
                val stock = dialogBinding.etEditStock.text.toString().toIntOrNull() ?: item.stock

                val success = db.updateMarketplaceItem(item.id, title, desc, cat, price, stock)
                if (success) {
                    Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show()
                    loadData()
                } else {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
