package com.example.unihub;

/**
 * Data model representing a request to join a ride.
 *
 * This class stores information about a user's request to participate in a specific ride,
 * including the current status of the request (pending, approved, or rejected).
 */
public class RideRequest {
    private int id;
    private int rideId;
    private String requesterUid;
    private String requesterName;
    private String status; // "pending", "approved", "rejected"

    /**
     * Default constructor for RideRequest.
     */
    public RideRequest() {}

    /**
     * Full constructor for RideRequest.
     *
     * @param id The unique identifier of the ride request.
     * @param rideId The identifier of the ride being requested.
     * @param requesterUid The Firebase UID of the user making the request.
     * @param requesterName The name of the user making the request.
     * @param status The current status of the request ("pending", "approved", "rejected").
     */
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
