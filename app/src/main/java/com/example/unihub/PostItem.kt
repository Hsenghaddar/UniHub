package com.example.unihub

import java.io.Serializable

sealed class PostItem : Serializable {
    data class Marketplace(val item: MarketplaceItem) : PostItem()
    data class RidePost(val ride: Ride) : PostItem()

    val id: Int
        get() = when (this) {
            is Marketplace -> item.id
            is RidePost -> ride.id
        }

    val title: String
        get() = when (this) {
            is Marketplace -> item.title
            is RidePost -> "${ride.type}: ${ride.fromLocation} to ${ride.toLocation}"
        }

    val description: String
        get() = when (this) {
            is Marketplace -> item.description
            is RidePost -> "${ride.date} at ${ride.time}. ${ride.note}"
        }
}