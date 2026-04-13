package com.example.unihub;

import java.io.Serializable;

/**
 * Ride data model representing a carpooling entry in the system.
 * Implements Serializable to allow passing Ride objects between Activities via Intents.
 */
public class Ride implements Serializable {
    private int id;
    private String userUid; // Firebase UID of the user who created the ride
    private String driverName;
    private String type; // "Offer" or "Request"
    private String fromLocation;
    private String toLocation;
    private String date;
    private String time;
    private int totalSeats;
    private int availableSeats;
    private String note;
    private String status; // Possible values: "active", "full", "completed"
    private int universityId;
    private boolean hasNotification; // Flag to indicate if there are unread updates for this ride

    /**
     * Default constructor required for some serialization frameworks.
     */
    public Ride() {}

    /**
     * Constructor without notification flag (defaults to false).
     */
    public Ride(int id, String userUid, String driverName, String type, String fromLocation, String toLocation, String date, String time, int totalSeats, int availableSeats, String note, String status, int universityId) {
        this(id, userUid, driverName, type, fromLocation, toLocation, date, time, totalSeats, availableSeats, note, status, universityId, false);
    }

    /**
     * Full constructor for Ride object.
     */
    public Ride(int id, String userUid, String driverName, String type, String fromLocation, String toLocation, String date, String time, int totalSeats, int availableSeats, String note, String status, int universityId, boolean hasNotification) {
        this.id = id;
        this.userUid = userUid;
        this.driverName = driverName;
        this.type = type;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.date = date;
        this.time = time;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.note = note;
        this.status = status;
        this.universityId = universityId;
        this.hasNotification = hasNotification;
    }

    // Getters and Setters with basic Java documentation

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getUniversityId() { return universityId; }
    public void setUniversityId(int universityId) { this.universityId = universityId; }

    public boolean isHasNotification() { return hasNotification; }
    public void setHasNotification(boolean hasNotification) { this.hasNotification = hasNotification; }
}
