package com.example.unihub;

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
        holder.binding.tvRequestStatus.setText("Status: " + request.getStatus());

        if (request.getStatus().equalsIgnoreCase("pending")) {
            holder.binding.layoutActions.setVisibility(View.VISIBLE);
        } else {
            holder.binding.layoutActions.setVisibility(View.GONE);
        }

        holder.binding.btnApprove.setOnClickListener(v -> listener.onApprove(request));
        holder.binding.btnReject.setOnClickListener(v -> listener.onReject(request));
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
