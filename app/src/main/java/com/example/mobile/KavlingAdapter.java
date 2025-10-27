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

public class KavlingAdapter extends RecyclerView.Adapter<KavlingAdapter.KavlingViewHolder> implements Filterable {

    private static final String TAG = "KavlingAdapter";
    private List<KavlingWithInfo> kavlingList;
    private List<KavlingWithInfo> kavlingListFiltered;
    private OnItemClickListener onItemClickListener;

    // Interface untuk callback
    public interface OnItemClickListener {
        void onEditClick(KavlingWithInfo kavling);
        void onDeleteClick(KavlingWithInfo kavling);
    }

    // Setter untuk listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public KavlingAdapter(List<KavlingWithInfo> kavlingList) {
        this.kavlingList = kavlingList != null ? new ArrayList<>(kavlingList) : new ArrayList<>();
        this.kavlingListFiltered = new ArrayList<>(this.kavlingList);
        Log.d(TAG, "Adapter created with " + this.kavlingList.size() + " items");
    }

    @NonNull
    @Override
    public KavlingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "Creating view holder");
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_kavling, parent, false);
            return new KavlingViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout: " + e.getMessage());
            throw new RuntimeException("Could not inflate layout", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull KavlingViewHolder holder, int position) {
        if (position < kavlingListFiltered.size()) {
            KavlingWithInfo kavling = kavlingListFiltered.get(position);
            Log.d(TAG, "Binding position " + position + ": " + kavling.getTipeHunian());

            holder.tvNamaTipeHunian.setText("Tipe Hunian: " + kavling.getTipeHunian());
            holder.tvRefrensiProyek.setText("Proyek: " + kavling.getProyek());
            holder.tvJumlahTipeHunian.setText("Hunian: " + kavling.getHunian());
            holder.tvStatusPenjualan.setText("Status: " + kavling.getStatusPenjualan());

            // Sembunyikan tvStatusStok karena tidak ada data kode_kavling
            holder.tvStatusStok.setVisibility(View.GONE);

            // Setup click listener untuk tombol Edit
            holder.btnEdit.setOnClickListener(v -> {
                Log.d(TAG, "Edit button clicked for: " + kavling.getTipeHunian());
                if (onItemClickListener != null) {
                    onItemClickListener.onEditClick(kavling);
                }
            });

            // Setup click listener untuk tombol Delete
            holder.btnDelete.setOnClickListener(v -> {
                Log.d(TAG, "Delete button clicked for: " + kavling.getTipeHunian());
                if (onItemClickListener != null) {
                    onItemClickListener.onDeleteClick(kavling);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = kavlingListFiltered.size();
        Log.d(TAG, "Item count: " + count);
        return count;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<KavlingWithInfo> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(kavlingList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (KavlingWithInfo kavling : kavlingList) {
                        if (kavling.getTipeHunian().toLowerCase().contains(filterPattern) ||
                                kavling.getProyek().toLowerCase().contains(filterPattern) ||
                                kavling.getHunian().toLowerCase().contains(filterPattern) ||
                                kavling.getStatusPenjualan().toLowerCase().contains(filterPattern)) {
                            filteredList.add(kavling);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                kavlingListFiltered.clear();
                if (results.values != null) {
                    kavlingListFiltered.addAll((List<KavlingWithInfo>) results.values);
                }
                Log.d(TAG, "Filter applied, now showing " + kavlingListFiltered.size() + " items");
                notifyDataSetChanged();
            }
        };
    }

    public void updateData(List<KavlingWithInfo> newData) {
        Log.d(TAG, "Updating data with " + (newData != null ? newData.size() : "null") + " items");
        this.kavlingList.clear();
        if (newData != null) {
            this.kavlingList.addAll(newData);
        }
        this.kavlingListFiltered.clear();
        this.kavlingListFiltered.addAll(this.kavlingList);
        Log.d(TAG, "Data updated, now has " + this.kavlingList.size() + " items");
        notifyDataSetChanged();
    }

    public static class KavlingViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaTipeHunian, tvRefrensiProyek, tvJumlahTipeHunian, tvStatusPenjualan, tvStatusStok;
        com.google.android.material.button.MaterialButton btnEdit, btnDelete;

        public KavlingViewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                tvNamaTipeHunian = itemView.findViewById(R.id.tvNamaTipeHunian);
                tvRefrensiProyek = itemView.findViewById(R.id.tvRefrensiProyek);
                tvJumlahTipeHunian = itemView.findViewById(R.id.tvJumlahTipeHunian);
                tvStatusPenjualan = itemView.findViewById(R.id.tvStatusPenjualan);
                tvStatusStok = itemView.findViewById(R.id.tvStatusStok);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);

                Log.d("KavlingViewHolder", "All views found successfully");
            } catch (Exception e) {
                Log.e("KavlingViewHolder", "Error finding views: " + e.getMessage());
            }
        }
    }
}