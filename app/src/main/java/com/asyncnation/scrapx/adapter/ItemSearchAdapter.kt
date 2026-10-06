package com.asyncnation.scrapx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asyncnation.scrapx.databinding.ItemSearchRowBinding
import com.asyncnation.scrapx.room.entity.Item

class ItemSearchAdapter(
    private var items: List<Item>,
    private val onItemSelected: (Item) -> Unit
) : RecyclerView.Adapter<ItemSearchAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemSearchRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {

            binding.tvName.text = item.name
            binding.tvPrice.text =
                "₹%.2f/kg".format(item.currentPrice)

            binding.root.setOnClickListener {
                onItemSelected(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemSearchRowBinding.inflate(
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

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}