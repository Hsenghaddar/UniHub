package com.example.unihub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ItemMyPostBinding

class MyPostsAdapter(
    private var items: List<PostItem>,
    private val onEditClick: (PostItem) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<MyPostsAdapter.MyPostViewHolder>() {

    private val selectedItems = mutableSetOf<String>() // Use String to differentiate types, e.g., "M_1", "R_1"
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
        val post = items[position]
        val uniqueId = when(post) {
            is PostItem.Marketplace -> "M_${post.item.id}"
            is PostItem.RidePost -> "R_${post.ride.id}"
        }

        holder.binding.apply {
            tvPostTitle.text = post.title
            tvPostDescription.text = post.description

            cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            cbSelect.isChecked = selectedItems.contains(uniqueId)

            btnEdit.visibility = if (isSelectionMode) View.GONE else View.VISIBLE

            btnEdit.setOnClickListener { onEditClick(post) }

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(uniqueId)
                } else {
                    selectedItems.remove(uniqueId)
                }
                onSelectionChanged()
            }

            root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    selectedItems.add(uniqueId)
                    notifyDataSetChanged()
                    onSelectionChanged()
                }
                true
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<PostItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getSelectedUniqueIds(): List<String> = selectedItems.toList()

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
