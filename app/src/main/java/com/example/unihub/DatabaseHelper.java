package com.example.unihub;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "unihub_rides.db";
    private static final int DATABASE_VERSION = 2;

    // Table Rides
    private static final String TABLE_RIDES = "rides";
    private static final String COL_RIDE_ID = "id";
    private static final String COL_USER_UID = "user_uid";
    private static final String COL_DRIVER_NAME = "driver_name";
    private static final String COL_TYPE = "type";
    private static final String COL_FROM = "from_location";
    private static final String COL_TO = "to_location";
    private static final String COL_DATE = "date";
    private static final String COL_TIME = "time";
    private static final String COL_TOTAL_SEATS = "total_seats";
    private static final String COL_AVAILABLE_SEATS = "available_seats";
    private static final String COL_NOTE = "note";
    private static final String COL_STATUS = "status";
    private static final String COL_UNIVERSITY_ID = "university_id";

    // Table Ride Requests
    private static final String TABLE_REQUESTS = "ride_requests";
    private static final String COL_REQ_ID = "id";
    private static final String COL_REQ_RIDE_ID = "ride_id";
    private static final String COL_REQ_USER_UID = "requester_uid";
    private static final String COL_REQ_USER_NAME = "requester_name";
    private static final String COL_REQ_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

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
                COL_UNIVERSITY_ID + " INTEGER)";

        String createRequestsTable = "CREATE TABLE " + TABLE_REQUESTS + " (" +
                COL_REQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REQ_RIDE_ID + " INTEGER, " +
                COL_REQ_USER_UID + " TEXT, " +
                COL_REQ_USER_NAME + " TEXT, " +
                COL_REQ_STATUS + " TEXT, " +
                "FOREIGN KEY(" + COL_REQ_RIDE_ID + ") REFERENCES " + TABLE_RIDES + "(" + COL_RIDE_ID + "))";

        db.execSQL(createRidesTable);
        db.execSQL(createRequestsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQUESTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RIDES);
        onCreate(db);
    }

    // Methods
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

        long result = db.insert(TABLE_RIDES, null, values);
        return result != -1;
    }

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

    public boolean insertRideRequest(RideRequest request) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REQ_RIDE_ID, request.getRideId());
        values.put(COL_REQ_USER_UID, request.getRequesterUid());
        values.put(COL_REQ_USER_NAME, request.getRequesterName());
        values.put(COL_REQ_STATUS, request.getStatus());

        long result = db.insert(TABLE_REQUESTS, null, values);
        return result != -1;
    }

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

    public boolean updateRideSeats(int rideId, int availableSeats) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_AVAILABLE_SEATS, availableSeats);
        return db.update(TABLE_RIDES, values, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}) > 0;
    }

    public boolean updateRideStatus(int rideId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        return db.update(TABLE_RIDES, values, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}) > 0;
    }

    public boolean deleteRide(int rideId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REQUESTS, COL_REQ_RIDE_ID + "=?", new String[]{String.valueOf(rideId)});
        return db.delete(TABLE_RIDES, COL_RIDE_ID + "=?", new String[]{String.valueOf(rideId)}) > 0;
    }

    public boolean updateRequestStatus(int requestId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REQ_STATUS, status);
        return db.update(TABLE_REQUESTS, values, COL_REQ_ID + "=?", new String[]{String.valueOf(requestId)}) > 0;
    }

    public boolean hasAlreadyRequested(int rideId, String requesterUid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REQUESTS + " WHERE " + COL_REQ_RIDE_ID + " = ? AND " + COL_REQ_USER_UID + " = ?", new String[]{String.valueOf(rideId), requesterUid});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

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
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNIVERSITY_ID))
        );
    }
}
