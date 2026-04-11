package com.example.unihub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.unihub.databinding.ActivityRidesBinding;
import java.util.ArrayList;
import java.util.List;

public class RidesActivity extends AppCompatActivity {

    private ActivityRidesBinding binding;
    private DatabaseHelper dbHelper;
    private UserDatabaseHelper userDbHelper;
    private SessionManager sessionManager;
    private RideAdapter adapter;
    private int userUniversityId;
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRidesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        userDbHelper = new UserDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        currentUserUid = sessionManager.getSavedFirebaseUid();
        LocalUser user = userDbHelper.getUserByFirebaseUid(currentUserUid);
        if (user != null) {
            userUniversityId = user.getUniversityId();
        }

        setupRecyclerView();

        binding.fabAddRide.setOnClickListener(v -> {
            Intent intent = new Intent(RidesActivity.this, AddRideActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        binding.recyclerViewRides.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(new ArrayList<>(), currentUserUid, ride -> {
            Intent intent = new Intent(RidesActivity.this, RideDetailsActivity.class);
            intent.putExtra("RIDE_ID", ride.getId());
            startActivity(intent);
        });
        binding.recyclerViewRides.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRides();
    }

    private void loadRides() {
        List<Ride> rides = dbHelper.getAllRides(userUniversityId);
        if (rides.isEmpty()) {
            binding.textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.textViewEmpty.setVisibility(View.GONE);
        }
        adapter.updateList(rides);
    }
}
