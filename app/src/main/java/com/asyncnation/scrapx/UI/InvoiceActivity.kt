package com.asyncnation.scrapx.UI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asyncnation.scrapx.databinding.ActivityInvoiceBinding
import com.asyncnation.scrapx.databinding.InvoiceItemRowBinding
import com.asyncnation.scrapx.model.PurchaseHistory
import com.asyncnation.scrapx.pdf.InvoicePdfGenerator
import com.asyncnation.scrapx.store.InvoiceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InvoiceActivity : BaseActivity() {

    private lateinit var binding: ActivityInvoiceBinding

    private var purchase: PurchaseHistory? = null
    private var generatedPdfUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        purchase = InvoiceStore.purchase

        if (purchase == null) {
            Toast.makeText(
                this,
                "Purchase not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        setupInvoiceUI()

        binding.btnSavePdf.setOnClickListener {
            savePdf()
        }

        binding.btnShare.setOnClickListener {
            shareInvoice()
        }
    }

    private fun setupInvoiceUI() {

        val purchase = purchase ?: return

        binding.tvInvoiceNumber.text =
            "Invoice #${purchase.purchaseId}"

        binding.tvDate.text =
            InvoicePdfGenerator.formatDate(
                purchase.purchaseId
            )

        binding.itemContainer.removeAllViews()

        purchase.items.forEach { item ->

            val itemBinding =
                InvoiceItemRowBinding.inflate(
                    layoutInflater,
                    binding.itemContainer,
                    false
                )

            itemBinding.tvItemName.text =
                item.itemName

            itemBinding.tvWeight.text =
                "%.2f kg".format(item.weight)

            itemBinding.tvRate.text =
                "₹%.2f".format(item.pricePerKg)

            itemBinding.tvAmount.text =
                "₹%.2f".format(item.itemTotal)

            binding.itemContainer.addView(
                itemBinding.root
            )
        }

        binding.tvGrandTotal.text =
            "₹%.2f".format(purchase.total)
    }

    private fun validateCustomerDetails(): Boolean {

        val customerName =
            binding.etCustomerName.text
                ?.toString()
                ?.trim()

        if (customerName.isNullOrEmpty()) {

            binding.customerNameLayout.error =
                "Customer name is required"

            binding.etCustomerName.requestFocus()

            return false
        }

        binding.customerNameLayout.error = null

        return true
    }

    private fun getCustomerName(): String {

        return binding.etCustomerName.text
            ?.toString()
            ?.trim()
            ?: ""
    }

    private fun getCustomerPhone(): String {

        return binding.etCustomerPhone.text
            ?.toString()
            ?.trim()
            ?: ""
    }

    private fun savePdf() {

        if (!validateCustomerDetails()) {
            return
        }

        val purchase = purchase ?: return

        val customerName =
            getCustomerName()

        val customerPhone =
            getCustomerPhone()

        lifecycleScope.launch {

            binding.btnSavePdf.isEnabled = false

            val result = withContext(Dispatchers.IO) {

                InvoicePdfGenerator.generate(
                    this@InvoiceActivity,
                    purchase,
                    customerName,
                    customerPhone
                )
            }

            binding.btnSavePdf.isEnabled = true

            if (result.isSuccess) {

                generatedPdfUri =
                    result.getOrNull()

                Toast.makeText(
                    this@InvoiceActivity,
                    "Invoice saved successfully",
                    Toast.LENGTH_LONG
                ).show()

            } else {

                Toast.makeText(
                    this@InvoiceActivity,
                    "Failed to generate invoice",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun shareInvoice() {

        if (!validateCustomerDetails()) {
            return
        }

        val purchase = purchase ?: return

        val existingUri = generatedPdfUri

        if (existingUri != null) {
            sharePdf(existingUri)
            return
        }

        val customerName =
            getCustomerName()

        val customerPhone =
            getCustomerPhone()

        lifecycleScope.launch {

            binding.btnShare.isEnabled = false

            val result = withContext(Dispatchers.IO) {

                InvoicePdfGenerator.generate(
                    this@InvoiceActivity,
                    purchase,
                    customerName,
                    customerPhone
                )
            }

            binding.btnShare.isEnabled = true

            if (result.isSuccess) {

                generatedPdfUri =
                    result.getOrNull()

                generatedPdfUri?.let {
                    sharePdf(it)
                }

            } else {

                Toast.makeText(
                    this@InvoiceActivity,
                    "Failed to generate invoice",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun sharePdf(uri: Uri) {

        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {

                type = "application/pdf"

                putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        startActivity(
            Intent.createChooser(
                shareIntent,
                "Share Invoice"
            )
        )
    }
}