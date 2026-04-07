package com.example.unihub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ItemMyPostBinding

class MyPostsAdapter(
    private var items: List<MarketplaceItem>,
    private val onEditClick: (MarketplaceItem) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<MyPostsAdapter.MyPostViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()
    private var isSelectionMode = false

    class MyPostViewHolder(val binding: ItemMyPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostViewHolder {
        val binding = ItemMyPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyPostViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvPostTitle.text = item.title
            tvPostDescription.text = item.description

            cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            cbSelect.isChecked = selectedItems.contains(item.id)

            btnEdit.visibility = if (isSelectionMode) View.GONE else View.VISIBLE

            btnEdit.setOnClickListener { onEditClick(item) }

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(item.id)
                } else {
                    selectedItems.remove(item.id)
                }
                onSelectionChanged()
            }

            root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    selectedItems.add(item.id)
                    notifyDataSetChanged()
                    onSelectionChanged()
                }
                true
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MarketplaceItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getSelectedIds(): List<Int> = selectedItems.toList()

    fun isSelectionMode(): Boolean = isSelectionMode

    fun enterSelectionMode() {
        isSelectionMode = true
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }
}
