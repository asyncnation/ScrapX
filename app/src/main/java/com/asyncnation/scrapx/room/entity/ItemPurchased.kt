package com.asyncnation.scrapx.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_purchased")
data class ItemPurchased(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val purchaseId: Long,
    val itemId: Int,
    val itemName: String,
    val weight: Double,
    val pricePerKg: Double,
    val itemTotal: Double,
    val totalPrice: Double
)