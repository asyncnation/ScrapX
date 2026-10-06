package com.asyncnation.scrapx.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey
    val purchaseId: Long,
    val customerName: String,
    val customerPhone: String,
    val purchaseDate: Long,
    val totalPrice: Double
)