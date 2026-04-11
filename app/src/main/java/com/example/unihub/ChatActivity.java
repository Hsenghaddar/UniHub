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

        currentUserUid = sessionManager.getSavedFirebaseUid();
        LocalUser user = userDbHelper.getUserByFirebaseUid(currentUserUid);
        if (user != null) {
            currentUserName = user.getFullName();
        }

        receiverUid = getIntent().getStringExtra("RECEIVER_UID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");
        itemId = getIntent().getIntExtra("ITEM_ID", -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(receiverName != null ? receiverName : "Chat");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupItemPreview();
        setupRecyclerView();
        loadMessages();
        
        if (itemId != -1) {
            dbHelper.markMessagesAsRead(currentUserUid, receiverUid, itemId);
        }

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 101, 0, "Send Purchase Link")
            .setIcon(android.R.drawable.ic_menu_send)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 101) {
            showItemSelectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    private void sendPurchaseInquiry(MarketplaceItem item, double price) {
        String inquiryText = "Proposed Purchase: " + item.getTitle() + " for $" + String.format("%.2f", price);
        if (dbHelper.insertMessage(currentUserUid, currentUserName, receiverUid, inquiryText, item.getId(), 1, price)) {
            loadMessages();
        }
    }

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
                binding.ivItemPreview.setImageURI(Uri.parse(item.getImageUri()));
            }
        } else {
            binding.cardItemPreview.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(new ArrayList<>(), currentUserUid);
        adapter.setOnPurchaseClickListener(msg -> handlePurchase(msg));
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

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

        // Mark as completed immediately
        dbHelper.updateMessageType(msg.getId(), 2);

        int newStock = Math.max(0, item.getStock() - 1);
        userDbHelper.updateMarketplaceStock(item.getId(), newStock);
        
        // Use the price from the message instead of the original item price
        double finalPrice = msg.getPrice() > 0 ? msg.getPrice() : item.getPrice();
        userDbHelper.recordSale(item.getId(), currentUserUid, 1, finalPrice);
        
        dbHelper.insertMessage(currentUserUid, currentUserName, receiverUid, "I have purchased: " + item.getTitle() + " for $" + String.format("%.2f", finalPrice), item.getId(), 2, finalPrice);
        
        Toast.makeText(this, "Purchase successful!", Toast.LENGTH_SHORT).show();
        loadMessages();
    }

    private void loadMessages() {
        List<Message> messages = dbHelper.getMessages(currentUserUid, receiverUid);
        adapter.updateList(messages);
        if (adapter.getItemCount() > 0) {
            binding.rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

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
