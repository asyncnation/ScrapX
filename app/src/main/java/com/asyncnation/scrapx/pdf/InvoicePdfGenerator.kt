package com.asyncnation.scrapx.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.asyncnation.scrapx.model.PurchaseHistory
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    private const val MARGIN = 40f

    fun formatDate(timestamp: Long): String {

        return SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(timestamp))
    }

    fun generate(
        context: Context,
        purchase: PurchaseHistory,
        customerName: String,
        customerPhone: String
    ): Result<Uri> {

        val pdfDocument = PdfDocument()

        return try {

            val pageInfo =
                PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH,
                    PAGE_HEIGHT,
                    1
                ).create()

            val page =
                pdfDocument.startPage(pageInfo)

            val canvas =
                page.canvas

            // =================================================
            // PAINTS
            // =================================================

            val normalPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.BLACK
                    textSize = 10f
                }

            val smallPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 9f
                }

            val boldPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.BLACK
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                }

            val titlePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                }

            val customerPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.BLACK
                    textSize = 11f
                }

            var y = 50f

            // =================================================
            // HEADER
            // =================================================

            canvas.drawText(
                "ScrapX",
                MARGIN,
                y,
                titlePaint
            )

            y += 10f

            canvas.drawText(
                "by asyncnation",
                100f,
                y,
                smallPaint
            )

            y += 18f

            canvas.drawText(
                "SCRAP PURCHASE INVOICE",
                MARGIN,
                y,
                smallPaint
            )

            // Invoice number
            canvas.drawText(
                "Invoice #${purchase.purchaseId}",
                390f,
                55f,
                boldPaint
            )

            // Invoice date
            canvas.drawText(
                formatDate(purchase.purchaseId),
                390f,
                72f,
                smallPaint
            )

            y += 25f

            // =================================================
            // DIVIDER
            // =================================================

            canvas.drawLine(
                MARGIN,
                y,
                PAGE_WIDTH - MARGIN,
                y,
                normalPaint
            )

            y += 25f

            // =================================================
            // CUSTOMER DETAILS
            // =================================================

            canvas.drawText(
                "CUSTOMER DETAILS",
                MARGIN,
                y,
                boldPaint
            )

            y += 20f

            canvas.drawText(
                "Name:",
                MARGIN,
                y,
                boldPaint
            )

            canvas.drawText(
                customerName,
                90f,
                y,
                customerPaint
            )

            // Phone is optional
            if (customerPhone.isNotBlank()) {

                y += 17f

                canvas.drawText(
                    "Phone:",
                    MARGIN,
                    y,
                    boldPaint
                )

                canvas.drawText(
                    customerPhone,
                    90f,
                    y,
                    customerPaint
                )
            }

            y += 25f

            // =================================================
            // TABLE HEADER
            // =================================================

            canvas.drawLine(
                MARGIN,
                y,
                PAGE_WIDTH - MARGIN,
                y,
                normalPaint
            )

            y += 25f

            canvas.drawText(
                "ITEM",
                MARGIN,
                y,
                boldPaint
            )

            canvas.drawText(
                "WEIGHT",
                300f,
                y,
                boldPaint
            )

            canvas.drawText(
                "RATE",
                390f,
                y,
                boldPaint
            )

            canvas.drawText(
                "AMOUNT",
                475f,
                y,
                boldPaint
            )

            y += 10f

            canvas.drawLine(
                MARGIN,
                y,
                PAGE_WIDTH - MARGIN,
                y,
                normalPaint
            )

            y += 25f

            // =================================================
            // ITEMS
            // =================================================

            purchase.items.forEach { item ->

                canvas.drawText(
                    item.itemName.take(30),
                    MARGIN,
                    y,
                    normalPaint
                )

                canvas.drawText(
                    "%.2f kg".format(item.weight),
                    300f,
                    y,
                    normalPaint
                )

                canvas.drawText(
                    "₹%.2f".format(item.pricePerKg),
                    390f,
                    y,
                    normalPaint
                )

                canvas.drawText(
                    "₹%.2f".format(item.itemTotal),
                    475f,
                    y,
                    normalPaint
                )

                y += 25f
            }

            // =================================================
            // TOTAL
            // =================================================

            y += 10f

            canvas.drawLine(
                MARGIN,
                y,
                PAGE_WIDTH - MARGIN,
                y,
                normalPaint
            )

            y += 30f

            canvas.drawText(
                "GRAND TOTAL",
                380f,
                y,
                boldPaint
            )

            canvas.drawText(
                "₹%.2f".format(purchase.total),
                475f,
                y,
                titlePaint
            )

            // =================================================
            // FOOTER
            // =================================================

            canvas.drawText(
                "Thank you for your business!",
                MARGIN,
                PAGE_HEIGHT - 50f,
                smallPaint
            )

            canvas.drawText(
                "Generated by ScrapX",
                430f,
                PAGE_HEIGHT - 50f,
                smallPaint
            )

            // Finish page
            pdfDocument.finishPage(page)

            // =================================================
            // FILE NAME
            // =================================================

            val fileName =
                "Invoice_${customerName}_${purchase.purchaseId}.pdf"

            // =================================================
            // SAVE PDF
            // =================================================

            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    saveUsingMediaStore(
                        context,
                        pdfDocument,
                        fileName
                    )

                } else {

                    saveUsingAppStorage(
                        context,
                        pdfDocument,
                        fileName
                    )
                }

            pdfDocument.close()

            if (uri != null) {

                Result.success(uri)

            } else {

                Result.failure(
                    Exception("Unable to create PDF")
                )
            }

        } catch (e: Exception) {

            pdfDocument.close()

            Result.failure(e)
        }
    }

    // =========================================================
    // ANDROID 10+
    // =========================================================

    private fun saveUsingMediaStore(
        context: Context,
        pdfDocument: PdfDocument,
        fileName: String
    ): Uri? {

        val values =
            ContentValues().apply {

                put(
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    fileName
                )

                put(
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    "application/pdf"
                )

                put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS +
                            "/ScrapX"
                )

                put(
                    MediaStore.Files.FileColumns.IS_PENDING,
                    1
                )
            }

        val resolver =
            context.contentResolver

        val collection =
            MediaStore.Files.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

        val uri =
            resolver.insert(
                collection,
                values
            ) ?: return null

        try {

            resolver.openOutputStream(uri).use { outputStream ->

                if (outputStream == null) {
                    throw Exception(
                        "Could not open output stream"
                    )
                }

                pdfDocument.writeTo(outputStream)
            }

            val completedValues =
                ContentValues().apply {

                    put(
                        MediaStore.Files.FileColumns.IS_PENDING,
                        0
                    )
                }

            resolver.update(
                uri,
                completedValues,
                null,
                null
            )

            return uri

        } catch (e: Exception) {

            resolver.delete(
                uri,
                null,
                null
            )

            throw e
        }
    }

    // =========================================================
    // ANDROID 9 AND BELOW
    // =========================================================

    private fun saveUsingAppStorage(
        context: Context,
        pdfDocument: PdfDocument,
        fileName: String
    ): Uri? {

        val directory =
            File(
                context.getExternalFilesDir(
                    Environment.DIRECTORY_DOCUMENTS
                ),
                "ScrapX"
            )

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file =
            File(
                directory,
                fileName
            )

        FileOutputStream(file).use { outputStream ->

            pdfDocument.writeTo(outputStream)
        }

        return Uri.fromFile(file)
    }
}