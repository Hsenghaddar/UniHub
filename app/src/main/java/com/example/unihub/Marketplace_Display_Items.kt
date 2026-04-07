package com.example.unihub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

        setupRecyclerView()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val items = db.getAllMarketplaceItems()
        adapter = MarketplaceAdapter(items)
        binding.rvMarketplaceItems.layoutManager = LinearLayoutManager(this)
        binding.rvMarketplaceItems.adapter = adapter
    }
}