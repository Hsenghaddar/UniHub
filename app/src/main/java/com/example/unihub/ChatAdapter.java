package com.example.unihub;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying chat messages in a RecyclerView.
 *
 * This adapter supports two different view types: sent messages (right-aligned)
 * and received messages (left-aligned). It also handles the display of 
 * marketplace item previews and interactive purchase buttons within messages.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserUid;
    private UserDatabaseHelper userDb;
    private OnPurchaseClickListener purchaseClickListener;

    /**
     * Interface for handling click events on the purchase button within a message.
     */
    public interface OnPurchaseClickListener {
        void onPurchaseClick(Message message);
    }

    /**
     * Sets the listener for purchase actions.
     */
    public void setOnPurchaseClickListener(OnPurchaseClickListener listener) {
        this.purchaseClickListener = listener;
    }

    /**
     * Constructor for ChatAdapter.
     *
     * @param messages List of messages to display.
     * @param currentUserUid UID of the logged-in user to determine sent vs received.
     */
    public ChatAdapter(List<Message> messages, String currentUserUid) {
        this.messages = messages;
        this.currentUserUid = currentUserUid;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine if the message was sent by the current user
        if (messages.get(position).getSenderUid().equals(currentUserUid)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (userDb == null) userDb = new UserDatabaseHelper(parent.getContext());
        
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean showTime = true;

        // Optimization: only show timestamp if it's a different minute from the previous message
        if (position > 0) {
            Message previousMessage = messages.get(position - 1);
            showTime = !isSameMinute(message.getTimestamp(), previousMessage.getTimestamp());
        }

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, showTime, userDb);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, showTime, userDb, purchaseClickListener);
        }
    }

    /**
     * Compares two timestamps down to the minute.
     */
    private boolean isSameMinute(String t1, String t2) {
        if (t1 == null || t2 == null) return false;
        if (t1.length() >= 16 && t2.length() >= 16) {
            return t1.substring(0, 16).equals(t2.substring(0, 16));
        }
        return t1.equals(t2);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Updates the message list and refreshes the RecyclerView.
     */
    public void updateList(List<Message> newList) {
        this.messages = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for messages sent by the current user.
     */
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp, tvItemTitle;
        ImageView ivItem;
        LinearLayout layoutItemPreview;
        Button btnPurchase;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvItemTitle = itemView.findViewById(R.id.tvItemPreviewTitle);
            ivItem = itemView.findViewById(R.id.ivItemPreview);
            layoutItemPreview = itemView.findViewById(R.id.layoutItemPreview);
            btnPurchase = itemView.findViewById(R.id.btnPurchaseAction);
        }

        void bind(Message message, boolean showTime, UserDatabaseHelper db) {
            tvMessage.setText(message.getMessageText());
            if (btnPurchase != null) btnPurchase.setVisibility(View.GONE); 

            if (showTime) {
                tvTimestamp.setVisibility(View.VISIBLE);
                tvTimestamp.setText(formatTime(message.getTimestamp()));
            } else {
                tvTimestamp.setVisibility(View.GONE);
            }

            // Display marketplace item preview if linked
            if (message.getItemId() != -1) {
                layoutItemPreview.setVisibility(View.VISIBLE);
                MarketplaceItem item = db.getMarketplaceItemById(message.getItemId());
                if (item != null) {
                    String title = item.getTitle();
                    if (message.getType() >= 1) { // Inquiry or Completed
                        title += "\nPrice: $" + String.format("%.2f", message.getPrice());
                    }
                    tvItemTitle.setText(title);
                    if (item.getImageUri() != null) {
                        ImageUtils.INSTANCE.loadImage(itemView.getContext(), Uri.parse(item.getImageUri()), ivItem);
                    }
                }
            } else {
                layoutItemPreview.setVisibility(View.GONE);
            }
        }
    }

    /**
     * ViewHolder for messages received from other users.
     */
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp, tvItemTitle;
        ImageView ivItem;
        LinearLayout layoutItemPreview;
        Button btnPurchase;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvItemTitle = itemView.findViewById(R.id.tvItemPreviewTitle);
            ivItem = itemView.findViewById(R.id.ivItemPreview);
            layoutItemPreview = itemView.findViewById(R.id.layoutItemPreview);
            btnPurchase = itemView.findViewById(R.id.btnPurchaseAction);
        }

        void bind(Message message, boolean showTime, UserDatabaseHelper db, OnPurchaseClickListener listener) {
            tvMessage.setText(message.getMessageText());
            
            // Handle interactive purchase buttons
            if (btnPurchase != null) {
                if (message.getType() == 1) { // Purchase Inquiry - button is clickable
                    btnPurchase.setVisibility(View.VISIBLE);
                    btnPurchase.setText("Confirm Purchase ($" + String.format("%.2f", message.getPrice()) + ")");
                    btnPurchase.setOnClickListener(v -> {
                        if (listener != null) listener.onPurchaseClick(message);
                    });
                } else if (message.getType() == 2) { // Purchase Completed - button is disabled
                    btnPurchase.setVisibility(View.VISIBLE);
                    btnPurchase.setText("Purchased ✓");
                    btnPurchase.setEnabled(false);
                    btnPurchase.setBackgroundTintList(null); 
                } else {
                    btnPurchase.setVisibility(View.GONE);
                }
            }

            if (showTime) {
                tvTimestamp.setVisibility(View.VISIBLE);
                tvTimestamp.setText(formatTime(message.getTimestamp()));
            } else {
                tvTimestamp.setVisibility(View.GONE);
            }

            // Display marketplace item preview if linked
            if (message.getItemId() != -1) {
                layoutItemPreview.setVisibility(View.VISIBLE);
                MarketplaceItem item = db.getMarketplaceItemById(message.getItemId());
                if (item != null) {
                    String title = item.getTitle();
                    if (message.getType() >= 1) {
                        title += "\nPrice: $" + String.format("%.2f", message.getPrice());
                    }
                    tvItemTitle.setText(title);
                    if (item.getImageUri() != null) {
                        ImageUtils.INSTANCE.loadImage(itemView.getContext(), Uri.parse(item.getImageUri()), ivItem);
                    }
                }
            } else {
                layoutItemPreview.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Helper to format a full timestamp string into a concise time string (HH:mm).
     */
    private static String formatTime(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return timeFormat.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }
}
