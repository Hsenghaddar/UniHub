package com.example.unihub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ItemInquiryMiniBinding
import com.example.unihub.databinding.ItemMyPostBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * MyPostsAdapter manages the display of the current user's marketplace and ride posts.
 * 
 * It supports:
 * - Polymorphic display of PostItem (Marketplace vs. Ride).
 * - Multi-selection mode for bulk actions.
 * - Dynamic notification badges for new sales, requests, or inquiries.
 * - Nested RecyclerView for showing mini-inquiries (unread messages about an item).
 */
class MyPostsAdapter(
    private var items: List<PostItem>,
    private val onEditClick: (PostItem) -> Unit,
    private val onSaleClick: (PostItem) -> Unit,
    private val onInquiryClick: (PostItem, String, String) -> Unit, // post, senderUid, senderName
    private val onSelectionChanged: () -> Unit,
    private val dbHelper: DatabaseHelper // Used to query for item-specific unread messages
) : RecyclerView.Adapter<MyPostsAdapter.MyPostViewHolder>() {

    private val selectedItems = mutableSetOf<String>()
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
        // Generate a unique identifier to distinguish between Marketplace (M) and Ride (R) items
        val uniqueId = when(post) {
            is PostItem.Marketplace -> "M_${post.item.id}"
            is PostItem.RidePost -> "R_${post.ride.id}"
        }

        holder.binding.apply {
            tvPostTitle.text = post.title
            tvPostDescription.text = post.description

            // Retrieve notification status from the specific post type
            val hasNotification = when(post) {
                is PostItem.Marketplace -> post.item.hasNotification
                is PostItem.RidePost -> post.ride.isHasNotification
            }

            // Inquiry logic for Marketplace: Show unread messages related to this item
            if (post is PostItem.Marketplace) {
                val inquirers = getInquirers(post.item.id, post.item.userUid)
                
                if (inquirers.isNotEmpty()) {
                    tvInquiryBadge.visibility = View.VISIBLE
                    rvInquiries.visibility = View.VISIBLE
                    rvInquiries.layoutManager = LinearLayoutManager(root.context)
                    rvInquiries.adapter = MiniInquiryAdapter(inquirers) { senderUid, senderName ->
                        onInquiryClick(post, senderUid, senderName)
                    }
                } else {
                    tvInquiryBadge.visibility = View.GONE
                    rvInquiries.visibility = View.GONE
                }

                // Show "New Sale" badge if the item has a pending sale notification
                if (hasNotification) {
                    tvNotificationBadge.visibility = View.VISIBLE
                    tvNotificationBadge.text = "New Sale"
                    tvNotificationBadge.setOnClickListener { onSaleClick(post) }
                } else {
                    tvNotificationBadge.visibility = View.GONE
                }
            } else {
                // Rides logic: Rides don't have nested inquiries, only request notifications
                tvInquiryBadge.visibility = View.GONE
                rvInquiries.visibility = View.GONE
                
                if (hasNotification) {
                    tvNotificationBadge.visibility = View.VISIBLE
                    tvNotificationBadge.text = "New Request"
                    tvNotificationBadge.setOnClickListener { onSaleClick(post) }
                } else {
                    tvNotificationBadge.visibility = View.GONE
                }
            }

            // Selection mode UI handling
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

            // Enter selection mode on long click
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

    /**
     * Queries the database for users who have sent unread messages about a specific item.
     */
    private fun getInquirers(itemId: Int, ownerUid: String): List<Inquirer> {
        val list = mutableListOf<Inquirer>()
        val db = dbHelper.readableDatabase
        // Query distinct senders with unread messages for this item context
        val query = """
            SELECT DISTINCT sender_uid, sender_name, MAX(timestamp) as last_time 
            FROM messages 
            WHERE item_id = ? AND receiver_uid = ? AND is_read = 0
            GROUP BY sender_uid 
            ORDER BY last_time DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(itemId.toString(), ownerUid))
        if (cursor.moveToFirst()) {
            do {
                list.add(Inquirer(
                    uid = cursor.getString(0),
                    name = cursor.getString(1),
                    lastTime = cursor.getString(2)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
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

    /**
     * Represents a user who has inquired about an item.
     */
    data class Inquirer(val uid: String, val name: String, val lastTime: String)

    /**
     * A smaller adapter for the nested RecyclerView that lists people who messaged about a post.
     */
    class MiniInquiryAdapter(
        private val inquirers: List<Inquirer>,
        private val onClick: (String, String) -> Unit
    ) : RecyclerView.Adapter<MiniInquiryAdapter.ViewHolder>() {
        
        class ViewHolder(val binding: ItemInquiryMiniBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemInquiryMiniBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val inquirer = inquirers[position]
            holder.binding.tvInquirySender.text = inquirer.name
            holder.binding.tvInquiryTime.text = formatTime(inquirer.lastTime)
            holder.binding.root.setOnClickListener { onClick(inquirer.uid, inquirer.name) }
        }

        override fun getItemCount() = inquirers.size

        private fun formatTime(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(timestamp)
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date!!)
            } catch (e: Exception) { "" }
        }
    }
}
