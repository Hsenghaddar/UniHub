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
import android.content.Intent

/**
 * MyPostsActivity allows users to manage their own contributions to UniHub.
 * 
 * It displays a unified list of the user's marketplace listings and ride offers/requests.
 * Features include:
 * - Viewing post counts and status.
 * - Editing existing marketplace items (via dialog) or rides (via AddRideActivity).
 * - Multi-selection mode for bulk deletion of posts.
 * - Navigating to sales history or ride request management.
 */
class MyPostsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPostsBinding
    private lateinit var userDb: UserDatabaseHelper
    private lateinit var rideDb: DatabaseHelper
    private lateinit var adapter: MyPostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDb = UserDatabaseHelper(this)
        rideDb = DatabaseHelper(this)

        setupRecyclerView()

        // Handle back button behavior based on selection mode
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

        binding.btnSalesPannel.setOnClickListener {
            startActivity(Intent(this, SalesActivity::class.java))
        }

        loadData()

        // Register a back press callback to exit selection mode if active
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

    /**
     * Initializes the RecyclerView with a polymorphic adapter.
     * Configures callbacks for editing, viewing sales/requests, and selection changes.
     */
    private fun setupRecyclerView() {
        adapter = MyPostsAdapter(
            emptyList(),
            onEditClick = { post -> handleEditPost(post) },
            onSaleClick = { post -> handleSaleClick(post) },
            onInquiryClick = { post, senderUid, senderName -> handleInquiryClick(post, senderUid, senderName) },
            onSelectionChanged = { updateDeleteButtonVisibility() },
            dbHelper = rideDb
        )
        binding.rvMyPosts.layoutManager = LinearLayoutManager(this)
        binding.rvMyPosts.adapter = adapter
    }

    /**
     * Handles clicks on notifications or "Sale/Request" indicators.
     * Redirects to SalesActivity for marketplace items or RideRequestsActivity for rides.
     */
    private fun handleSaleClick(post: PostItem) {
        when (post) {
            is PostItem.Marketplace -> {
                userDb.setMarketplaceNotification(post.item.id, false)
                startActivity(Intent(this, SalesActivity::class.java))
            }
            is PostItem.RidePost -> {
                rideDb.setRideNotification(post.ride.id, false)
                val intent = Intent(this, RideRequestsActivity::class.java)
                intent.putExtra("RIDE_ID", post.ride.id)
                startActivity(intent)
            }
        }
    }

    /**
     * Opens a chat with a user who made an inquiry about a marketplace item.
     */
    private fun handleInquiryClick(post: PostItem, senderUid: String, senderName: String) {
        if (post is PostItem.Marketplace) {
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("RECEIVER_UID", senderUid)
                putExtra("RECEIVER_NAME", senderName)
                putExtra("ITEM_ID", post.item.id)
            }
            startActivity(intent)
        }
    }

    /**
     * Queries both databases for the current user's posts and updates the UI.
     * Combines MarketplaceItem and Ride objects into a single list of PostItem.
     */
    private fun loadData() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val userUid = firebaseUser.uid

        val marketplaceItems = userDb.getMarketplaceItemsByUser(userUid)
        val rides = rideDb.getRidesByUser(userUid)

        val combinedItems = mutableListOf<PostItem>()
        combinedItems.addAll(marketplaceItems.map { PostItem.Marketplace(it) })
        combinedItems.addAll(rides.map { PostItem.RidePost(it) })
        
        adapter.updateItems(combinedItems)

        val marketplaceCount = marketplaceItems.size
        val rideCount = rides.size

        binding.tvMarketplaceCount.text = marketplaceCount.toString()
        binding.tvRideCount.text = rideCount.toString()

        // Show hint if no posts exist
        if (marketplaceCount == 0 && rideCount == 0) {
            binding.tvMyPostsHint.visibility = View.VISIBLE
            binding.rvMyPosts.visibility = View.GONE
        } else {
            binding.tvMyPostsHint.visibility = View.GONE
            binding.rvMyPosts.visibility = View.VISIBLE
        }
    }

    /**
     * Toggles the visibility of the bulk delete button based on selection mode.
     */
    private fun updateDeleteButtonVisibility() {
        if (adapter.isSelectionMode()) {
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.GONE
        }
    }

    /**
     * Deletes all items currently selected in the adapter from their respective databases.
     */
    private fun deleteSelectedItems() {
        val selectedUniqueIds = adapter.getSelectedUniqueIds()
        if (selectedUniqueIds.isEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Delete Posts")
            .setMessage("Are you sure you want to delete ${selectedUniqueIds.size} post(s)?")
            .setPositiveButton("Delete") { _, _ ->
                var successCount = 0
                for (uniqueId in selectedUniqueIds) {
                    // Extract ID and Type from the unique key (e.g., "M-123" or "R-456")
                    val id = uniqueId.substring(2).toInt()
                    val type = uniqueId.substring(0, 1)
                    
                    val success = if (type == "M") {
                        userDb.deleteMarketplaceItem(id)
                    } else {
                        rideDb.deleteRide(id)
                    }
                    
                    if (success) successCount++
                }
                Toast.makeText(this, "Deleted $successCount posts", Toast.LENGTH_SHORT).show()
                adapter.exitSelectionMode()
                loadData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Triggers the edit flow for a specific post.
     * Shows a dialog for Marketplace items or starts AddRideActivity for Rides.
     */
    private fun handleEditPost(post: PostItem) {
        when (post) {
            is PostItem.Marketplace -> showMarketplaceEditDialog(post.item)
            is PostItem.RidePost -> {
                val intent = Intent(this, AddRideActivity::class.java)
                intent.putExtra("EDIT_MODE", true)
                intent.putExtra("RIDE_ID", post.ride.id)
                startActivity(intent)
            }
        }
    }

    /**
     * Shows a dialog to edit the text-based fields of a Marketplace item.
     */
    private fun showMarketplaceEditDialog(item: MarketplaceItem) {
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

                val success = userDb.updateMarketplaceItem(item.id, title, desc, cat, price, stock)
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

    override fun onResume() {
        super.onResume()
        loadData()
    }
}
