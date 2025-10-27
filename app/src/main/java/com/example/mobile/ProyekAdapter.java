package com.example.mobile;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProyekAdapter extends RecyclerView.Adapter<ProyekAdapter.ProyekViewHolder> {

    private static final String TAG = "ProyekAdapter";
    private List<ProyekWithInfo> proyekList;
    private OnItemClickListener onItemClickListener;

    // Interface untuk callback
    public interface OnItemClickListener {
        void onEditClick(ProyekWithInfo proyek);
        void onDeleteClick(ProyekWithInfo proyek);
    }

    // Setter untuk listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public ProyekAdapter(List<ProyekWithInfo> proyekList) {
        this.proyekList = proyekList != null ? new ArrayList<>(proyekList) : new ArrayList<>();
        Log.d(TAG, "Adapter created with " + this.proyekList.size() + " items");
    }

    @NonNull
    @Override
    public ProyekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating view holder");
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_proyek, parent, false);
            return new ProyekViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout: " + e.getMessage());
            throw new RuntimeException("Could not inflate layout", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ProyekViewHolder holder, int position) {
        if (position < proyekList.size()) {
            ProyekWithInfo proyek = proyekList.get(position);
            Log.d(TAG, "Binding position " + position + ": " + proyek.getNamaProyek());

            holder.tvNamaProyek.setText("Nama Proyek: " + proyek.getNamaProyek());
            holder.tvJumlahHunian.setText("Jumlah Hunian: " + proyek.getJumlahHunian());

            String statusStok = proyek.getJumlahStok() > 0 ?
                    "Stok Tersedia (" + proyek.getJumlahStok() + " kavling)" : "Stok Habis";
            holder.tvStatusStok.setText("Status Stok: " + statusStok);

            // TAMPILKAN tombol Edit dan Delete
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            // Setup click listener untuk tombol Edit dengan callback
            holder.btnEdit.setOnClickListener(v -> {
                Log.d(TAG, "Edit button clicked for: " + proyek.getNamaProyek());
                if (onItemClickListener != null) {
                    onItemClickListener.onEditClick(proyek);
                }
            });

            // Setup click listener untuk tombol Delete dengan callback
            holder.btnDelete.setOnClickListener(v -> {
                Log.d(TAG, "Delete button clicked for: " + proyek.getNamaProyek());
                if (onItemClickListener != null) {
                    onItemClickListener.onDeleteClick(proyek);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = proyekList.size();
        Log.d(TAG, "Item count: " + count);
        return count;
    }

    public void updateData(List<ProyekWithInfo> newData) {
        Log.d(TAG, "Updating data with " + (newData != null ? newData.size() : "null") + " items");
        this.proyekList.clear();
        if (newData != null) {
            this.proyekList.addAll(newData);
        }
        Log.d(TAG, "Data updated, now has " + this.proyekList.size() + " items");
        notifyDataSetChanged();
    }

    public static class ProyekViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaProyek, tvJumlahHunian, tvStatusStok;
        com.google.android.material.button.MaterialButton btnEdit, btnDelete;

        public ProyekViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views dengan try-catch
            try {
                tvNamaProyek = itemView.findViewById(R.id.tvNamaProyek);
                tvJumlahHunian = itemView.findViewById(R.id.tvJumlahHunian);
                tvStatusStok = itemView.findViewById(R.id.tvStatusStok);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);

                Log.d("ProyekViewHolder", "All views found successfully");
            } catch (Exception e) {
                Log.e("ProyekViewHolder", "Error finding views: " + e.getMessage());
            }
        }
    }
}