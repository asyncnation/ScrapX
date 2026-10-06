package com.asyncnation.scrapx.room.dao

import com.asyncnation.scrapx.room.entity.Item

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: Item)

    @Insert
    suspend fun insertAll(items: List<Item>)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<Item>

    @Query("""
        SELECT * FROM items
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name
    """)
    suspend fun searchItems(query: String): List<Item>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): Item?

    @Query("UPDATE items SET currentPrice = :price WHERE id = :id")
    suspend fun updatePrice(id: Int, price: Double): Int

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}