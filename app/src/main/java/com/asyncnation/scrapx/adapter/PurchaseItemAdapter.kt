package com.asyncnation.scrapx.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asyncnation.scrapx.databinding.PurchaseItemRowBinding
import com.asyncnation.scrapx.model.PurchaseItem

class PurchaseItemAdapter(
    private val items: MutableList<PurchaseItem>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<PurchaseItemAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: PurchaseItemRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var weightWatcher: TextWatcher? = null

        fun bind(item: PurchaseItem) {

            binding.tvItemName.text = item.itemName

            binding.tvPricePerKg.text =
                "₹%.2f/kg".format(item.pricePerKg)

            // Remove the old watcher before setting text
            weightWatcher?.let {
                binding.etWeight.removeTextChangedListener(it)
            }

            // Set current weight
            binding.etWeight.setText(
                if (item.weight == 0.0) {
                    ""
                } else {
                    item.weight.toString()
                }
            )

            // Show current item total
            updateItemTotal(item)

            // Create a new watcher
            weightWatcher = object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val weight =
                        s?.toString()?.toDoubleOrNull() ?: 0.0

                    item.weight = weight

                    // Update item total immediately
                    updateItemTotal(item)

                    // Update grand total immediately
                    onChanged()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }

            binding.etWeight.addTextChangedListener(weightWatcher)

            binding.btnRemove.setOnClickListener {

                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {

                    items.removeAt(position)

                    notifyItemRemoved(position)

                    onChanged()
                }
            }
        }

        private fun updateItemTotal(item: PurchaseItem) {

            binding.tvItemTotal.text =
                "Total: ₹%.2f".format(item.total)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            PurchaseItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int =
        items.size
}