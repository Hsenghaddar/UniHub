package com.example.unihub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.unihub.databinding.ActivityRidesBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for browsing available rides.
 *
 * This activity displays a list of rides available within the user's university.
 * Users can view ride details by clicking on an item or offer a new ride using the FloatingActionButton.
 */
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

        // Retrieve the current user's UID and university ID to filter rides
        currentUserUid = sessionManager.getSavedFirebaseUid();
        LocalUser user = userDbHelper.getUserByFirebaseUid(currentUserUid);
        if (user != null) {
            userUniversityId = user.getUniversityId();
        }

        setupRecyclerView();

        // Navigate to AddRideActivity to create a new ride offer
        binding.fabAddRide.setOnClickListener(v -> {
            Intent intent = new Intent(RidesActivity.this, AddRideActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Initializes the RecyclerView with a RideAdapter.
     */
    private void setupRecyclerView() {
        binding.recyclerViewRides.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(new ArrayList<>(), currentUserUid, ride -> {
            // Navigate to details when a ride is clicked
            Intent intent = new Intent(RidesActivity.this, RideDetailsActivity.class);
            intent.putExtra("RIDE_ID", ride.getId());
            startActivity(intent);
        });
        binding.recyclerViewRides.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list of rides whenever the activity becomes visible
        loadRides();
    }

    /**
     * Fetches rides from the database and updates the UI.
     * Filters rides based on the user's university to ensure community relevance.
     */
    private void loadRides() {
        List<Ride> rides = dbHelper.getAllRides(userUniversityId);
        if (rides.isEmpty()) {
            binding.textViewEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewRides.setVisibility(View.GONE);
        } else {
            binding.textViewEmpty.setVisibility(View.GONE);
            binding.recyclerViewRides.setVisibility(View.VISIBLE);
        }
        adapter.updateList(rides);
    }
}
