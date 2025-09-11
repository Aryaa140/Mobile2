package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ProspekAdapter extends RecyclerView.Adapter<ProspekAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Prospek> prospekList;
    private DatabaseHelper databaseHelper;

    public ProspekAdapter(Context context, ArrayList<Prospek> prospekList) {
        this.context = context;
        this.prospekList = prospekList;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prospek, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prospek prospek = prospekList.get(position);

        // Null safety check
        if (prospek != null) {
            holder.tvPenginput.setText("Penginput: " + (prospek.getPenginput() != null ? prospek.getPenginput() : "-"));
            holder.tvNama.setText("Nama: " + (prospek.getNama() != null ? prospek.getNama() : "-"));
            holder.tvEmail.setText("Email: " + (prospek.getEmail() != null ? prospek.getEmail() : "-"));
            holder.tvNoHp.setText("No. HP: " + (prospek.getNoHp() != null ? prospek.getNoHp() : "-"));
            holder.tvAlamat.setText("Alamat: " + (prospek.getAlamat() != null ? prospek.getAlamat() : "-"));
            holder.tvTanggal.setText("Tanggal: " + prospek.getTanggalBuatFormatted()); // Format tanggal lengkap
            holder.tvStatusNPWP.setText("Status NPWP: " + (prospek.getStatusNpwp() != null ? prospek.getStatusNpwp() : "-")); // TAMBAHAN: Status NPWP
            holder.tvStatusBPJS.setText("Status BPJS: " + (prospek.getStatusBpjs() != null ? prospek.getStatusBpjs() : "-")); // TAMBAHAN: Status BPJS

        } else {
            // Handle null data
            holder.tvPenginput.setText("Penginput: -");
            holder.tvNama.setText("Nama: -");
            holder.tvEmail.setText("Email: -");
            holder.tvNoHp.setText("No. HP: -");
            holder.tvAlamat.setText("Alamat: -");
            holder.tvTanggal.setText("Tanggal: -");
            holder.tvStatusNPWP.setText("Status NPWP: -"); // TAMBAHAN
            holder.tvStatusBPJS.setText("Status BPJS: -"); // TAMBAHAN
        }

        // Edit button click listener
        holder.btnEdit.setOnClickListener(v -> {
            if (prospek != null) {
                // Buka EditDataProspekActivity dengan membawa prospekId
                Intent intent = new Intent(context, EditDataProspekActivity.class);
                intent.putExtra("PROSPEK_ID", prospek.getProspekId());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Data tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            if (prospek != null) {
                showDeleteConfirmationDialog(prospek, position);
            } else {
                Toast.makeText(context, "Data tidak valid", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method untuk menampilkan dialog konfirmasi delete
    private void showDeleteConfirmationDialog(Prospek prospek, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Apakah Anda yakin ingin menghapus data prospek: " + prospek.getNama() + "?");

        builder.setPositiveButton("Ya", (dialog, which) -> {
            // Hapus data dari database
            boolean isDeleted = deleteProspek(prospek.getProspekId());

            if (isDeleted) {
                // Hapus dari list dan update UI
                prospekList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, prospekList.size());

                Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Tidak", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method untuk menghapus data dari database
    private boolean deleteProspek(int prospekId) {
        int result = databaseHelper.deleteProspek(prospekId);
        return result > 0;
    }

    @Override
    public int getItemCount() {
        return prospekList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPenginput, tvNama, tvEmail, tvNoHp, tvAlamat, tvTanggal, tvStatusNPWP, tvStatusBPJS;
        MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvStatusNPWP = itemView.findViewById(R.id.tvStatusNPWP); // TAMBAHAN
            tvStatusBPJS = itemView.findViewById(R.id.tvStatusBPJS); // TAMBAHAN
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Method untuk update data
    public void updateData(ArrayList<Prospek> newProspekList) {
        prospekList.clear();
        prospekList.addAll(newProspekList);
        notifyDataSetChanged();
    }

    // Method untuk refresh data dari database
    public void refreshData() {
        ArrayList<Prospek> newData = new ArrayList<>(databaseHelper.getAllProspek());
        updateData(newData);
    }

    // Clean up resources
    public void close() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}