package com.example.unihub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.unihub.databinding.ActivityRideDetailsBinding;

public class RideDetailsActivity extends AppCompatActivity {

    private ActivityRideDetailsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int rideId;
    private Ride ride;
    private String currentUserUid;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRideDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserUid = sessionManager.getSavedFirebaseUid();

        UserDatabaseHelper userDbHelper = new UserDatabaseHelper(this);
        LocalUser user = userDbHelper.getUserByFirebaseUid(currentUserUid);
        if (user != null) {
            currentUserName = user.getFullName();
        }

        rideId = getIntent().getIntExtra("RIDE_ID", -1);

        binding.btnRequestSeat.setOnClickListener(v -> requestSeat());

        binding.btnMessageDriver.setOnClickListener(v -> {
            if (ride != null) {
                Intent intent = new Intent(RideDetailsActivity.this, ChatActivity.class);
                intent.putExtra("RECEIVER_UID", ride.getUserUid());
                intent.putExtra("RECEIVER_NAME", ride.getDriverName());
                startActivity(intent);
            }
        });

        binding.btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(RideDetailsActivity.this, RideRequestsActivity.class);
            intent.putExtra("RIDE_ID", rideId);
            startActivity(intent);
        });

        binding.btnEditRide.setOnClickListener(v -> {
            Intent intent = new Intent(RideDetailsActivity.this, AddRideActivity.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("RIDE_ID", rideId);
            startActivity(intent);
        });

        binding.btnDeleteRide.setOnClickListener(v -> showDeleteConfirmation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRideDetails();
    }

    private void loadRideDetails() {
        ride = dbHelper.getRideById(rideId);
        if (ride != null) {
            binding.tvDetailDriverName.setText(ride.getDriverName() + " (" + ride.getType() + ")");
            binding.tvDetailFromTo.setText("From: " + ride.getFromLocation() + "\nTo: " + ride.getToLocation());
            binding.tvDetailDateTime.setText("Date: " + ride.getDate() + " | Time: " + ride.getTime());
            binding.tvDetailSeats.setText(ride.getType().equalsIgnoreCase("Request") ? "Requested Seats: " + ride.getTotalSeats() : "Available Seats: " + ride.getAvailableSeats() + " / " + ride.getTotalSeats());
            binding.tvDetailNote.setText(ride.getNote().isEmpty() ? "No additional notes" : ride.getNote());

            if (ride.getUserUid().equals(currentUserUid)) {
                // Owner view
                binding.tvYourPostingLabel.setVisibility(View.VISIBLE);
                binding.layoutOwnerActions.setVisibility(View.VISIBLE);
                binding.btnRequestSeat.setVisibility(View.GONE);
                binding.btnMessageDriver.setVisibility(View.GONE);
            } else {
                // Visitor view
                binding.tvYourPostingLabel.setVisibility(View.GONE);
                binding.layoutOwnerActions.setVisibility(View.GONE);
                
                binding.btnMessageDriver.setVisibility(View.VISIBLE);
                binding.btnMessageDriver.setText("Message " + ride.getDriverName());

                if (ride.getType().equalsIgnoreCase("Offer")) {
                    if (ride.getAvailableSeats() > 0 && ride.getStatus().equals("active")) {
                        if (!dbHelper.hasAlreadyRequested(rideId, currentUserUid)) {
                            binding.btnRequestSeat.setVisibility(View.VISIBLE);
                            binding.btnRequestSeat.setText("Request Seat");
                        } else {
                            binding.btnRequestSeat.setVisibility(View.GONE);
                        }
                    } else {
                        binding.btnRequestSeat.setVisibility(View.GONE);
                    }
                } else {
                    // It's a Request
                    if (ride.getStatus().equals("active")) {
                        if (!dbHelper.hasAlreadyRequested(rideId, currentUserUid)) {
                            binding.btnRequestSeat.setVisibility(View.VISIBLE);
                            binding.btnRequestSeat.setText("Offer Seat");
                        } else {
                            binding.btnRequestSeat.setVisibility(View.GONE);
                        }
                    } else {
                        binding.btnRequestSeat.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            Toast.makeText(this, "Ride not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void requestSeat() {
        if (dbHelper.hasAlreadyRequested(rideId, currentUserUid)) {
            Toast.makeText(this, "You have already responded to this ride", Toast.LENGTH_SHORT).show();
            return;
        }

        RideRequest request = new RideRequest();
        request.setRideId(rideId);
        request.setRequesterUid(currentUserUid);
        request.setRequesterName(currentUserName);
        request.setStatus("pending");

        if (dbHelper.insertRideRequest(request)) {
            Toast.makeText(this, "Response sent successfully", Toast.LENGTH_SHORT).show();
            binding.btnRequestSeat.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Failed to send response", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ride")
                .setMessage("Are you sure you want to delete this ride?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteRide(rideId)) {
                        Toast.makeText(RideDetailsActivity.this, "Ride deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RideDetailsActivity.this, "Failed to delete ride", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
