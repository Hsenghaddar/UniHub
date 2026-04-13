package com.example.unihub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unihub.databinding.ActivityContactsBinding
import com.example.unihub.databinding.ItemContactBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity for displaying a list of recent chat contacts.
 *
 * This activity queries the local database to find all users that the current user
 * has exchanged messages with. It displays them in a list, showing their name,
 * last message, and profile picture if available.
 */
class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var db: DatabaseHelper
    private lateinit var userDb: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Return to previous screen
        binding.btnBack.setOnClickListener { finish() }

        db = DatabaseHelper(this)
        userDb = UserDatabaseHelper(this)
        setupRecyclerView()
    }

    /**
     * Initializes the RecyclerView with the list of chat contacts.
     */
    private fun setupRecyclerView() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val contacts = getChatContacts(currentUserUid)

        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = ContactsAdapter(contacts) { contact ->
            // Open the chat with the selected contact
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("RECEIVER_UID", contact.uid)
                putExtra("RECEIVER_NAME", contact.name)
            }
            startActivity(intent)
        }
    }

    /**
     * Queries the local SQLite database to retrieve distinct chat partners.
     * It performs a UNION of sent and received messages to find all unique UIDs.
     *
     * @param myUid The current user's Firebase UID.
     * @return A list of Contact objects representing unique chat partners.
     */
    private fun getChatContacts(myUid: String): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val sqliteDb = db.readableDatabase
        
        // Complex query to find distinct people I've chatted with, along with the latest message and timestamp
        val query = """
            SELECT uid, name, MAX(timestamp) as last_time, text 
            FROM (
                SELECT receiver_uid as uid, 'Me' as name, timestamp, message_text as text FROM messages WHERE sender_uid = ?
                UNION
                SELECT sender_uid as uid, sender_name as name, timestamp, message_text as text FROM messages WHERE receiver_uid = ?
            ) 
            GROUP BY uid 
            ORDER BY last_time DESC
        """.trimIndent()

        val cursor = sqliteDb.rawQuery(query, arrayOf(myUid, myUid))
        
        if (cursor.moveToFirst()) {
            do {
                val uid = cursor.getString(0)
                var name = cursor.getString(1)
                
                // If the name in the subquery was "Me", we need to fetch the actual name of the recipient
                if (name == "Me") {
                    name = getRecipientName(uid)
                }

                // Fetch image URI from UserDatabaseHelper for the profile picture
                val user = userDb.getUserByFirebaseUid(uid)
                val imageUri = user?.imageUri

                contacts.add(Contact(
                    uid = uid,
                    name = name,
                    lastMessage = cursor.getString(3),
                    imageUri = imageUri
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return contacts
    }

    /**
     * Helper to find the sender name for a given UID from the messages table.
     */
    private fun getRecipientName(uid: String): String {
        val sqliteDb = db.readableDatabase
        val cursor = sqliteDb.rawQuery("SELECT sender_name FROM messages WHERE sender_uid = ? LIMIT 1", arrayOf(uid))
        var name = "User"
        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }
        cursor.close()
        return name
    }

    /**
     * Simple data class to represent a contact in the list.
     */
    data class Contact(val uid: String, val name: String, val lastMessage: String, val imageUri: String? = null)

    /**
     * Internal adapter class for the contacts list.
     */
    class ContactsAdapter(
        private val contacts: List<Contact>,
        private val onClick: (Contact) -> Unit
    ) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val contact = contacts[position]
            holder.binding.tvContactName.text = contact.name
            holder.binding.tvLastMessage.text = contact.lastMessage
            
            // Efficiently load the contact's avatar using ImageUtils
            if (contact.imageUri != null) {
                ImageUtils.loadImage(holder.binding.root.context, Uri.parse(contact.imageUri), holder.binding.ivContactAvatar)
            } else {
                holder.binding.ivContactAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            holder.binding.root.setOnClickListener { onClick(contact) }
        }

        override fun getItemCount() = contacts.size
    }
}
