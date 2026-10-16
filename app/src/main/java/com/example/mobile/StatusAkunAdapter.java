package com.example.mobile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StatusAkunAdapter extends RecyclerView.Adapter<StatusAkunAdapter.StatusAkunViewHolder> {

    private Context context;
    private List<User> userList;
    private OnStatusAkunActionListener actionListener;

    // ✅ level user yang sedang login (dikirim dari Activity)
    private String currentUserLevel = "";

    public interface OnStatusAkunActionListener {
        void onAktifkanUser(int userId, String username);
        void onNonaktifkanUser(int userId, String username);
    }

    public StatusAkunAdapter(Context context, List<User> userList, OnStatusAkunActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.actionListener = listener;
    }

    // ✅ setter supaya Activity bisa kirim level user login
    public void setCurrentUserLevel(String level) {
        this.currentUserLevel = (level != null) ? level : "";
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
        return userList != null ? userList.size() : 0;
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

            // ✅ Atur warna status (pakai ContextCompat agar tidak deprecated)
            if ("Aktif".equals(user.getStatusAkun())) {
                tvStatusAkun.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else {
                tvStatusAkun.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }

            // ✅ cek apakah user ini adalah Admin Operator
            String userLevel = (user.getLevel() != null)
                    ? user.getLevel().toLowerCase().trim()
                    : "";
            boolean isAdminOperator = userLevel.equals("admin operator");

            // ✅ cek apakah yang login adalah Admin Pembuat
            boolean isCurrentAdminPembuat =
                    "admin pembuat".equalsIgnoreCase(currentUserLevel);

            // ✅ hak akses toggle
            // - Admin Operator → HANYA Admin Pembuat
            // - Selain itu (Marketing Inhouse/Freelance) → semua admin boleh
            boolean canToggle;
            if (isAdminOperator) {
                canToggle = isCurrentAdminPembuat;
            } else {
                canToggle = true;
            }

            // Handle button aktif
            btnAktif.setOnClickListener(v -> {
                if (isAdminOperator && !isCurrentAdminPembuat) {
                    Toast.makeText(context,
                            "Hanya Admin Pembuat yang dapat mengubah status Admin Operator",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("Aktif".equals(user.getStatusAkun())) {
                    Toast.makeText(context, "Status " + user.getUsername() + " masih Aktif",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (actionListener != null) {
                        actionListener.onAktifkanUser(user.getId(), user.getUsername());
                    }
                }
            });

            // Handle button nonaktif
            btnDelete.setOnClickListener(v -> {
                if (isAdminOperator && !isCurrentAdminPembuat) {
                    Toast.makeText(context,
                            "Hanya Admin Pembuat yang dapat mengubah status Admin Operator",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if ("Nonaktif".equals(user.getStatusAkun())) {
                    Toast.makeText(context, "Status " + user.getUsername() + " sudah Nonaktif",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (actionListener != null) {
                        actionListener.onNonaktifkanUser(user.getId(), user.getUsername());
                    }
                }
            });

            // ✅ Update teks + warna tombol berdasarkan status
            //    Pakai setBackgroundTintList (bukan setBackgroundColor) untuk MaterialButton
            if ("Aktif".equals(user.getStatusAkun())) {
                btnAktif.setText("Aktif");
                btnAktif.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, android.R.color.darker_gray)));

                btnDelete.setText("Nonaktifkan");
                btnDelete.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark)));
            } else {
                btnAktif.setText("Aktifkan");
                btnAktif.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, android.R.color.holo_blue_light)));

                btnDelete.setText("Nonaktif");
                btnDelete.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, android.R.color.darker_gray)));
            }

            // ✅ Visual feedback kalau tombol dikunci
            if (!canToggle) {
                btnAktif.setEnabled(false);
                btnDelete.setEnabled(false);
                btnAktif.setAlpha(0.4f);
                btnDelete.setAlpha(0.4f);
            } else {
                btnAktif.setEnabled(true);
                btnDelete.setEnabled(true);
                btnAktif.setAlpha(1f);
                btnDelete.setAlpha(1f);
            }
        }
    }
}