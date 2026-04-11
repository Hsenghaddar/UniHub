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

public class RideRequestsAdapter extends RecyclerView.Adapter<RideRequestsAdapter.ViewHolder> {

    private List<RideRequest> requests;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onApprove(RideRequest request);
        void onReject(RideRequest request);
        void onContact(RideRequest request);
    }

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

    public void updateList(List<RideRequest> newList) {
        this.requests = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemRideRequestBinding binding;

        public ViewHolder(ItemRideRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
