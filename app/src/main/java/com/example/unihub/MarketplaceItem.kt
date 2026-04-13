package com.example.unihub

import java.io.Serializable

/**
 * Data class representing an item listed in the marketplace.
 * 
 * This model captures all details about a product for sale, including its classification,
 * pricing, and stock availability. It also tracks the owner (creator) and contains
 * metadata for UI notifications (e.g., if there are unread messages regarding this item).
 *
 * @property id Unique identifier for the item.
 * @property title Name of the product.
 * @property description Detailed description of the product.
 * @property category The category it belongs to (e.g., Electronics, Books).
 * @property price Selling price of the item.
 * @property stock Number of units available.
 * @property userUid Firebase UID of the seller.
 * @property creatorName Display name of the seller.
 * @property imageUri String Uri for the product image.
 * @property hasNotification Flag indicating if there's an unread activity related to this item.
 */
data class MarketplaceItem(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val userUid: String,
    val creatorName: String? = null,
    val imageUri: String? = null,
    val hasNotification: Boolean = false
) : Serializable
