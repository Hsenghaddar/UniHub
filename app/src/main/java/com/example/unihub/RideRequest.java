package com.example.unihub;

public class RideRequest {
    private int id;
    private int rideId;
    private String requesterUid;
    private String requesterName;
    private String status; // "pending", "approved", "rejected"

    public RideRequest() {}

    public RideRequest(int id, int rideId, String requesterUid, String requesterName, String status) {
        this.id = id;
        this.rideId = rideId;
        this.requesterUid = requesterUid;
        this.requesterName = requesterName;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRideId() { return rideId; }
    public void setRideId(int rideId) { this.rideId = rideId; }

    public String getRequesterUid() { return requesterUid; }
    public void setRequesterUid(String requesterUid) { this.requesterUid = requesterUid; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
