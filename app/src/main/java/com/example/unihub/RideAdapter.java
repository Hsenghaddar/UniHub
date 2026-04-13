package com.example.unihub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.unihub.databinding.ItemRideBinding;
import java.util.List;

/**
 * Adapter for displaying a list of rides in a RecyclerView.
 *
 * This adapter manages the visualization of both ride offers and ride requests.
 * it highlights posts created by the current user and shows seat availability or status.
 */
public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private OnRideClickListener listener;
    private String currentUserUid;

    /**
     * Interface for handling click events on ride items.
     */
    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    /**
     * Constructor for RideAdapter.
     *
     * @param rideList The list of rides to display.
     * @param currentUserUid The UID of the current user (to highlight their own posts).
     * @param listener Callback for item clicks.
     */
    public RideAdapter(List<Ride> rideList, String currentUserUid, OnRideClickListener listener) {
        this.rideList = rideList;
        this.currentUserUid = currentUserUid;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRideBinding binding = ItemRideBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        holder.bind(ride, currentUserUid, listener);
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    /**
     * Updates the data set and refreshes the view.
     */
    public void updateList(List<Ride> newList) {
        this.rideList = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for ride items.
     */
    static class RideViewHolder extends RecyclerView.ViewHolder {
        private final ItemRideBinding binding;

        public RideViewHolder(ItemRideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Binds ride data to the UI components.
         *
         * @param ride The ride data model.
         * @param currentUserUid UID of the logged-in user.
         * @param listener Click listener for the item.
         */
        public void bind(Ride ride, String currentUserUid, OnRideClickListener listener) {
            binding.tvType.setText(ride.getType());
            binding.tvFromTo.setText(ride.getFromLocation() + " -> " + ride.getToLocation());
            binding.tvDateTime.setText(ride.getDate() + " at " + ride.getTime());
            
            // Format seat information based on post type
            if (ride.getType().equalsIgnoreCase("Request")) {
                binding.tvSeats.setText("Requested Seats: " + ride.getTotalSeats());
            } else {
                binding.tvSeats.setText("Available Seats: " + ride.getAvailableSeats() + " / " + ride.getTotalSeats());
            }

            // Show a label if this ride was posted by the current user
            if (ride.getUserUid().equals(currentUserUid)) {
                binding.tvMyPostLabel.setVisibility(View.VISIBLE);
            } else {
                binding.tvMyPostLabel.setVisibility(View.GONE);
            }

            // Show "Full" status if it's an offer with no seats left
            if (ride.getAvailableSeats() == 0 && ride.getType().equalsIgnoreCase("Offer")) {
                binding.tvStatus.setVisibility(View.VISIBLE);
            } else {
                binding.tvStatus.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onRideClick(ride));
        }
    }
}
