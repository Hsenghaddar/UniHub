package com.example.unihub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.unihub.databinding.ActivityRideRequestsBinding;
import java.util.List;

public class RideRequestsActivity extends AppCompatActivity {

    private ActivityRideRequestsBinding binding;
    private DatabaseHelper dbHelper;
    private UserDatabaseHelper userDbHelper;
    private RideRequestsAdapter adapter;
    private int rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRideRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        userDbHelper = new UserDatabaseHelper(this);
        rideId = getIntent().getIntExtra("RIDE_ID", -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ride Requests");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        loadRequests();
    }

    private void setupRecyclerView() {
        binding.rvRideRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideRequestsAdapter(null, new RideRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onApprove(RideRequest request) {
                approveRequest(request);
            }

            @Override
            public void onReject(RideRequest request) {
                rejectRequest(request);
            }

            @Override
            public void onContact(RideRequest request) {
                Intent intent = new Intent(RideRequestsActivity.this, ChatActivity.class);
                intent.putExtra("RECEIVER_UID", request.getRequesterUid());
                intent.putExtra("RECEIVER_NAME", request.getRequesterName());
                startActivity(intent);
            }
        });
        binding.rvRideRequests.setAdapter(adapter);
    }

    private void loadRequests() {
        List<RideRequest> requests = dbHelper.getRequestsForRide(rideId);
        if (requests == null || requests.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvRideRequests.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvRideRequests.setVisibility(View.VISIBLE);
            adapter.updateList(requests);
        }
    }

    private void approveRequest(RideRequest request) {
        Ride ride = dbHelper.getRideById(rideId);
        if (ride != null) {
            if (ride.getAvailableSeats() > 0) {
                if (dbHelper.updateRequestStatus(request.getId(), "approved")) {
                    dbHelper.updateRideSeats(rideId, ride.getAvailableSeats() - 1);
                    Toast.makeText(this, "Request approved", Toast.LENGTH_SHORT).show();
                    loadRequests();
                } else {
                    Toast.makeText(this, "Failed to approve request", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No seats available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void rejectRequest(RideRequest request) {
        if (dbHelper.updateRequestStatus(request.getId(), "rejected")) {
            Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
            loadRequests();
        } else {
            Toast.makeText(this, "Failed to reject request", Toast.LENGTH_SHORT).show();
        }
    }
}
