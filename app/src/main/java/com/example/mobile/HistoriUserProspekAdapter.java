package com.example.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoriUserProspekAdapter extends RecyclerView.Adapter<HistoriUserProspekAdapter.ViewHolder> {

    private List<HistoriUserProspek> historiList;

    public HistoriUserProspekAdapter(List<HistoriUserProspek> historiList) {
        this.historiList = historiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_histori_utj, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoriUserProspek histori = historiList.get(position);

        // Set data ke view dengan null safety
        holder.tvPenginput.setText("Penginput: " + safeString(histori.getNamaPenginput()));
        holder.tvTanggal.setText("Tanggal: " + formatDate(histori.getTanggalPerubahan()));
        holder.tvNama.setText("Nama: " + safeString(histori.getNamaUser()));
        holder.tvStatusNPWP.setText("Status NPWP: " + safeString(histori.getStatusNpwp()));
        holder.tvStatusBPJS.setText("Status BPJS: " + safeString(histori.getStatusBpjs()));
        holder.tvStatusSPS.setText("Status SPS: " + safeString(histori.getStatusSps())); // Ganti DP
        holder.tvEmail.setText("Email: " + safeString(histori.getEmail()));
        holder.tvNoHp.setText("No. HP: " + safeString(histori.getNoHp()));
        holder.tvProyek.setText("Proyek: " + safeString(histori.getProyek()));
        holder.tvHunian.setText("Hunian: " + safeString(histori.getHunian()));
        holder.tvTipeHunian.setText("Tipe Hunian: " + safeString(histori.getTipeHunian()));
        holder.tvAlamat.setText("Alamat: " + safeString(histori.getAlamat()));

        // Set background color based on change type
        if ("UPDATE".equals(histori.getTipePerubahan())) {
            holder.itemView.setBackgroundColor(0x30FFA500); // Orange transparan
        } else if ("DELETE".equals(histori.getTipePerubahan())) {
            holder.itemView.setBackgroundColor(0x30FF0000); // Merah transparan
        } else {
            holder.itemView.setBackgroundColor(0x00000000); // Transparan
        }
    }

    @Override
    public int getItemCount() {
        return historiList != null ? historiList.size() : 0;
    }

    // Helper methods
    private String safeString(String value) {
        return value != null && !value.isEmpty() ? value : "-";
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString != null ? dateString : "-";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPenginput, tvTanggal, tvNama, tvStatusNPWP, tvStatusBPJS, tvStatusSPS,
                tvEmail, tvNoHp, tvProyek, tvHunian, tvTipeHunian, tvAlamat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvStatusNPWP = itemView.findViewById(R.id.tvStatusNPWP);
            tvStatusBPJS = itemView.findViewById(R.id.tvStatusBPJS);
            tvStatusSPS = itemView.findViewById(R.id.tvStatusSPS);      // TextView baru untuk Status SPS
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvProyek = itemView.findViewById(R.id.tvProyek);
            tvHunian = itemView.findViewById(R.id.tvHunian);
            tvTipeHunian = itemView.findViewById(R.id.tvTipeHunian);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
        }
    }

    public void updateData(List<HistoriUserProspek> newData) {
        this.historiList = newData;
        notifyDataSetChanged();
    }
}