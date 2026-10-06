package com.asyncnation.scrapx.room.dao

import com.asyncnation.scrapx.room.entity.ItemPurchased


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemPurchasedDao {

    @Insert
    suspend fun insert(item: ItemPurchased)

    @Insert
    suspend fun insertAll(items: List<ItemPurchased>)

    @Query("""
        SELECT * FROM item_purchased
        ORDER BY purchaseId DESC
    """)
    suspend fun getAllPurchases(): List<ItemPurchased>

    @Query("""
        SELECT * FROM item_purchased
        WHERE purchaseId = :purchaseId
    """)
    suspend fun getPurchase(purchaseId: Long): List<ItemPurchased>

    @Query("DELETE FROM item_purchased")
    suspend fun deleteAll()
}