package com.example.unihub

import java.io.Serializable

/**
 * A sealed class representing a polymorphic post item in the UniHub ecosystem.
 * 
 * This class allows different types of user-generated content (Marketplace items and Ride offers/requests)
 * to be treated as a single "Post" entity, which is useful for displaying a unified list of 
 * a user's activity in the "My Posts" section.
 */
sealed class PostItem : Serializable {
    /** Represents a marketplace listing. */
    data class Marketplace(val item: MarketplaceItem) : PostItem()
    
    /** Represents a ride offer or request. */
    data class RidePost(val ride: Ride) : PostItem()

    /**
     * The unique identifier for the post, delegated to the underlying model.
     */
    val id: Int
        get() = when (this) {
            is Marketplace -> item.id
            is RidePost -> ride.id
        }

    /**
     * A human-readable title for the post.
     */
    val title: String
        get() = when (this) {
            is Marketplace -> item.title
            is RidePost -> "${ride.type}: ${ride.fromLocation} to ${ride.toLocation}"
        }

    /**
     * A brief description or summary of the post content.
     */
    val description: String
        get() = when (this) {
            is Marketplace -> item.description
            is RidePost -> "${ride.date} at ${ride.time}. ${ride.note}"
        }
}