package com.asyncnation.scrapx.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val currentPrice: Double,
    )
