package com.example.mobile;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HunianAdapter extends RecyclerView.Adapter<HunianAdapter.HunianViewHolder> implements Filterable {

    private static final String TAG = "HunianAdapter";
    private List<HunianWithInfo> hunianList;
    private List<HunianWithInfo> hunianListFiltered;
    private OnItemClickListener onItemClickListener;

    // Interface untuk callback
    public interface OnItemClickListener {
        void onEditClick(HunianWithInfo hunian);
        void onDeleteClick(HunianWithInfo hunian);
    }

    // Setter untuk listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public HunianAdapter(List<HunianWithInfo> hunianList) {
        this.hunianList = hunianList != null ? new ArrayList<>(hunianList) : new ArrayList<>();
        this.hunianListFiltered = new ArrayList<>(this.hunianList);
        Log.d(TAG, "Adapter created with " + this.hunianList.size() + " items");
    }

    @NonNull
    @Override
    public HunianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating view holder");
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hunian, parent, false);
            return new HunianViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout: " + e.getMessage());
            throw new RuntimeException("Could not inflate layout", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HunianViewHolder holder, int position) {
        if (position < hunianListFiltered.size()) {
            HunianWithInfo hunian = hunianListFiltered.get(position);
            Log.d(TAG, "Binding position " + position + ": " + hunian.getNamaHunian());

            holder.tvNamaHunian.setText("Nama Hunian: " + hunian.getNamaHunian());
            holder.tvRefrensiProyek.setText("Referensi Proyek: " + hunian.getNamaProyek());
            holder.tvJumlahTipeHunian.setText("Jumlah Tipe Hunian: " + hunian.getJumlahTipeHunian());

            String statusStok = hunian.getJumlahStok() > 0 ?
                    "Stok Tersedia (" + hunian.getJumlahStok() + " kavling)" : "Stok Habis";
            holder.tvStatusStok.setText("Status Stok: " + statusStok);

            // TAMPILKAN tombol Edit dan Delete
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            // Setup click listener untuk tombol Edit dengan callback
            holder.btnEdit.setOnClickListener(v -> {
                Log.d(TAG, "Edit button clicked for: " + hunian.getNamaHunian());
                if (onItemClickListener != null) {
                    onItemClickListener.onEditClick(hunian);
                }
            });

            // Setup click listener untuk tombol Delete dengan callback
            holder.btnDelete.setOnClickListener(v -> {
                Log.d(TAG, "Delete button clicked for: " + hunian.getNamaHunian());
                if (onItemClickListener != null) {
                    onItemClickListener.onDeleteClick(hunian);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = hunianListFiltered.size();
        Log.d(TAG, "Item count: " + count);
        return count;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<HunianWithInfo> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(hunianList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (HunianWithInfo hunian : hunianList) {
                        if (hunian.getNamaHunian().toLowerCase().contains(filterPattern)) {
                            filteredList.add(hunian);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                hunianListFiltered.clear();
                if (results.values != null) {
                    hunianListFiltered.addAll((List<HunianWithInfo>) results.values);
                }
                Log.d(TAG, "Filter applied, now showing " + hunianListFiltered.size() + " items");
                notifyDataSetChanged();
            }
        };
    }

    public void updateData(List<HunianWithInfo> newData) {
        Log.d(TAG, "Updating data with " + (newData != null ? newData.size() : "null") + " items");
        this.hunianList.clear();
        if (newData != null) {
            this.hunianList.addAll(newData);
        }
        this.hunianListFiltered.clear();
        this.hunianListFiltered.addAll(this.hunianList);
        Log.d(TAG, "Data updated, now has " + this.hunianList.size() + " items");
        notifyDataSetChanged();
    }

    public static class HunianViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaHunian, tvRefrensiProyek, tvJumlahTipeHunian, tvStatusStok;
        com.google.android.material.button.MaterialButton btnEdit, btnDelete;

        public HunianViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find views dengan try-catch
            try {
                tvNamaHunian = itemView.findViewById(R.id.tvNamaHunian);
                tvRefrensiProyek = itemView.findViewById(R.id.tvRefrensiProyek);
                tvJumlahTipeHunian = itemView.findViewById(R.id.tvJumlahTipeHunian);
                tvStatusStok = itemView.findViewById(R.id.tvStatusStok);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);

                Log.d("HunianViewHolder", "All views found successfully");
            } catch (Exception e) {
                Log.e("HunianViewHolder", "Error finding views: " + e.getMessage());
            }
        }
    }
}