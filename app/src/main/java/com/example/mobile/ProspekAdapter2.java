package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProspekAdapter2 extends RecyclerView.Adapter<ProspekAdapter2.ViewHolder> {

    private final Context context;
    private final ArrayList<Prospek2> prospekList;
    private final String userLevel;
    private final ArrayList<Prospek2> prospekListFull;

    private static final String TAG = "ProspekAdapter2";

    // Warna untuk setiap status
    private static final int COLOR_COLD = Color.parseColor("#E1F5FE");
    private static final int COLOR_FOLLOW_UP = Color.parseColor("#FFF9C4");
    private static final int COLOR_KUNJUNGAN = Color.parseColor("#E8F5E9");
    private static final int COLOR_BATAL = Color.parseColor("#FFEBEE");
    private static final int COLOR_DEFAULT = Color.WHITE;

    public ProspekAdapter2(Context context, ArrayList<Prospek2> prospekList, String userLevel) {
        this.context = context;
        this.prospekList = prospekList;
        this.userLevel = userLevel;
        this.prospekListFull = new ArrayList<>(prospekList);
    }

    public void filterByStatus(String status) {
        prospekList.clear();
        if (status.equals("Semua")) {
            prospekList.addAll(prospekListFull);
        } else {
            for (Prospek2 prospek : prospekListFull) {
                if (prospek.getStatusLib() != null && prospek.getStatusLib().equalsIgnoreCase(status)) {
                    prospekList.add(prospek);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterBySearch(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        prospekList.clear();

        if (lowerCaseQuery.isEmpty()) {
            prospekList.addAll(prospekListFull);
        } else {
            for (Prospek2 prospek : prospekListFull) {
                boolean matches = (prospek.getNamaProspek() != null && prospek.getNamaProspek().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getNamaPenginput() != null && prospek.getNamaPenginput().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getEmail() != null && prospek.getEmail().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getNoHp() != null && prospek.getNoHp().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getAlamat() != null && prospek.getAlamat().toLowerCase().contains(lowerCaseQuery)) ||
                        (prospek.getStatusLib() != null && prospek.getStatusLib().toLowerCase().contains(lowerCaseQuery));

                if (matches) {
                    prospekList.add(prospek);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(ArrayList<Prospek2> newData) {
        prospekList.clear();
        prospekList.addAll(newData);
        prospekListFull.clear();
        prospekListFull.addAll(newData);
        notifyDataSetChanged();
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
        String status = prospek.getStatusLib();

        // Binding data ke card
        holder.tvPenginput.setText("Penginput: " + safeString(prospek.getNamaPenginput()));
        holder.tvTanggal.setText("Tanggal: " + safeString(prospek.getTanggalInput()));
        holder.tvNama.setText("Nama: " + safeString(prospek.getNamaProspek()));
        holder.tvStatus.setText("Status: " + safeString(status));
        holder.tvEmail.setText("Email: " + safeString(prospek.getEmail()));
        holder.tvNoHp.setText("No. HP: " + safeString(prospek.getNoHp()));
        holder.tvAlamat.setText("Alamat: " + safeString(prospek.getAlamat()));
        holder.tvStatusNPWP.setText("NPWP: " + safeString(prospek.getStatusNpwp()));
        holder.tvStatusBPJS.setText("BPJS: " + safeString(prospek.getStatusBpjs()));

        // Atur warna background card berdasarkan status
        setCardColorSimple(holder, status);

        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("username", "");
        String savedLevel = sharedPreferences.getString("level", "Operator");

        final String effectiveUserLevel;
        String tempUserLevel = userLevel;
        if (tempUserLevel == null || tempUserLevel.isEmpty()) {
            tempUserLevel = savedLevel;
        }
        effectiveUserLevel = tempUserLevel;

        final boolean canEdit;
        if ("Admin".equals(effectiveUserLevel)) {
            canEdit = true;
        } else {
            canEdit = currentUsername.equals(prospek.getNamaPenginput());
        }

        holder.btnEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        // TOMBOL HISTORI - HANYA JIKA ADA ID_PROSPEK
        if (prospek.getIdProspek() > 0) {
            holder.btnHistoriStatus.setVisibility(View.VISIBLE);
        } else {
            holder.btnHistoriStatus.setVisibility(View.GONE);
        }

        if ("Admin".equals(effectiveUserLevel) && !currentUsername.equals(prospek.getNamaPenginput())) {
            holder.tvPenginput.setText("Penginput: " + safeString(prospek.getNamaPenginput()) + " (Orang Lain)");
        }

        // Tombol Edit
        holder.btnEdit.setOnClickListener(v -> {
            if (!canEdit) {
                Toast.makeText(context, "Anda tidak memiliki akses untuk mengedit data ini", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, EditDataProspekActivity.class);
            intent.putExtra("ID_PROSPEK", prospek.getIdProspek());
            intent.putExtra("NAMA_PENGINPUT", prospek.getNamaPenginput());
            intent.putExtra("NAMA_PROSPEK", prospek.getNamaProspek());
            intent.putExtra("EMAIL", prospek.getEmail());
            intent.putExtra("NO_HP", prospek.getNoHp());
            intent.putExtra("ALAMAT", prospek.getAlamat());
            intent.putExtra("REFERENSI_PROYEK", prospek.getReferensiProyek());
            intent.putExtra("STATUS_NPWP", prospek.getStatusNpwp());
            intent.putExtra("STATUS_BPJS", prospek.getStatusBpjs());
            intent.putExtra("STATUS_LIB", prospek.getStatusLib());

            if ("Admin".equals(effectiveUserLevel)) {
                intent.putExtra("IS_ADMIN_EDIT", true);
            }

            context.startActivity(intent);
        });

        // Tombol Histori Status
        holder.btnHistoriStatus.setOnClickListener(v -> {
            if (prospek.getIdProspek() > 0) {
                Intent intent = new Intent(context, HistoriStatusLibActivity.class);
                intent.putExtra("ID_PROSPEK", prospek.getIdProspek());
                intent.putExtra("NAMA_PROSPEK", prospek.getNamaProspek());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "ID Prospek tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol Delete
        holder.btnDelete.setOnClickListener(v -> {
            if (!canEdit) {
                Toast.makeText(context, "Anda tidak memiliki akses untuk menghapus data ini", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteProspek(prospek.getNamaPenginput(), prospek.getNamaProspek(), position, effectiveUserLevel);
        });
    }

    private void setCardColorSimple(ViewHolder holder, String status) {
        if (status == null) {
            holder.cardProspek.setCardBackgroundColor(COLOR_DEFAULT);
            return;
        }

        String statusLower = status.toLowerCase().trim();

        switch (statusLower) {
            case "cold":
                holder.cardProspek.setCardBackgroundColor(COLOR_COLD);
                break;
            case "follow up":
                holder.cardProspek.setCardBackgroundColor(COLOR_FOLLOW_UP);
                break;
            case "kunjungan":
                holder.cardProspek.setCardBackgroundColor(COLOR_KUNJUNGAN);
                break;
            case "batal":
                holder.cardProspek.setCardBackgroundColor(COLOR_BATAL);
                break;
            default:
                holder.cardProspek.setCardBackgroundColor(COLOR_DEFAULT);
                break;
        }
    }

    private void deleteProspek(String namaPenginput, String namaProspek, int position, String effectiveUserLevel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Apakah Anda yakin ingin menghapus data prospek '" + namaProspek + "'?");

        builder.setPositiveButton("Ya", (dialog, which) -> {
            performDelete(namaPenginput, namaProspek, position, effectiveUserLevel);
        });

        builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void performDelete(String namaPenginput, String namaProspek, int position, String effectiveUserLevel) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.deleteProspekByData(namaPenginput, namaProspek, effectiveUserLevel);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse deleteResponse = response.body();
                    if (deleteResponse.isSuccess()) {

                        // ✅ BARU: Log DELETE prospek ke tabel log_aktivitas
                        ActivityLogger.log(
                                context,
                                ActivityLogger.AKSI_DELETE,
                                ActivityLogger.FITUR_PROSPEK,
                                "Hapus prospek: " + namaProspek + " | Penginput: " + namaPenginput,
                                "LihatDataProspekActivity",
                                null,
                                namaProspek
                        );
                        // ==================================================

                        prospekList.remove(position);
                        prospekListFull.remove(position);
                        notifyItemRemoved(position);

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
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(context, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return prospekList.size();
    }

    private String safeString(String value) {
        return value != null ? value : "-";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPenginput, tvTanggal, tvNama, tvStatus, tvEmail,
                tvNoHp, tvAlamat, tvStatusNPWP, tvStatusBPJS;
        MaterialButton btnEdit, btnDelete, btnHistoriStatus;
        MaterialCardView cardProspek;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inisialisasi semua TextView sesuai dengan layout
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvStatusNPWP = itemView.findViewById(R.id.tvStatusNPWP);
            tvStatusBPJS = itemView.findViewById(R.id.tvStatusBPJS);

            // Inisialisasi tombol
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnHistoriStatus = itemView.findViewById(R.id.btnHistoriStatus);

            // Inisialisasi card view
            cardProspek = itemView.findViewById(R.id.cardProspek);
        }
    }
}