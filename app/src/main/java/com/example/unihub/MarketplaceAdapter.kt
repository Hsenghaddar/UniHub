package com.example.unihub

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ItemMarketplaceBinding

class MarketplaceAdapter(
    private val items: List<MarketplaceItem>,
    private val onItemClick: (MarketplaceItem) -> Unit
) : RecyclerView.Adapter<MarketplaceAdapter.MarketplaceViewHolder>() {

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
            tvItemPrice.text = "$${String.format("%.2f", item.price)}"
            tvItemCategory.text = item.category
            
            if (item.imageUri != null) {
                try {
                    ImageUtils.loadImage(root.context, Uri.parse(item.imageUri), ivItemImage)
                } catch (e: Exception) {
                    ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            
            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
