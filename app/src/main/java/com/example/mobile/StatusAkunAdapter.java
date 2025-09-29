package com.example.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StatusAkunAdapter extends RecyclerView.Adapter<StatusAkunAdapter.StatusAkunViewHolder> {

    private Context context;
    private List<User> userList;
    private OnStatusAkunActionListener actionListener;

    public interface OnStatusAkunActionListener {
        void onAktifkanUser(int userId, String username);
        void onNonaktifkanUser(int userId, String username);
    }

    public StatusAkunAdapter(Context context, List<User> userList, OnStatusAkunActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public StatusAkunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status_akun, parent, false);
        return new StatusAkunViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusAkunViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class StatusAkunViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoNIP, tvUsername, tvDivisi, tvLevelAkun, tvStatusAkun;
        com.google.android.material.button.MaterialButton btnAktif, btnDelete;

        public StatusAkunViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoNIP = itemView.findViewById(R.id.tvNoNIP);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDivisi = itemView.findViewById(R.id.tvDivisi);
            tvLevelAkun = itemView.findViewById(R.id.tvLevelAkun);
            tvStatusAkun = itemView.findViewById(R.id.tvStatusAkun);
            btnAktif = itemView.findViewById(R.id.btnAktif);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(User user) {
            tvNoNIP.setText("No NIP : " + (user.getNip() != null ? user.getNip() : "-"));
            tvUsername.setText("Username : " + user.getUsername());
            tvDivisi.setText("Divisi : " + (user.getDivisi() != null ? user.getDivisi() : "-"));
            tvLevelAkun.setText("Akun Level : " + (user.getLevel() != null ? user.getLevel() : "-"));
            tvStatusAkun.setText("Status Akun : " + (user.getStatusAkun() != null ? user.getStatusAkun() : "-"));

            // Atur warna status
            if ("Aktif".equals(user.getStatusAkun())) {
                tvStatusAkun.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatusAkun.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }

            // Handle button aktif
            btnAktif.setOnClickListener(v -> {
                if ("Aktif".equals(user.getStatusAkun())) {
                    Toast.makeText(context, "Status " + user.getUsername() + " masih Aktif", Toast.LENGTH_SHORT).show();
                } else {
                    if (actionListener != null) {
                        actionListener.onAktifkanUser(user.getId(), user.getUsername());
                    }
                }
            });

            // Handle button nonaktif
            btnDelete.setOnClickListener(v -> {
                if ("Nonaktif".equals(user.getStatusAkun())) {
                    Toast.makeText(context, "Status " + user.getUsername() + " sudah Nonaktif", Toast.LENGTH_SHORT).show();
                } else {
                    if (actionListener != null) {
                        actionListener.onNonaktifkanUser(user.getId(), user.getUsername());
                    }
                }
            });

            // Update teks button berdasarkan status
            if ("Aktif".equals(user.getStatusAkun())) {
                btnAktif.setText("Aktif");
                btnAktif.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                btnDelete.setText("Nonaktifkan");
                btnDelete.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                btnAktif.setText("Aktifkan");
                btnAktif.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                btnDelete.setText("Nonaktif");
                btnDelete.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        }
    }
}