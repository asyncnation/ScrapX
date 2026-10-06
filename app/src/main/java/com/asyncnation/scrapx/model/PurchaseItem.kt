package com.asyncnation.scrapx.model

data class PurchaseItem(

    val itemId: Int,

    val itemName: String,

    val pricePerKg: Double,

    var weight: Double = 0.0
) {
    val total: Double
        get() = pricePerKg * weight
}