package com.example.unihub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper manages the SQLite database for rides, ride requests, and chat messages.
 * This is a separate database from UserDatabaseHelper, specifically handling activity-related data.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "unihub_rides.db";
    private static final int DATABASE_VERSION = 7;

    // --- Table Rides Columns ---
    private static final String TABLE_RIDES = "rides";
    private static final String COL_RIDE_ID = "id";
    private static final String COL_USER_UID = "user_uid";
    private static final String COL_DRIVER_NAME = "driver_name";
    private static final String COL_TYPE = "type"; // "Offer" or "Request"
    private static final String COL_FROM = "from_location";
    private static final String COL_TO = "to_location";
    private static final String COL_DATE = "date";
    private static final String COL_TIME = "time";
    private static final String COL_TOTAL_SEATS = "total_seats";
    private static final String COL_AVAILABLE_SEATS = "available_seats";
    private static final String COL_NOTE = "note";
    private static final String COL_STATUS = "status"; // active, full, completed
    private static final String COL_UNIVERSITY_ID = "university_id";
    private static final String COL_RIDE_HAS_NOTIFICATION = "has_notification";

    // --- Table Ride Requests Columns ---
    private static final String TABLE_REQUESTS = "ride_requests";
    private static final String COL_REQ_ID = "id";
    private static final String COL_REQ_RIDE_ID = "ride_id";
    private static final String COL_REQ_USER_UID = "requester_uid";
    private static final String COL_REQ_USER_NAME = "requester_name";
    private static final String COL_REQ_STATUS = "status"; // pending, accepted, rejected

    // --- Table Messages (Chat) Columns ---
    private static final String TABLE_MESSAGES = "messages";
    private static final String COL_MSG_ID = "id";
    private static final String COL_SENDER_UID = "sender_uid";
    private static final String COL_SENDER_NAME = "sender_name";
    private static final String COL_RECEIVER_UID = "receiver_uid";
    private static final String COL_MSG_TEXT = "message_text";
    private static final String COL_MSG_ITEM_ID = "item_id"; // Links to Marketplace item if applicable
    private static final String COL_MSG_TYPE = "message_type"; // 0: normal, 1: purchase_request, 2: purchase_completed
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_IS_READ = "is_read";
    private static final String COL_MSG_PRICE = "price";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Initializes the database tables.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createRidesTable = "CREATE TABLE " + TABLE_RIDES + " (" +
                COL_RIDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_UID + " TEXT, " +
                COL_DRIVER_NAME + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_FROM + " TEXT, " +
                COL_TO + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_TIME + " TEXT, " +
                COL_TOTAL_SEATS + " INTEGER, " +
                COL_AVAILABLE_SEATS + " INTEGER, " +
                COL_NOTE + " TEXT, " +
                COL_STATUS + " TEXT, " +
                COL_UNIVERSITY_ID + " INTEGER, " +
                COL_RIDE_HAS_NOTIFICATION + " INTEGER DEFAULT 0)";

        String createRequestsTable = "CREATE TABLE " + TABLE_REQUESTS + " (" +
                COL_REQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REQ_RIDE_ID + " INTEGER, " +
                COL_REQ_USER_UID + " TEXT, " +
                COL_REQ_USER_NAME + " TEXT, " +
                COL_REQ_STATUS + " TEXT, " +
                "FOREIGN KEY(" + COL_REQ_RIDE_ID + ") REFERENCES " + TABLE_RIDES + "(" + COL_RIDE_ID + "))";

        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COL_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SENDER_UID + " TEXT, " +
                COL_SENDER_NAME + " TEXT, " +
                COL_RECEIVER_UID + " TEXT, " +
                COL_MSG_TEXT + " TEXT, " +
                COL_MSG_ITEM_ID + " INTEGER DEFAULT -1, " +
                COL_MSG_TYPE + " INTEGER DEFAULT 0, " +
                COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COL_IS_READ + " INTEGER DEFAULT 0, " +
                COL_MSG_PRICE + " REAL DEFAULT 0.0)";

        db.execSQL(createRidesTable);
        db.execSQL(createRequestsTable);
        db.execSQL(createMessagesTable);
    }

    /**
     * Handles schema upgrades between database versions.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            String createMessagesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES + " (" +
                    COL_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_SENDER_UID + " TEXT, " +
                    COL_SENDER_NAME + " TEXT, " +
                    COL_RECEIVER_UID + " TEXT, " +
                    COL_MSG_TEXT + " TEXT, " +
                    COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
            db.execSQL(createMessagesTable);
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_MSG_ITEM_ID + " INTEGER DEFAULT -1");
            db.execSQL("ALTER TABLE " + TABLE_RIDES + " ADD COLUMN " + COL_RIDE_HAS_NOTIFICATION + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_MSG_TYPE + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_IS_READ + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_MSG_PRICE + " REAL DEFAULT 0.0");
        }
    }

    /**
     * Inserts a new ride offer/request into the database.
     */
    public boolean insertRide(Ride ride) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_UID, ride.getUserUid());
        values.put(COL_DRIVER_NAME, ride.getDriverName());
        values.put(COL_TYPE, ride.getType());
        values.put(COL_FROM, ride.getFromLocation());
        values.put(COL_TO, ride.getToLocation());
        values.put(COL_DATE, ride.getDate());
        values.put(COL_TIME, ride.getTime());
        values.put(COL_TOTAL_SEATS, ride.getTotalSeats());
        values.put(COL_AVAILABLE_SEATS, ride.getAvailableSeats());
        values.put(COL_NOTE, ride.getNote());
        values.put(COL_STATUS, ride.getStatus());
        values.put(COL_UNIVERSITY_ID, ride.getUniversityId());
        values.put(COL_RIDE_HAS_NOTIFICATION, ride.isHasNotification() ? 1 : 0);

        long result = db.insert(TABLE_RIDES, null, values);
        return result != -1;
    }

    /**
     * Updates an existing ride's details.
     */
    public boolean updateRide(Ride ride) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TYPE, ride.getType());
        values.put(COL_FROM, ride.getFromLocation());
        values.put(COL_TO, ride.getToLocation());
        values.put(COL_DATE, ride.getDate());
        values.put(COL_TIME, ride.getTime());
        values.put(COL_TOTAL_SEATS, ride.getTotalSeats());
        values.put(COL_AVAILABLE_SEATS, ride.getAvailableSeats());
        values.put(COL_NOTE, ride.getNote());

        return db.update(TABLE_RIDES, values, COL_RIDE_ID + " = ?", new String[]{String.valueOf(ride.getId())}) > 0;
    }

    /**
     * Fetches all active rides for a specific university.
     */
    public List<Ride> getAllRides(int universityId) {
        List<Ride> rides = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RIDES + " WHERE " + COL_UNIVERSITY_ID + " = ? AND " + COL_STATUS + " = 'active' ORDER BY " + COL_DATE + " DESC", new String[]{String.valueOf(universityId)});

        if (cursor.moveToFirst()) {
            do {
                rides.add(cursorToRide(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rides;
    }

    /**
     * Retrieves a single ride by its database ID.
     */
    public Ride getRideById(int rideId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RIDES, null, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Ride ride = cursorToRide(cursor);
            cursor.close();
            return ride;
        }
        return null;
    }

    /**
     * Submits a request to join a ride.
     */
    public boolean insertRideRequest(RideRequest request) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REQ_RIDE_ID, request.getRideId());
        values.put(COL_REQ_USER_UID, request.getRequesterUid());
        values.put(COL_REQ_USER_NAME, request.getRequesterName());
        values.put(COL_REQ_STATUS, request.getStatus());

        long result = db.insert(TABLE_REQUESTS, null, values);
        if (result != -1) {
            setRideNotification(request.getRideId(), true);
        }
        return result != -1;
    }

    /**
     * Retrieves all join requests for a specific ride.
     */
    public List<RideRequest> getRequestsForRide(int rideId) {
        List<RideRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REQUESTS + " WHERE " + COL_REQ_RIDE_ID + " = ?", new String[]{String.valueOf(rideId)});

        if (cursor.moveToFirst()) {
            do {
                requests.add(new RideRequest(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_REQ_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_REQ_RIDE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_USER_UID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_USER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_REQ_STATUS))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return requests;
    }

    /**
     * Updates the number of available seats in a ride.
     */
    public boolean updateRideSeats(int rideId, int availableSeats) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AVAILABLE_SEATS, availableSeats);
        return db.update(TABLE_RIDES, values, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}) > 0;
    }

    /**
     * Updates the overall status of a ride (e.g., active -> full).
     */
    public boolean updateRideStatus(int rideId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        return db.update(TABLE_RIDES, values, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}) > 0;
    }

    /**
     * Marks a ride as having unread notifications for the driver.
     */
    public boolean setRideNotification(int rideId, boolean hasNotification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RIDE_HAS_NOTIFICATION, hasNotification ? 1 : 0);
        return db.update(TABLE_RIDES, values, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}) > 0;
    }

    /**
     * Deletes a ride and all associated join requests.
     */
    public boolean deleteRide(int rideId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REQUESTS, COL_REQ_RIDE_ID + "=?", new String[]{String.valueOf(rideId)});
        return db.delete(TABLE_RIDES, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}) > 0;
    }

    /**
     * Updates the status of a specific ride request (Accepted/Rejected).
     */
    public boolean updateRequestStatus(int requestId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REQ_STATUS, status);
        return db.update(TABLE_REQUESTS, values, COL_REQ_ID + "=?", new String[]{String.valueOf(requestId)}) > 0;
    }

    /**
     * Checks if a user has already sent a request for a particular ride.
     */
    public boolean hasAlreadyRequested(int rideId, String requesterUid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REQUESTS + " WHERE " + COL_REQ_RIDE_ID + " = ? AND " + COL_REQ_USER_UID + " = ?", new String[]{String.valueOf(rideId), requesterUid});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Retrieves all rides posted by a specific user.
     */
    public List<Ride> getRidesByUser(String userUid) {
        List<Ride> rides = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RIDES + " WHERE " + COL_USER_UID + " = ? ORDER BY " + COL_DATE + " DESC", new String[]{userUid});

        if (cursor.moveToFirst()) {
            do {
                rides.add(cursorToRide(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rides;
    }

    // --- Message Methods (Chat) ---

    public boolean insertMessage(String senderUid, String senderName, String receiverUid, String text) {
        return insertMessage(senderUid, senderName, receiverUid, text, -1, 0, 0.0);
    }

    public boolean insertMessage(String senderUid, String senderName, String receiverUid, String text, int itemId) {
        return insertMessage(senderUid, senderName, receiverUid, text, itemId, 0, 0.0);
    }

    public boolean insertMessage(String senderUid, String senderName, String receiverUid, String text, int itemId, int type) {
        return insertMessage(senderUid, senderName, receiverUid, text, itemId, type, 0.0);
    }

    /**
     * Inserts a new message into the chat database.
     */
    public boolean insertMessage(String senderUid, String senderName, String receiverUid, String text, int itemId, int type, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SENDER_UID, senderUid);
        values.put(COL_SENDER_NAME, senderName);
        values.put(COL_RECEIVER_UID, receiverUid);
        values.put(COL_MSG_TEXT, text);
        values.put(COL_MSG_ITEM_ID, itemId);
        values.put(COL_MSG_TYPE, type);
        values.put(COL_MSG_PRICE, price);
        values.put(COL_IS_READ, 0);
        return db.insert(TABLE_MESSAGES, null, values) != -1;
    }

    /**
     * Updates the type of a message (e.g., from purchase_request to purchase_completed).
     */
    public boolean updateMessageType(int msgId, int newType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MSG_TYPE, newType);
        return db.update(TABLE_MESSAGES, values, COL_MSG_ID + "=?", new String[]{String.valueOf(msgId)}) > 0;
    }

    /**
     * Marks messages as read for a specific conversation.
     */
    public boolean markMessagesAsRead(String currentUserId, String otherUserId, int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_READ, 1);
        String whereClause = COL_RECEIVER_UID + "=? AND " + COL_SENDER_UID + "=? AND " + COL_MSG_ITEM_ID + "=?";
        return db.update(TABLE_MESSAGES, values, whereClause, new String[]{currentUserId, otherUserId, String.valueOf(itemId)}) > 0;
    }

    /**
     * Retrieves the chat history between two users for a specific item (or overall).
     */
    public List<Message> getMessages(String user1Uid, String user2Uid) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " +
                "(" + COL_SENDER_UID + "=? AND " + COL_RECEIVER_UID + "=?) OR " +
                "(" + COL_SENDER_UID + "=? AND " + COL_RECEIVER_UID + "=?) " +
                "ORDER BY " + COL_TIMESTAMP + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{user1Uid, user2Uid, user2Uid, user1Uid});

        if (cursor.moveToFirst()) {
            do {
                messages.add(new Message(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_MSG_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_UID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_RECEIVER_UID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_MSG_ITEM_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_MSG_TYPE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_MSG_PRICE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }

    /**
     * Helper to convert a cursor row into a Ride object.
     */
    private Ride cursorToRide(Cursor cursor) {
        return new Ride(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_RIDE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_UID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DRIVER_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_FROM)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TO)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOTAL_SEATS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_AVAILABLE_SEATS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNIVERSITY_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_RIDE_HAS_NOTIFICATION)) == 1
        );
    }
}
