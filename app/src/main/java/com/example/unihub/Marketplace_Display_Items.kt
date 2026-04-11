package com.example.unihub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.unihub.databinding.ActivityMarketplaceDisplayItemsBinding

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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val items = db.getAllMarketplaceItems()
        adapter = MarketplaceAdapter(items) { clickedItem ->
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("ITEM_DATA", clickedItem)
            startActivity(intent)
        }
        // Grid View with 2 columns like Facebook Marketplace
        binding.rvMarketplaceItems.layoutManager = GridLayoutManager(this, 2)
        binding.rvMarketplaceItems.adapter = adapter
    }
}
