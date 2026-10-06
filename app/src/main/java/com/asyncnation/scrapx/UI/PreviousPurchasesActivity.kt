package com.asyncnation.scrapx.UI

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asyncnation.scrapx.adapter.PurchaseHistoryAdapter
import com.asyncnation.scrapx.databinding.ActivityPreviousPurchasesBinding
import com.asyncnation.scrapx.model.PurchaseHistory
import com.asyncnation.scrapx.store.InvoiceStore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PreviousPurchasesActivity : BaseActivity() {

    private lateinit var binding: ActivityPreviousPurchasesBinding
    private lateinit var adapter: PurchaseHistoryAdapter

    private var selectedDate: Calendar? = null
    private var allPurchases = listOf<PurchaseHistory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreviousPurchasesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupDateFilter()
        loadPurchases()
    }

    private fun setupRecyclerView() {

        adapter = PurchaseHistoryAdapter(emptyList()) { purchase ->

            openInvoice(purchase)

        }

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerView.adapter = adapter
    }

    private fun loadPurchases() {

        lifecycleScope.launch {

            val rows =
                database.itemPurchasedDao().getAllPurchases()

            val grouped =
                rows.groupBy { it.purchaseId }

            allPurchases =
                grouped.map { (purchaseId, items) ->

                    PurchaseHistory(
                        purchaseId = purchaseId,
                        items = items,
                        total = items.firstOrNull()?.totalPrice ?: 0.0
                    )
                }

            adapter.updateItems(allPurchases)
        }
    }

    private fun setupDateFilter() {

        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnClearFilter.setOnClickListener {
            clearDateFilter()
        }
    }

    private fun showDatePicker() {

        val calendar =
            selectedDate ?: Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                val date = Calendar.getInstance()

                date.set(
                    year,
                    month,
                    day
                )

                selectedDate = date

                filterPurchasesByDate(date)

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)

        ).show()
    }

    private fun filterPurchasesByDate(
        date: Calendar
    ) {

        val selectedYear =
            date.get(Calendar.YEAR)

        val selectedMonth =
            date.get(Calendar.MONTH)

        val selectedDay =
            date.get(Calendar.DAY_OF_MONTH)

        val filtered =
            allPurchases.filter { purchase ->

                val purchaseDate =
                    Calendar.getInstance()

                purchaseDate.timeInMillis =
                    purchase.purchaseId

                purchaseDate.get(Calendar.YEAR) ==
                        selectedYear &&

                        purchaseDate.get(Calendar.MONTH) ==
                        selectedMonth &&

                        purchaseDate.get(Calendar.DAY_OF_MONTH) ==
                        selectedDay
            }

        adapter.updateItems(filtered)

        val formattedDate =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(date.time)

        binding.tvSelectedDate.text =
            "Showing purchases for $formattedDate"

        binding.btnClearFilter.visibility =
            View.VISIBLE
    }

    private fun clearDateFilter() {

        selectedDate = null

        adapter.updateItems(allPurchases)

        binding.tvSelectedDate.text = ""

        binding.btnClearFilter.visibility =
            View.GONE
    }

    private fun openInvoice(
        purchase: PurchaseHistory
    ) {

        val intent =
            Intent(
                this,
                InvoiceActivity::class.java
            )

        InvoiceStore.purchase = purchase

        startActivity(intent)
    }
}