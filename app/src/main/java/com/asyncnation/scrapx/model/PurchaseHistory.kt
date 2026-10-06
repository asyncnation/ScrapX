package com.asyncnation.scrapx.model

import com.asyncnation.scrapx.room.entity.ItemPurchased

data class PurchaseHistory(
    val purchaseId: Long,
    val items: List<ItemPurchased>,
    val total: Double
)