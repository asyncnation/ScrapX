package com.asyncnation.scrapx

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.asyncnation.scrapx.model.BackupData
import com.asyncnation.scrapx.room.AppDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BackupManager {

    private const val BACKUP_VERSION = 1

    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .create()

    suspend fun exportDatabase(
        context: Context,
        uri: Uri
    ): Result<Unit> {

        return withContext(Dispatchers.IO) {

            try {

                val database =
                    AppDatabase.getDatabase(context)

                // ---------------------------------------------
                // READ ALL DATA
                // ---------------------------------------------

                val items =
                    database.itemDao()
                        .getAllItems()

                val purchases =
                    database.purchaseDao()
                        .getAllPurchases()

                val purchasedItems =
                    database.itemPurchasedDao()
                        .getAllPurchases()

                // ---------------------------------------------
                // CREATE BACKUP OBJECT
                // ---------------------------------------------

                val backupData =
                    BackupData(
                        schemaVersion = BACKUP_VERSION,
                        exportedAt = System.currentTimeMillis(),
                        items = items,
                        purchases = purchases,
                        purchasedItems = purchasedItems
                    )

                // ---------------------------------------------
                // CONVERT TO JSON
                // ---------------------------------------------

                val json =
                    gson.toJson(backupData)

                // ---------------------------------------------
                // WRITE FILE
                // ---------------------------------------------

                context.contentResolver
                    .openOutputStream(uri)
                    .use { outputStream ->

                        if (outputStream == null) {

                            throw Exception(
                                "Could not open backup file"
                            )
                        }

                        outputStream.write(
                            json.toByteArray(Charsets.UTF_8)
                        )
                    }

                Result.success(Unit)

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }

    suspend fun importDatabase(
        context: Context,
        uri: Uri
    ): Result<Unit> {

        return withContext(Dispatchers.IO) {

            try {

                // ---------------------------------------------
                // READ JSON
                // ---------------------------------------------

                val json =
                    context.contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.use {
                            it.readText()
                        }
                        ?: throw Exception(
                            "Could not read backup file"
                        )

                // ---------------------------------------------
                // PARSE JSON
                // ---------------------------------------------

                val backupData =
                    gson.fromJson(
                        json,
                        BackupData::class.java
                    )
                        ?: throw Exception(
                            "Invalid backup file"
                        )

                // ---------------------------------------------
                // VALIDATE BACKUP
                // ---------------------------------------------

                if (
                    backupData.schemaVersion <= 0
                ) {
                    throw Exception(
                        "Unsupported backup version"
                    )
                }

                val database =
                    AppDatabase.getDatabase(context)

                // ---------------------------------------------
                // RESTORE EVERYTHING IN ONE TRANSACTION
                // ---------------------------------------------

                database.withTransaction {

                    // Delete child data first
                    database.itemPurchasedDao()
                        .deleteAll()

                    // Delete purchases
                    database.purchaseDao()
                        .deleteAll()

                    // Delete master items
                    database.itemDao()
                        .deleteAll()

                    // -----------------------------------------
                    // INSERT BACKUP DATA
                    // -----------------------------------------

                    if (backupData.items.isNotEmpty()) {

                        database.itemDao()
                            .insertAll(
                                backupData.items
                            )
                    }

                    if (backupData.purchases.isNotEmpty()) {

                        database.purchaseDao()
                            .insertAll(
                                backupData.purchases
                            )
                    }

                    if (
                        backupData.purchasedItems.isNotEmpty()
                    ) {

                        database.itemPurchasedDao()
                            .insertAll(
                                backupData.purchasedItems
                            )
                    }
                }

                Result.success(Unit)

            } catch (e: Exception) {

                Result.failure(e)
            }
        }
    }
}