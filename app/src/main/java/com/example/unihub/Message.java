package com.example.unihub;

/**
 * Data model representing a chat message.
 *
 * This class stores information about individual messages sent between users.
 * It supports standard text messages as well as specialized message types for 
 * marketplace interactions, such as purchase requests and confirmations.
 */
public class Message {
    private int id;
    private String senderUid;
    private String senderName;
    private String receiverUid;
    private String messageText;
    private String timestamp;
    private int itemId; // -1 if not related to an item (e.g., marketplace item)
    private int type; // 0: normal, 1: purchase_request, 2: purchase_completed
    private double price;

    /**
     * Constructor for a standard text message.
     */
    public Message(int id, String senderUid, String senderName, String receiverUid, String messageText, String timestamp) {
        this(id, senderUid, senderName, receiverUid, messageText, timestamp, -1, 0, 0.0);
    }

    /**
     * Constructor for a message related to a specific marketplace item.
     */
    public Message(int id, String senderUid, String senderName, String receiverUid, String messageText, String timestamp, int itemId) {
        this(id, senderUid, senderName, receiverUid, messageText, timestamp, itemId, 0, 0.0);
    }

    /**
     * Constructor for a message with a specific interaction type (e.g., purchase request).
     */
    public Message(int id, String senderUid, String senderName, String receiverUid, String messageText, String timestamp, int itemId, int type) {
        this(id, senderUid, senderName, receiverUid, messageText, timestamp, itemId, type, 0.0);
    }

    /**
     * Comprehensive constructor for all message fields.
     *
     * @param id Unique message ID.
     * @param senderUid Firebase UID of the sender.
     * @param senderName Name of the sender.
     * @param receiverUid Firebase UID of the recipient.
     * @param messageText Content of the message.
     * @param timestamp Time when the message was sent.
     * @param itemId ID of the associated marketplace item, or -1.
     * @param type The type of message (normal, request, completion).
     * @param price The price involved in a marketplace interaction.
     */
    public Message(int id, String senderUid, String senderName, String receiverUid, String messageText, String timestamp, int itemId, int type, double price) {
        this.id = id;
        this.senderUid = senderUid;
        this.senderName = senderName;
        this.receiverUid = receiverUid;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.itemId = itemId;
        this.type = type;
        this.price = price;
    }

    public int getId() { return id; }
    public String getSenderUid() { return senderUid; }
    public String getSenderName() { return senderName; }
    public String getReceiverUid() { return receiverUid; }
    public String getMessageText() { return messageText; }
    public String getTimestamp() { return timestamp; }
    public int getItemId() { return itemId; }
    public int getType() { return type; }
    public double getPrice() { return price; }
}
