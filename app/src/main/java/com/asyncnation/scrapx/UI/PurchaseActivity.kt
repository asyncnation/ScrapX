package com.asyncnation.scrapx.UI

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asyncnation.scrapx.adapter.ItemSearchAdapter
import com.asyncnation.scrapx.adapter.PurchaseItemAdapter
import com.asyncnation.scrapx.adapter.RecentItemAdapter
import com.asyncnation.scrapx.databinding.ActivityPurchaseBinding
import com.asyncnation.scrapx.model.PurchaseItem
import com.asyncnation.scrapx.room.entity.Item
import com.asyncnation.scrapx.room.entity.ItemPurchased
import kotlinx.coroutines.launch

class PurchaseActivity : BaseActivity() {

    private lateinit var binding: ActivityPurchaseBinding
    private lateinit var searchAdapter: ItemSearchAdapter
    private lateinit var recentAdapter: RecentItemAdapter
    private lateinit var purchaseAdapter: PurchaseItemAdapter

    private val selectedItems = mutableListOf<PurchaseItem>()

    private val recentItems = mutableListOf<Item>()

    private val prefs by lazy {
        getSharedPreferences(
            "recent_items",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchRecyclerView()
        setupRecentRecyclerView()
        setupSelectedRecyclerView()

        setupSearch()

        loadRecentItems()

        binding.btnSave.setOnClickListener {
            savePurchase()
        }
    }

    private fun setupSearchRecyclerView() {

        searchAdapter = ItemSearchAdapter(emptyList()) { item ->

            addItem(item)

            saveRecentItem(item)

            binding.etSearch.text?.clear()

            binding.rvSearchResults.visibility =
                android.view.View.GONE
        }

        binding.rvSearchResults.adapter = searchAdapter

        binding.rvSearchResults.layoutManager =
            LinearLayoutManager(this)
    }

    private fun setupRecentRecyclerView() {

        recentAdapter = RecentItemAdapter(emptyList()) { item ->

            addItem(item)
        }

        binding.rvRecentItems.adapter = recentAdapter

        binding.rvRecentItems.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
    }

    private fun setupSelectedRecyclerView() {

        purchaseAdapter = PurchaseItemAdapter(
            selectedItems
        ) {
            updateTotal()
        }

        binding.rvSelectedItems.adapter =
            purchaseAdapter

        binding.rvSelectedItems.layoutManager =
            LinearLayoutManager(this)
    }

    private fun setupSearch() {

        binding.etSearch.doAfterTextChanged { text ->

            val query = text
                .toString()
                .trim()

            if (query.isEmpty()) {

                binding.rvSearchResults.visibility =
                    android.view.View.GONE

                return@doAfterTextChanged
            }

            searchItems(query)
        }
    }

    private fun searchItems(query: String) {

        lifecycleScope.launch {

            val items =
                database.itemDao().searchItems(query)

            searchAdapter.updateItems(items)

            binding.rvSearchResults.visibility =
                if (items.isEmpty()) {
                    android.view.View.GONE
                } else {
                    android.view.View.VISIBLE
                }
        }
    }

    private fun addItem(item: Item) {

        if (selectedItems.any { it.itemId == item.id }) {

            Toast.makeText(
                this,
                "${item.name} is already added",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val purchaseItem = PurchaseItem(
            itemId = item.id,
            itemName = item.name,
            pricePerKg = item.currentPrice
        )

        selectedItems.add(purchaseItem)

        purchaseAdapter.notifyItemInserted(
            selectedItems.lastIndex
        )

        updateTotal()
    }

    private fun updateTotal() {

        val total =
            selectedItems.sumOf { it.total }

        binding.tvTotal.text =
            "Total: ₹%.2f".format(total)
    }

    // -----------------------------
    // Recent Items
    // -----------------------------

    private fun saveRecentItem(item: Item) {

        val currentIds =
            getRecentItemIds().toMutableList()

        // Remove existing occurrence
        currentIds.remove(item.id)

        // Add latest item at beginning
        currentIds.add(0, item.id)

        // Keep only latest 4
        val limitedIds =
            currentIds.take(4)

        prefs.edit()
            .putString(
                "ids",
                limitedIds.joinToString(",")
            )
            .apply()

        loadRecentItems()
    }

    private fun getRecentItemIds(): List<Int> {

        val value =
            prefs.getString("ids", "") ?: ""

        if (value.isEmpty()) {
            return emptyList()
        }

        return value
            .split(",")
            .mapNotNull { it.toIntOrNull() }
    }

    private fun loadRecentItems() {

        lifecycleScope.launch {

            val ids =
                getRecentItemIds()

            if (ids.isEmpty()) {

                binding.tvRecentTitle.visibility =
                    android.view.View.GONE

                binding.rvRecentItems.visibility =
                    android.view.View.GONE

                return@launch
            }

            val items = mutableListOf<Item>()

            for (id in ids) {

                val item =
                    database.itemDao().getItemById(id)

                if (item != null) {
                    items.add(item)
                }
            }

            recentItems.clear()
            recentItems.addAll(items)

            recentAdapter.updateItems(items)

            val visible =
                items.isNotEmpty()

            binding.tvRecentTitle.visibility =
                if (visible)
                    android.view.View.VISIBLE
                else
                    android.view.View.GONE

            binding.rvRecentItems.visibility =
                if (visible)
                    android.view.View.VISIBLE
                else
                    android.view.View.GONE
        }
    }

    // -----------------------------
    // Save Purchase
    // -----------------------------

    private fun savePurchase() {

        if (selectedItems.isEmpty()) {

            Toast.makeText(
                this,
                "Add at least one item",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (selectedItems.any { it.weight <= 0 }) {

            Toast.makeText(
                this,
                "Enter weight for all items",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        lifecycleScope.launch {

            val totalPrice =
                selectedItems.sumOf { it.total }

            val purchaseId =
                System.currentTimeMillis()

            val purchasedItems =
                selectedItems.map { item ->

                    ItemPurchased(
                        purchaseId = purchaseId,
                        itemId = item.itemId,
                        itemName = item.itemName,
                        weight = item.weight,
                        pricePerKg = item.pricePerKg,
                        itemTotal = item.total,
                        totalPrice = totalPrice
                    )
                }

            database.itemPurchasedDao()
                .insertAll(purchasedItems)

            Toast.makeText(
                this@PurchaseActivity,
                "Purchase saved",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}