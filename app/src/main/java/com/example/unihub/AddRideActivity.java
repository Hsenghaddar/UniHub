package com.example.unihub;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.unihub.databinding.ActivityAddRideBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRideActivity extends AppCompatActivity {

    private ActivityAddRideBinding binding;
    private DatabaseHelper dbHelper;
    private UserDatabaseHelper userDbHelper;
    private SessionManager sessionManager;
    private String currentUserUid;
    private String currentUserName;
    private int userUniversityId;

    private boolean isEditMode = false;
    private int rideIdToEdit = -1;
    private Ride existingRide;

    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        userDbHelper = new UserDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        currentUserUid = sessionManager.getSavedFirebaseUid();
        LocalUser user = userDbHelper.getUserByFirebaseUid(currentUserUid);
        if (user != null) {
            currentUserName = user.getFullName();
            userUniversityId = user.getUniversityId();
        }

        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        rideIdToEdit = getIntent().getIntExtra("RIDE_ID", -1);

        setupTypeDropdown();
        setupDateTimePickers();

        if (isEditMode && rideIdToEdit != -1) {
            loadRideForEditing();
        }

        binding.btnSubmit.setOnClickListener(v -> submitRide());
    }

    private void loadRideForEditing() {
        existingRide = dbHelper.getRideById(rideIdToEdit);
        if (existingRide != null) {
            binding.tvTitle.setText("Edit Ride");
            binding.btnSubmit.setText("Update Ride");

            binding.autoCompleteType.setText(existingRide.getType(), false);
            updateLabelsByType(existingRide.getType());

            binding.etFrom.setText(existingRide.getFromLocation());
            binding.etTo.setText(existingRide.getToLocation());
            binding.etDate.setText(existingRide.getDate());
            binding.etTime.setText(existingRide.getTime());
            binding.etSeats.setText(String.valueOf(existingRide.getTotalSeats()));
            binding.etNote.setText(existingRide.getNote());
        }
    }

    private void setupTypeDropdown() {
        String[] types = getResources().getStringArray(R.array.ride_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        binding.autoCompleteType.setAdapter(adapter);

        binding.autoCompleteType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = (String) parent.getItemAtPosition(position);
            updateLabelsByType(selectedType);
        });
    }

    private void updateLabelsByType(String type) {
        if (type.equalsIgnoreCase("Request")) {
            binding.tvSeatsLabel.setText("Requested Seats");
            binding.etSeats.setHint("Number of seats you need");
        } else {
            binding.tvSeatsLabel.setText("Available Seats");
            binding.etSeats.setHint("Number of seats available");
        }
    }

    private void setupDateTimePickers() {
        binding.etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateLabel();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        binding.etTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateTimeLabel();
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false);
            timePickerDialog.show();
        });
    }

    private void updateDateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        binding.etDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeLabel() {
        String myFormat = "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        binding.etTime.setText(sdf.format(calendar.getTime()));
    }

    private void submitRide() {
        String type = binding.autoCompleteType.getText().toString();
        String from = binding.etFrom.getText().toString().trim();
        String to = binding.etTo.getText().toString().trim();
        String date = binding.etDate.getText().toString().trim();
        String time = binding.etTime.getText().toString().trim();
        String seatsStr = binding.etSeats.getText().toString().trim();
        String note = binding.etNote.getText().toString().trim();

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(from) || TextUtils.isEmpty(to) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(seatsStr)) {
            Toast.makeText(this, "Please specify number of seats", Toast.LENGTH_SHORT).show();
            return;
        }

        int seats;
        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0) {
                Toast.makeText(this, "Seats must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid seat number", Toast.LENGTH_SHORT).show();
            return;
        }

        Ride ride;
        if (isEditMode && existingRide != null) {
            ride = existingRide;
        } else {
            ride = new Ride();
            ride.setUserUid(currentUserUid);
            ride.setDriverName(currentUserName);
            ride.setStatus("active");
            ride.setUniversityId(userUniversityId);
        }

        ride.setType(type);
        ride.setFromLocation(from);
        ride.setToLocation(to);
        ride.setDate(date);
        ride.setTime(time);
        ride.setTotalSeats(seats);
        ride.setAvailableSeats(seats);
        ride.setNote(note);

        boolean success;
        if (isEditMode) {
            success = dbHelper.updateRide(ride);
        } else {
            success = dbHelper.insertRide(ride);
        }

        if (success) {
            Toast.makeText(this, isEditMode ? "Ride updated successfully" : "Ride posted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Operation failed", Toast.LENGTH_SHORT).show();
        }
    }
}
