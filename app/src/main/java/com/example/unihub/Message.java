package com.example.unihub;

public class Message {
    private int id;
    private String senderUid;
    private String senderName;
    private String receiverUid;
    private String messageText;
    private String timestamp;
    private int itemId; // -1 if not related to an item
    private int type; // 0: normal, 1: purchase_request, 2: purchase_completed
    private double price;

    public Message(int id, String senderUid, String senderName, String receiverUid, String messageText, String timestamp) {
        this(id, senderUid, senderName, receiverUid, messageText, timestamp, -1, 0, 0.0);
    }

    public Message(int id, String senderUid, String senderName, String receiverUid, String messageText, String timestamp, int itemId) {
        this(id, senderUid, senderName, receiverUid, messageText, timestamp, itemId, 0, 0.0);
    }

    public Message(int id, String senderUid, String senderName, String receiverUid, String messageText, String timestamp, int itemId, int type) {
        this(id, senderUid, senderName, receiverUid, messageText, timestamp, itemId, type, 0.0);
    }

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
