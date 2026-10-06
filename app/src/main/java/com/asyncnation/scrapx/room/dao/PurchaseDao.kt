package com.asyncnation.scrapx.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.asyncnation.scrapx.room.entity.Purchase

@Dao
interface PurchaseDao {

    @Insert
    suspend fun insert(purchase: Purchase)

    @Insert
    suspend fun insertAll(purchases: List<Purchase>)

    @Query("SELECT * FROM purchases ORDER BY purchaseId DESC")
    suspend fun getAllPurchases(): List<Purchase>

    @Query("SELECT * FROM purchases WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseById(purchaseId: Long): Purchase?

    @Query("DELETE FROM purchases")
    suspend fun deleteAll()
}