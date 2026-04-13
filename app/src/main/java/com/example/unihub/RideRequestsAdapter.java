package com.example.unihub;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.unihub.databinding.ItemRideRequestBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of ride requests in a RecyclerView.
 *
 * This adapter handles the visualization of individual ride requests, showing the requester's name,
 * the current status (pending, approved, or rejected), and providing action buttons for the driver.
 */
public class RideRequestsAdapter extends RecyclerView.Adapter<RideRequestsAdapter.ViewHolder> {

    private List<RideRequest> requests;
    private OnRequestActionListener listener;

    /**
     * Interface for handling actions on a ride request.
     */
    public interface OnRequestActionListener {
        /** Called when the driver approves a request. */
        void onApprove(RideRequest request);
        /** Called when the driver rejects a request. */
        void onReject(RideRequest request);
        /** Called when the driver wants to contact the requester via chat. */
        void onContact(RideRequest request);
    }

    /**
     * Constructor for RideRequestsAdapter.
     *
     * @param requests List of RideRequest objects to display.
     * @param listener Listener for handle user actions.
     */
    public RideRequestsAdapter(List<RideRequest> requests, OnRequestActionListener listener) {
        this.requests = requests != null ? requests : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRideRequestBinding binding = ItemRideRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideRequest request = requests.get(position);
        holder.binding.tvRequesterName.setText(request.getRequesterName());
        
        String status = request.getStatus();
        holder.binding.tvRequestStatus.setText("Status: " + status);
        
        // Change text color and visibility of buttons based on the request status
        if (status.equalsIgnoreCase("approved")) {
            holder.binding.tvRequestStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.binding.btnApprove.setVisibility(View.GONE);
            holder.binding.btnReject.setVisibility(View.GONE);
        } else if (status.equalsIgnoreCase("rejected")) {
            holder.binding.tvRequestStatus.setTextColor(Color.parseColor("#F44336")); // Red
            holder.binding.btnApprove.setVisibility(View.GONE);
            holder.binding.btnReject.setVisibility(View.GONE);
        } else {
            holder.binding.tvRequestStatus.setTextColor(Color.parseColor("#757575")); // Gray
            holder.binding.btnApprove.setVisibility(View.VISIBLE);
            holder.binding.btnReject.setVisibility(View.VISIBLE);
        }

        // Set up click listeners for the action buttons
        holder.binding.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(request);
        });
        
        holder.binding.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(request);
        });

        holder.binding.btnContact.setOnClickListener(v -> {
            if (listener != null) listener.onContact(request);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    /**
     * Updates the data set and refreshes the RecyclerView.
     *
     * @param newList The new list of ride requests.
     */
    public void updateList(List<RideRequest> newList) {
        this.requests = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for ride request items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemRideRequestBinding binding;

        public ViewHolder(ItemRideRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
