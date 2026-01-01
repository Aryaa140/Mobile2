package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RealisasiAdapter extends RecyclerView.Adapter<RealisasiAdapter.ViewHolder> {

    private List<Realisasi> realisasiList;
    private String userLevel;
    private Context context;

    public RealisasiAdapter(List<Realisasi> realisasiList, String userLevel) {
        this.realisasiList = realisasiList;
        this.userLevel = userLevel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_realisasi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Realisasi realisasi = realisasiList.get(position);

        holder.tvPenginput.setText("Penginput: " + (realisasi.getNamaPenginput() != null ? realisasi.getNamaPenginput() : "-"));
        holder.tvTanggalRealisasi.setText("Tgl Realisasi: " + (realisasi.getTanggalRealisasi() != null ? formatDate(realisasi.getTanggalRealisasi()) : "-"));
        holder.tvNama.setText("Nama: " + (realisasi.getNamaUser() != null ? realisasi.getNamaUser() : "-"));
        holder.tvEmail.setText("Email: " + (realisasi.getEmail() != null ? realisasi.getEmail() : "-"));
        holder.tvNoHp.setText("No. HP: " + (realisasi.getNoHp() != null ? realisasi.getNoHp() : "-"));
        holder.tvAlamat.setText("Alamat: " + (realisasi.getAlamat() != null ? realisasi.getAlamat() : "-"));
        holder.tvHunian.setText("Hunian: " + (realisasi.getHunian() != null ? realisasi.getHunian() : "-"));
        holder.tvTipeHunian.setText("Tipe Hunian: " + (realisasi.getTipeHunian() != null ? realisasi.getTipeHunian() : "-"));
        holder.tvStatusBPJS.setText("Status BPJS: " + (realisasi.getStatusBpjs() != null ? realisasi.getStatusBpjs() : "-"));
        holder.tvStatusNPWP.setText("Status NPWP: " + (realisasi.getStatusNpwp() != null ? realisasi.getStatusNpwp() : "-"));

        String formattedDP = "DP: Rp " + formatCurrency(realisasi.getDp());
        holder.tvJumlahDP.setText(formattedDP);

        // Setup button Edit (kode yang sudah benar - tidak diubah)
        if (holder.btnEdit != null) {
            if ("Admin".equals(userLevel)) {
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnEdit.setOnClickListener(v -> {
                    // Panggil method edit di activity
                    if (context instanceof LihatDataRealisasiActivity) {
                        ((LihatDataRealisasiActivity) context).showEditRealisasiDialog(realisasi);
                    }
                });
            } else {
                holder.btnEdit.setVisibility(View.GONE);
            }
        }

        // Setup button Histori (tambahan baru)
        if (holder.btnHistori != null) {
            holder.btnHistori.setVisibility(View.VISIBLE);
            holder.btnHistori.setOnClickListener(v -> {
                // Pindah ke activity histori
                Intent intent = new Intent(context, HistoriRealisasiActivity.class);
                intent.putExtra("ID_REALISASI", realisasi.getId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return realisasiList != null ? realisasiList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPenginput, tvTanggalRealisasi, tvNama, tvEmail, tvNoHp, tvAlamat, tvHunian, tvTipeHunian, tvStatusBPJS, tvStatusNPWP, tvJumlahDP;
        MaterialButton btnEdit;
        MaterialButton btnHistori; // Tambahan button histori

        public ViewHolder(View itemView) {
            super(itemView);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvTanggalRealisasi = itemView.findViewById(R.id.tvTanggalRealisasi);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvHunian = itemView.findViewById(R.id.tvHunian);
            tvTipeHunian = itemView.findViewById(R.id.tvTipeHunian);
            tvStatusBPJS = itemView.findViewById(R.id.tvStatusBPJS);
            tvStatusNPWP = itemView.findViewById(R.id.tvStatusNPWP);
            tvJumlahDP = itemView.findViewById(R.id.tvJumlahDP);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnHistori = itemView.findViewById(R.id.btnHistori); // Inisialisasi button histori
        }
    }

    private String formatCurrency(int amount) {
        try {
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            formatter.applyPattern("#,###");
            return formatter.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                java.util.Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception ex) {
                return dateString;
            }
        }
    }
}