package com.example.unihub

data class MarketplaceItem(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val userUid: String,
    val creatorName: String? = null
)
