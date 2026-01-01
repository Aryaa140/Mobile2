package com.example.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoriRealisasiAdapter extends RecyclerView.Adapter<HistoriRealisasiAdapter.ViewHolder> {

    private List<HistoriRealisasi> historiList;

    public HistoriRealisasiAdapter(List<HistoriRealisasi> historiList) {
        this.historiList = historiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_histori_realisasi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoriRealisasi histori = historiList.get(position);

        // Set data ke view
        holder.tvNamaUser.setText("Nama User: " + (histori.getNamaUser() != null ? histori.getNamaUser() : "-"));
        holder.tvTanggalRealisasi.setText("Tgl Realisasi: " + formatDateForDisplay(histori.getTanggalRealisasi()));
        holder.tvPenginput.setText("Penginput: " + (histori.getNamaPenginput() != null ? histori.getNamaPenginput() : "-"));
        holder.tvTanggalInput.setText("Tanggal Input: " + formatDateForDisplay(histori.getTanggalInput()));
    }

    @Override
    public int getItemCount() {
        return historiList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaUser, tvTanggalRealisasi, tvPenginput, tvTanggalInput;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaUser = itemView.findViewById(R.id.tvNamaUserHistori);
            tvTanggalRealisasi = itemView.findViewById(R.id.tvTanggalRealisasiHistori);
            tvPenginput = itemView.findViewById(R.id.tvPenginputHistori);
            tvTanggalInput = itemView.findViewById(R.id.tvTanggalInputHistori);
        }
    }

    private String formatDateForDisplay(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception ex) {
                return dateString;
            }
        }
    }

    public void updateData(List<HistoriRealisasi> newHistoriList) {
        historiList.clear();
        historiList.addAll(newHistoriList);
        notifyDataSetChanged();
    }
}