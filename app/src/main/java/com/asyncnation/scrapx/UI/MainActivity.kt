package com.asyncnation.scrapx.UI

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asyncnation.scrapx.R
import com.asyncnation.scrapx.adapter.ItemAdapter
import com.asyncnation.scrapx.databinding.ActivityMainBinding
import com.asyncnation.scrapx.room.entity.Item
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAdd.setOnClickListener {
            showAddItemDialog()
        }
        setupRecyclerView()
        loadItems()
    }

    private fun setupRecyclerView() {

        adapter = ItemAdapter(
            items = emptyList(),

            onEditClick = { item ->
                editItem(item)
            },

            onDeleteClick = { item ->
                deleteItem(item)
            }
        )

        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager =
            LinearLayoutManager(this)
    }

    private fun loadItems() {

        lifecycleScope.launch {

            val items = database.itemDao().getAllItems()

            adapter.updateItems(items)
        }
    }

    private fun editItem(item: Item) {

        val input = EditText(this)

        input.inputType =
            InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

        input.setText(item.currentPrice.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Price")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->

                val newPrice = input.text.toString().toDoubleOrNull()

                if (newPrice != null) {

                    lifecycleScope.launch {

                        database.itemDao().updatePrice(
                            item.id,
                            newPrice
                        )

                        loadItems()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem(item: Item) {

        lifecycleScope.launch {

            database.itemDao().delete(item)

            loadItems()
        }
    }

    private fun showAddItemDialog() {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_add_item,
            null
        )

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Item")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val name = etName.text.toString().trim()
                val price = etPrice.text.toString().toDoubleOrNull()

                if (name.isEmpty()) {
                    etName.error = "Enter item name"
                    return@setOnClickListener
                }

                if (price == null) {
                    etPrice.error = "Enter valid price"
                    return@setOnClickListener
                }

                lifecycleScope.launch {

                    val item = Item(
                        name = name,
                        currentPrice     = price
                    )

                    database.itemDao().insert(item)

                    loadItems()
                }

                dialog.dismiss()
            }
        }

        dialog.show()
    }
}