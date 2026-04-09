package com.example.unihub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.unihub.databinding.ItemRideBinding;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private OnRideClickListener listener;
    private String currentUserUid;

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

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

    public void updateList(List<Ride> newList) {
        this.rideList = newList;
        notifyDataSetChanged();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        private final ItemRideBinding binding;

        public RideViewHolder(ItemRideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Ride ride, String currentUserUid, OnRideClickListener listener) {
            binding.tvType.setText(ride.getType());
            binding.tvFromTo.setText(ride.getFromLocation() + " -> " + ride.getToLocation());
            binding.tvDateTime.setText(ride.getDate() + " at " + ride.getTime());
            
            if (ride.getType().equalsIgnoreCase("Request")) {
                binding.tvSeats.setText("Requested Seats: " + ride.getTotalSeats());
            } else {
                binding.tvSeats.setText("Available Seats: " + ride.getAvailableSeats() + " / " + ride.getTotalSeats());
            }

            if (ride.getUserUid().equals(currentUserUid)) {
                binding.tvMyPostLabel.setVisibility(View.VISIBLE);
            } else {
                binding.tvMyPostLabel.setVisibility(View.GONE);
            }

            if (ride.getAvailableSeats() == 0 && ride.getType().equalsIgnoreCase("Offer")) {
                binding.tvStatus.setVisibility(View.VISIBLE);
            } else {
                binding.tvStatus.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onRideClick(ride));
        }
    }
}
