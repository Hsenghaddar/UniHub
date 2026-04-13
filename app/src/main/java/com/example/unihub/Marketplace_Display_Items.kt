package com.example.unihub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.unihub.databinding.ActivityMarketplaceDisplayItemsBinding

/**
 * Activity for browsing all available marketplace items.
 *
 * This activity displays a grid-based list of products posted by all users across the platform.
 * It fetches data from the local SQLite database and uses a MarketplaceAdapter to render items
 * in a 2-column grid layout, similar to popular marketplace applications.
 */
class Marketplace_Display_Items : AppCompatActivity() {
    private lateinit var binding: ActivityMarketplaceDisplayItemsBinding
    private lateinit var db: UserDatabaseHelper
    private lateinit var adapter: MarketplaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMarketplaceDisplayItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = UserDatabaseHelper(this)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list whenever the user returns to this screen to reflect new postings or stock changes
        setupRecyclerView()
    }

    /**
     * Initializes the RecyclerView with a 2-column GridLayoutManager and the MarketplaceAdapter.
     */
    private fun setupRecyclerView() {
        val items = db.getAllMarketplaceItems()
        adapter = MarketplaceAdapter(items) { clickedItem ->
            // Navigate to the detail view for the clicked item
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("ITEM_DATA", clickedItem)
            startActivity(intent)
        }
        
        // Grid View with 2 columns provides a modern, visual-heavy browsing experience
        binding.rvMarketplaceItems.layoutManager = GridLayoutManager(this, 2)
        binding.rvMarketplaceItems.adapter = adapter
    }
}
