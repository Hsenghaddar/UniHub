package com.example.unihub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ItemMarketplaceBinding

class MarketplaceAdapter(private val items: List<MarketplaceItem>) :
    RecyclerView.Adapter<MarketplaceAdapter.MarketplaceViewHolder>() {

    class MarketplaceViewHolder(val binding: ItemMarketplaceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketplaceViewHolder {
        val binding = ItemMarketplaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MarketplaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarketplaceViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemTitle.text = item.title
            tvItemDescription.text = item.description
            tvCreatorName.text = "Added by: ${item.creatorName ?: "Unknown"}"
        }
    }

    override fun getItemCount(): Int = items.size
}
