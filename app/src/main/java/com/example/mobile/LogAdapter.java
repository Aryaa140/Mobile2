package com.example.mobile;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private final Context context;
    private final List<LogEntry> originalList = new ArrayList<>();
    private final List<LogEntry> filteredList = new ArrayList<>();

    public LogAdapter(Context context, List<LogEntry> logList) {
        this.context = context;
        if (logList != null) {
            this.originalList.addAll(logList);
            this.filteredList.addAll(logList);
        }
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = filteredList.get(position);

        // Warna badge aksi (tetap)
        int color;
        switch (entry.getAksi() != null ? entry.getAksi() : "") {
            case "CREATE":  color = Color.parseColor("#2E7D32"); break;
            case "UPDATE":  color = Color.parseColor("#F9A825"); break;
            case "DELETE":  color = Color.parseColor("#C62828"); break;
            case "VIEW":    color = Color.parseColor("#1565C0"); break;
            case "BOOKING": color = Color.parseColor("#6A1B9A"); break;
            case "LOGIN":   color = Color.parseColor("#00838F"); break;
            case "LOGOUT":  color = Color.parseColor("#4E342E"); break;
            default:        color = Color.GRAY;
        }
        holder.tvAksi.setText(entry.getAksi() != null ? entry.getAksi() : "-");
        holder.tvAksi.setBackgroundColor(color);

        holder.tvFitur.setText(entry.getFitur() != null ? entry.getFitur() : "-");
        holder.tvUsername.setText("User: " + (entry.getUsername() != null ? entry.getUsername() : "-"));
        holder.tvLevel.setText("Level: " + (entry.getLevel() != null ? entry.getLevel() : "-"));
        holder.tvDeskripsi.setText(entry.getDeskripsi() != null ? entry.getDeskripsi() : "-");

        // ===== TANGGAL LENGKAP =====
        // Format: "Kamis, 15 Oktober 2026 20:26:52.123 +07:00"
        StringBuilder waktu = new StringBuilder();
        if (entry.getHari() != null)         waktu.append(entry.getHari()).append(", ");
        if (entry.getDeviceTimeString() != null) {
            waktu.append(entry.getDeviceTimeString());
        } else if (entry.getTanggalLog() != null) {
            waktu.append(entry.getTanggalLog());
        }
        if (entry.getTimezoneOffset() != null) {
            waktu.append(" ").append(entry.getTimezoneOffset());
        }
        if (entry.getTimezone() != null) {
            waktu.append(" (").append(entry.getTimezone()).append(")");
        }
        holder.tvTanggal.setText(waktu.toString());

        // Extra: selisih device vs server
        StringBuilder extra = new StringBuilder();
        if (entry.getActivityName() != null && !entry.getActivityName().isEmpty()) {
            extra.append("Activity: ").append(entry.getActivityName());
        }
        if (entry.getDeviceTimeMillis() != null && entry.getServerTimeMicros() != null) {
            long selisih = entry.getServerTimeMicros() - entry.getDeviceTimeMillis();
            if (extra.length() > 0) extra.append(" | ");
            extra.append("Δ device-server: ").append(selisih).append(" ms");
        }
        if (entry.getIpAddress() != null && !entry.getIpAddress().isEmpty()) {
            if (extra.length() > 0) extra.append(" | ");
            extra.append("IP: ").append(entry.getIpAddress());
        }
        holder.tvExtra.setText(extra.toString());
        holder.tvExtra.setVisibility(extra.length() > 0 ? View.VISIBLE : View.GONE);
    }
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // ===== Update data penuh (dipanggil setelah load dari server) =====
    public void updateData(List<LogEntry> newList) {
        originalList.clear();
        filteredList.clear();
        if (newList != null) {
            originalList.addAll(newList);
            filteredList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    // ===== Filter berdasarkan keyword (username, fitur, deskripsi, aksi) =====
    public void filter(String query) {
        filteredList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String q = query.toLowerCase().trim();
            for (LogEntry entry : originalList) {
                boolean match =
                        (entry.getUsername() != null && entry.getUsername().toLowerCase().contains(q))
                                || (entry.getFitur()    != null && entry.getFitur().toLowerCase().contains(q))
                                || (entry.getAksi()     != null && entry.getAksi().toLowerCase().contains(q))
                                || (entry.getDeskripsi()!= null && entry.getDeskripsi().toLowerCase().contains(q))
                                || (entry.getLevel()    != null && entry.getLevel().toLowerCase().contains(q));
                if (match) filteredList.add(entry);
            }
        }
        notifyDataSetChanged();
    }

    // ===== ViewHolder =====
    public static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvAksi, tvFitur, tvUsername, tvLevel, tvDeskripsi, tvTanggal, tvExtra;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAksi      = itemView.findViewById(R.id.tvAksi);
            tvFitur     = itemView.findViewById(R.id.tvFitur);
            tvUsername  = itemView.findViewById(R.id.tvUsername);
            tvLevel     = itemView.findViewById(R.id.tvLevel);
            tvDeskripsi = itemView.findViewById(R.id.tvDeskripsi);
            tvTanggal   = itemView.findViewById(R.id.tvTanggal);
            tvExtra     = itemView.findViewById(R.id.tvExtra);
        }
    }
}