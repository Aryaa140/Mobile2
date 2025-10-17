package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProspekAdapter2 extends RecyclerView.Adapter<ProspekAdapter2.ViewHolder> {

    private final Context context;
    private final ArrayList<Prospek2> prospekList;
    private final String userLevel;

    public ProspekAdapter2(Context context, ArrayList<Prospek2> prospekList, String userLevel) {
        this.context = context;
        this.prospekList = prospekList;
        this.userLevel = userLevel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prospek, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Prospek2 prospek = prospekList.get(position);

        // Binding data ke card
        holder.tvNamaProspek.setText("Nama Prospek: " + safeString(prospek.getNamaProspek()));
        holder.tvNamaPenginput.setText("Penginput: " + safeString(prospek.getNamaPenginput()));
        holder.tvTanggalInput.setText("Tanggal: " + safeString(prospek.getTanggalInput()));
        holder.tvEmail.setText("Email: " + safeString(prospek.getEmail()));
        holder.tvNoHp.setText("No. HP (+62): " + safeString(prospek.getNoHp()));
        holder.tvAlamat.setText("Alamat: " + safeString(prospek.getAlamat()));
        holder.tvStatusNPWP.setText("NPWP: " + safeString(prospek.getStatusNpwp()));
        holder.tvStatusBPJS.setText("BPJS: " + safeString(prospek.getStatusBpjs()));

        // PERBAIKAN: Logika tombol edit/delete untuk Admin dan Operator
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("username", "");

        // PERBAIKAN: Gunakan level dari parameter DAN dari SharedPreferences sebagai fallback
        String savedLevel = sharedPreferences.getString("level", "Operator");
        String savedUserLevel = sharedPreferences.getString("user_level", "Operator");

        // Debug logging yang lebih detail
        Log.d("ProspekAdapter2", "=== DEBUG ADAPTER ===");
        Log.d("ProspekAdapter2", "Current User: " + currentUsername);
        Log.d("ProspekAdapter2", "User Level from param: " + userLevel);
        Log.d("ProspekAdapter2", "User Level from prefs (level): " + savedLevel);
        Log.d("ProspekAdapter2", "User Level from prefs (user_level): " + savedUserLevel);
        Log.d("ProspekAdapter2", "Prospek Owner: " + prospek.getNamaPenginput());
        Log.d("ProspekAdapter2", "Prospek ID: " + prospek.getIdProspek());
        Log.d("ProspekAdapter2", "All SharedPrefs: " + sharedPreferences.getAll().toString());

        // Tentukan level yang efektif - BUAT FINAL
        final String effectiveUserLevel; // PERBAIKAN: Deklarasikan sebagai final
        String tempUserLevel = userLevel;
        if (tempUserLevel == null || tempUserLevel.isEmpty()) {
            tempUserLevel = savedLevel;
            if (tempUserLevel == null || tempUserLevel.isEmpty()) {
                tempUserLevel = savedUserLevel;
            }
        }
        effectiveUserLevel = tempUserLevel; // PERBAIKAN: Assign ke final variable

        Log.d("ProspekAdapter2", "Effective User Level: " + effectiveUserLevel);

        // FORCE ADMIN MODE UNTUK TESTING - HAPUS BARIS INI JIKA SUDAH BERHASIL
        // effectiveUserLevel = "Admin"; // INI TIDAK BISA KARENA SUDAH FINAL
        // Log.d("ProspekAdapter2", "FORCE ADMIN MODE ACTIVATED");

        // Admin bisa edit/delete SEMUA data
        // Operator hanya bisa edit/delete data miliknya sendiri
        final boolean canEdit; // PERBAIKAN: Buat final
        if ("Admin".equals(effectiveUserLevel)) {
            // Admin dapat mengelola semua data
            canEdit = true;
            Log.d("ProspekAdapter2", "ADMIN MODE - Tombol akan ditampilkan untuk semua data");
        } else {
            // Non-admin hanya bisa mengelola data miliknya sendiri
            canEdit = currentUsername.equals(prospek.getNamaPenginput());
            Log.d("ProspekAdapter2", "OPERATOR MODE - can edit: " + canEdit);
        }

        // Set visibility tombol
        holder.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        Log.d("ProspekAdapter2", "Button Visibility - Edit: " + (canEdit ? "VISIBLE" : "GONE") +
                ", Delete: " + (canEdit ? "VISIBLE" : "GONE"));

        // Tambahkan indicator untuk data milik operator (opsional)
        if ("Admin".equals(effectiveUserLevel) && !currentUsername.equals(prospek.getNamaPenginput())) {
            // Jika Admin melihat data orang lain, beri indicator
            holder.tvNamaPenginput.setText("Penginput: " + safeString(prospek.getNamaPenginput()) );
        }

        // PERBAIKAN: Gunakan final variables dalam lambda
        final String finalNamaPenginput = prospek.getNamaPenginput();
        final String finalNamaProspek = prospek.getNamaProspek();
        final int finalPosition = position;

        holder.btnEdit.setOnClickListener(v -> {
            if (!canEdit) {
                Toast.makeText(context, "Anda tidak memiliki akses untuk mengedit data ini", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, EditDataProspekActivity.class);

            // Kirim semua data ke EditDataProspekActivity
            intent.putExtra("PROSPEK_ID", prospek.getIdProspek());
            intent.putExtra("NAMA_PENGINPUT", finalNamaPenginput);
            intent.putExtra("NAMA_PROSPEK", finalNamaProspek);
            intent.putExtra("EMAIL", prospek.getEmail());
            intent.putExtra("NO_HP", prospek.getNoHp());
            intent.putExtra("ALAMAT", prospek.getAlamat());
            intent.putExtra("REFERENSI_PROYEK", prospek.getReferensiProyek());
            intent.putExtra("STATUS_NPWP", prospek.getStatusNpwp());
            intent.putExtra("STATUS_BPJS", prospek.getStatusBpjs());

            // Tambahkan flag untuk menandai bahwa ini edit oleh Admin
            if ("Admin".equals(effectiveUserLevel)) {
                intent.putExtra("IS_ADMIN_EDIT", true);
            }

            context.startActivity(intent);
        });

        // Tombol Hapus
        holder.btnDelete.setOnClickListener(v -> {
            if (!canEdit) {
                Toast.makeText(context, "Anda tidak memiliki akses untuk menghapus data ini", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteProspek(finalNamaPenginput, finalNamaProspek, finalPosition, effectiveUserLevel);
        });
    }

    private void deleteProspek(String namaPenginput, String namaProspek, int position, String effectiveUserLevel) {
        // Konfirmasi hapus
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Apakah Anda yakin ingin menghapus data prospek '" + namaProspek + "'?");

        // PERBAIKAN: Buat final variables untuk digunakan dalam lambda
        final String finalNamaPenginput = namaPenginput;
        final String finalNamaProspek = namaProspek;
        final int finalPosition = position;
        final String finalUserLevel = effectiveUserLevel;

        builder.setPositiveButton("Ya", (dialog, which) -> {
            performDelete(finalNamaPenginput, finalNamaProspek, finalPosition, finalUserLevel);
        });

        builder.setNegativeButton("Tidak", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void performDelete(String namaPenginput, String namaProspek, int position, String effectiveUserLevel) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // PERBAIKAN: Kirim user_level ke API
        Call<BasicResponse> call = apiService.deleteProspekByData(namaPenginput, namaProspek, effectiveUserLevel);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse deleteResponse = response.body();
                    if (deleteResponse.isSuccess()) {
                        prospekList.remove(position);
                        notifyItemRemoved(position);

                        // Tampilkan pesan berdasarkan level user
                        if ("Admin".equals(effectiveUserLevel)) {
                            Toast.makeText(context, "Data berhasil dihapus (Admin)", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Gagal menghapus: " + deleteResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Error response dari server", Toast.LENGTH_SHORT).show();
                    Log.e("ProspekAdapter2", "Delete response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ProspekAdapter2", "Delete failure", t);
            }
        });
    }

    @Override
    public int getItemCount() {
        return prospekList.size();
    }

    // Helper untuk mencegah null
    private String safeString(String value) {
        return value != null ? value : "-";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaProspek, tvNamaPenginput, tvTanggalInput, tvEmail,
                tvNoHp, tvAlamat, tvStatusNPWP, tvStatusBPJS;
        MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaProspek = itemView.findViewById(R.id.tvNama);
            tvNamaPenginput = itemView.findViewById(R.id.tvPenginput);
            tvTanggalInput = itemView.findViewById(R.id.tvTanggal);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvStatusNPWP = itemView.findViewById(R.id.tvStatusNPWP);
            tvStatusBPJS = itemView.findViewById(R.id.tvStatusBPJS);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}