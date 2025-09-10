package com.example.mobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProyekAdapter extends RecyclerView.Adapter<ProyekAdapter.ProyekViewHolder> implements Filterable {

    private Context context;
    private List<DatabaseHelper.Proyek> proyekList;
    private List<DatabaseHelper.Proyek> proyekListFiltered;
    private DatabaseHelper databaseHelper;

    public ProyekAdapter(Context context, List<DatabaseHelper.Proyek> proyekList) {
        this.context = context;
        this.proyekList = proyekList;
        this.proyekListFiltered = proyekList;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ProyekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proyek, parent, false);
        return new ProyekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProyekViewHolder holder, int position) {
        DatabaseHelper.Proyek proyek = proyekListFiltered.get(position);

        holder.tvNamaProyek.setText("Nama Proyek: " + proyek.getNamaProyek());
        holder.tvLokasiProyek.setText("Lokasi Proyek: " + proyek.getLokasiProyek());
        holder.tvStatusProyek.setText("Status Proyek: " + proyek.getStatusProyek());

        holder.btnEdit.setOnClickListener(v -> {
            // Intent ke activity edit proyek
            Intent intent = new Intent(context, EditDataProyekActivity.class);
            intent.putExtra("PROYEK_ID", proyek.getProyekId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Tampilkan dialog konfirmasi hapus
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Proyek")
                    .setMessage("Apakah Anda yakin ingin menghapus proyek " + proyek.getNamaProyek() + "?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        int result = databaseHelper.deleteProyek(proyek.getProyekId());
                        if (result > 0) {
                            Toast.makeText(context, "Proyek berhasil dihapus", Toast.LENGTH_SHORT).show();
                            // Refresh data
                            proyekList.remove(proyek);
                            proyekListFiltered.remove(proyek);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Gagal menghapus proyek", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return proyekListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    proyekListFiltered = proyekList;
                } else {
                    List<DatabaseHelper.Proyek> filteredList = new ArrayList<>();
                    for (DatabaseHelper.Proyek row : proyekList) {
                        if (row.getNamaProyek().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    proyekListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = proyekListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                proyekListFiltered = (ArrayList<DatabaseHelper.Proyek>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ProyekViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaProyek, tvLokasiProyek, tvStatusProyek;
        MaterialButton btnEdit, btnDelete;

        public ProyekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaProyek = itemView.findViewById(R.id.tvNamaProyek);
            tvLokasiProyek = itemView.findViewById(R.id.tvLokasiProyek);
            tvStatusProyek = itemView.findViewById(R.id.tvStatusProyek);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}