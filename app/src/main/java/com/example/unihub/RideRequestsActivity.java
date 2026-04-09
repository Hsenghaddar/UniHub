package com.example.unihub;

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
    private RideRequestsAdapter adapter;
    private int rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRideRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        rideId = getIntent().getIntExtra("RIDE_ID", -1);

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
        if (ride != null && ride.getAvailableSeats() > 0) {
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

    private void rejectRequest(RideRequest request) {
        if (dbHelper.updateRequestStatus(request.getId(), "rejected")) {
            Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
            loadRequests();
        } else {
            Toast.makeText(this, "Failed to reject request", Toast.LENGTH_SHORT).show();
        }
    }
}
