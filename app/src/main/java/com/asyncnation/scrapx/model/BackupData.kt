package com.asyncnation.scrapx.model

import com.asyncnation.scrapx.room.entity.Item
import com.asyncnation.scrapx.room.entity.ItemPurchased
import com.asyncnation.scrapx.room.entity.Purchase

data class BackupData(
    val schemaVersion: Int,
    val exportedAt: Long,
    val items: List<Item>,
    val purchases: List<Purchase>,
    val purchasedItems: List<ItemPurchased>
)