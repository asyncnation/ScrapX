package com.asyncnation.scrapx.room

import com.asyncnation.scrapx.room.dao.ItemDao
import com.asyncnation.scrapx.room.dao.ItemPurchasedDao
import com.asyncnation.scrapx.room.dao.PurchaseDao
import com.asyncnation.scrapx.room.entity.Item
import com.asyncnation.scrapx.room.entity.ItemPurchased
import com.asyncnation.scrapx.room.entity.Purchase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Item::class,
        ItemPurchased::class,
        Purchase::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    abstract fun itemPurchasedDao(): ItemPurchasedDao

    abstract fun purchaseDao(): PurchaseDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /*
         * Version 2 -> Version 3
         *
         * Creates the new purchases table.
         *
         * Existing purchases are reconstructed from
         * item_purchased because purchaseId already exists there.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS purchases (
                        purchaseId INTEGER NOT NULL,
                        customerName TEXT NOT NULL,
                        customerPhone TEXT NOT NULL,
                        purchaseDate INTEGER NOT NULL,
                        totalPrice REAL NOT NULL,
                        PRIMARY KEY(purchaseId)
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT OR IGNORE INTO purchases (
                        purchaseId,
                        customerName,
                        customerPhone,
                        purchaseDate,
                        totalPrice
                    )
                    SELECT
                        purchaseId,
                        '',
                        '',
                        purchaseId,
                        totalPrice
                    FROM item_purchased
                    GROUP BY purchaseId
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scrap_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}