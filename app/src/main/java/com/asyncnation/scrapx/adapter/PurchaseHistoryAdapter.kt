package com.asyncnation.scrapx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asyncnation.scrapx.databinding.PurchaseHistoryItemBinding
import com.asyncnation.scrapx.databinding.PurchaseHistoryRowBinding
import com.asyncnation.scrapx.model.PurchaseHistory
import com.asyncnation.scrapx.room.entity.ItemPurchased
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PurchaseHistoryAdapter(
    private var purchases: List<PurchaseHistory>,
    private val onInvoiceClick: (PurchaseHistory) -> Unit
) : RecyclerView.Adapter<PurchaseHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: PurchaseHistoryRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(purchase: PurchaseHistory) {

            val date = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(Date(purchase.purchaseId))

            binding.tvDate.text = date

            binding.tvItemCount.text =
                "${purchase.items.size} Items"

            binding.tvTotal.text =
                "₹%.2f".format(purchase.total)

            // Remove old rows before adding new ones
            binding.itemContainer.removeAllViews()

            purchase.items.forEach { item ->

                val itemBinding =
                    PurchaseHistoryItemBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.itemContainer,
                        false
                    )

                itemBinding.tvItemName.text = item.itemName

                itemBinding.tvWeight.text =
                    "%.2f kg".format(item.weight)

                itemBinding.tvAmount.text =
                    "₹%.2f".format(item.itemTotal)

                binding.itemContainer.addView(
                    itemBinding.root
                )
            }

            binding.btnInvoice.setOnClickListener {
                onInvoiceClick(purchase)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            PurchaseHistoryRowBinding.inflate(
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
        holder.bind(purchases[position])
    }

    override fun getItemCount(): Int =
        purchases.size

    fun updateItems(newItems: List<PurchaseHistory>) {
        purchases = newItems
        notifyDataSetChanged()
    }
}