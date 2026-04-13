package com.example.unihub;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.unihub.databinding.ActivityChatBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for real-time-like local chat between users.
 *
 * This activity facilitates messaging between two users. It supports standard text messages
 * and specialized marketplace interactions, such as sending item links with custom prices
 * and handling direct purchases within the chat interface.
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private DatabaseHelper dbHelper;
    private UserDatabaseHelper userDbHelper;
    private SessionManager sessionManager;
    private ChatAdapter adapter;
    private String currentUserUid;
    private String currentUserName;
    private String receiverUid;
    private String receiverName;
    private int itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        userDbHelper = new UserDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Identify current user
        currentUserUid = sessionManager.getSavedFirebaseUid();
        LocalUser user = userDbHelper.getUserByFirebaseUid(currentUserUid);
        if (user != null) {
            currentUserName = user.getFullName();
        }

        // Get recipient info from intent
        receiverUid = getIntent().getStringExtra("RECEIVER_UID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");
        itemId = getIntent().getIntExtra("ITEM_ID", -1);

        binding.tvHeaderTitle.setText(receiverName != null ? receiverName : "Chat");
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Link an item from the marketplace to the chat
        binding.btnSendLink.setOnClickListener(v -> showItemSelectionDialog());

        setupItemPreview();
        setupRecyclerView();
        loadMessages();
        
        // Mark messages as read for this specific conversation context
        if (itemId != -1) {
            dbHelper.markMessagesAsRead(currentUserUid, receiverUid, itemId);
        }

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    /**
     * Shows a dialog allowing the user to select one of their marketplace items to share in the chat.
     */
    private void showItemSelectionDialog() {
        List<MarketplaceItem> myItems = userDbHelper.getMarketplaceItemsByUser(currentUserUid);
        if (myItems.isEmpty()) {
            Toast.makeText(this, "You have no items in the marketplace", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] itemTitles = new String[myItems.size()];
        for (int i = 0; i < myItems.size(); i++) {
            itemTitles[i] = myItems.get(i).getTitle() + (myItems.get(i).getStock() <= 0 ? " (Out of Stock)" : "");
        }

        new AlertDialog.Builder(this)
            .setTitle("Select Item")
            .setItems(itemTitles, (dialog, which) -> {
                MarketplaceItem selectedItem = myItems.get(which);
                showPriceInputDialog(selectedItem);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Displays a dialog to set a custom price for the item being shared in chat.
     *
     * @param item The marketplace item to be shared.
     */
    private void showPriceInputDialog(MarketplaceItem item) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(item.getPrice()));
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
            .setTitle("Set Price for Inquiry")
            .setMessage("Proposed price for " + item.getTitle() + ":")
            .setView(input)
            .setPositiveButton("Send", (dialog, which) -> {
                String priceStr = input.getText().toString();
                if (!TextUtils.isEmpty(priceStr)) {
                    double price = Double.parseDouble(priceStr);
                    sendPurchaseInquiry(item, price);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Inserts a purchase inquiry message into the database.
     */
    private void sendPurchaseInquiry(MarketplaceItem item, double price) {
        String inquiryText = "Proposed Purchase: " + item.getTitle() + " for $" + String.format("%.2f", price);
        if (dbHelper.insertMessage(currentUserUid, currentUserName, receiverUid, inquiryText, item.getId(), 1, price)) {
            loadMessages();
        }
    }

    /**
     * Sets up a small preview bar if the chat was started from a specific marketplace item.
     */
    private void setupItemPreview() {
        if (itemId == -1) {
            binding.cardItemPreview.setVisibility(View.GONE);
            return;
        }

        MarketplaceItem item = userDbHelper.getMarketplaceItemById(itemId);
        if (item != null) {
            binding.cardItemPreview.setVisibility(View.VISIBLE);
            binding.tvItemPreviewTitle.setText(item.getTitle());
            binding.tvItemPreviewPrice.setText(String.format("$%.2f", item.getPrice()));
            if (item.getImageUri() != null) {
                ImageUtils.INSTANCE.loadImage(this, Uri.parse(item.getImageUri()), binding.ivItemPreview);
            }
        } else {
            binding.cardItemPreview.setVisibility(View.GONE);
        }
    }

    /**
     * Configures the message list RecyclerView.
     */
    private void setupRecyclerView() {
        adapter = new ChatAdapter(new ArrayList<>(), currentUserUid);
        adapter.setOnPurchaseClickListener(msg -> handlePurchase(msg));
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Messages start from bottom
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    /**
     * Processes a purchase when the user clicks the "Purchase" button in a chat message.
     * Updates stock and records a sale in the database.
     *
     * @param msg The message containing the purchase link.
     */
    private void handlePurchase(Message msg) {
        if (msg.getType() != 1) {
            Toast.makeText(this, "This link has already been used.", Toast.LENGTH_SHORT).show();
            return;
        }

        MarketplaceItem item = userDbHelper.getMarketplaceItemById(msg.getItemId());
        if (item == null) {
            Toast.makeText(this, "Item no longer exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mark this message as completed to disable the button
        dbHelper.updateMessageType(msg.getId(), 2);

        // Decrement stock
        int newStock = Math.max(0, item.getStock() - 1);
        userDbHelper.updateMarketplaceStock(item.getId(), newStock);
        
        // Record the sale transaction
        double finalPrice = msg.getPrice() > 0 ? msg.getPrice() : item.getPrice();
        userDbHelper.recordSale(item.getId(), currentUserUid, 1, finalPrice);
        
        // Send an automated confirmation message
        dbHelper.insertMessage(currentUserUid, currentUserName, receiverUid, "I have purchased: " + item.getTitle() + " for $" + String.format("%.2f", finalPrice), item.getId(), 2, finalPrice);
        
        Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show();
        loadMessages();
    }

    /**
     * Loads messages from the local database and scrolls to the latest.
     */
    private void loadMessages() {
        List<Message> messages = dbHelper.getMessages(currentUserUid, receiverUid);
        adapter.updateList(messages);
        if (adapter.getItemCount() > 0) {
            binding.rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    /**
     * Sends a plain text message.
     */
    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        if (dbHelper.insertMessage(currentUserUid, currentUserName, receiverUid, text, itemId, 0)) {
            binding.etMessage.setText("");
            loadMessages();
        } else {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }
}
