package com.example.unihub

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ItemMarketplaceBinding

/**
 * Adapter for displaying a list of Marketplace items in a RecyclerView.
 * 
 * It manages the visualization of each MarketplaceItem, including 
 * its title, price, category, and image.
 * 
 * @property items The list of marketplace items to display.
 * @property onItemClick A callback function triggered when an item is clicked.
 */
class MarketplaceAdapter(
    private val items: List<MarketplaceItem>,
    private val onItemClick: (MarketplaceItem) -> Unit
) : RecyclerView.Adapter<MarketplaceAdapter.MarketplaceViewHolder>() {

    /**
     * ViewHolder class for a Marketplace item.
     * Uses ViewBinding for efficient UI component access.
     */
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

    /**
     * Binds the data of a MarketplaceItem to its UI representation.
     * 
     * Handles dynamic loading of images and formats prices for display.
     * Sets up the item click listener to trigger the provided callback.
     */
    override fun onBindViewHolder(holder: MarketplaceViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemTitle.text = item.title
            tvItemPrice.text = "$${String.format("%.2f", item.price)}"
            tvItemCategory.text = item.category
            
            // Handle item image loading or provide a placeholder
            if (item.imageUri != null) {
                try {
                    ImageUtils.loadImage(root.context, Uri.parse(item.imageUri), ivItemImage)
                } catch (e: Exception) {
                    ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            
            // Delegate click events to the provided listener
            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
