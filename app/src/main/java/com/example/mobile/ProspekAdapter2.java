package com.example.mobile;

import android.content.Context;
import android.content.Intent;
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

    public ProspekAdapter2(Context context, ArrayList<Prospek2> prospekList) {
        this.context = context;
        this.prospekList = prospekList;
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
        holder.tvNoHp.setText("No. HP: " + safeString(prospek.getNoHp()));
        holder.tvAlamat.setText("Alamat: " + safeString(prospek.getAlamat()));
        holder.tvStatusNPWP.setText("NPWP: " + safeString(prospek.getStatusNpwp()));
        holder.tvStatusBPJS.setText("BPJS: " + safeString(prospek.getStatusBpjs()));

        // Tombol Edit
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDataProspekActivity.class);

            // Kirim semua data ke EditDataProspekActivity
            intent.putExtra("PROSPEK_ID", prospek.getIdProspek());
            intent.putExtra("NAMA_PENGINPUT", prospek.getNamaPenginput());
            intent.putExtra("NAMA_PROSPEK", prospek.getNamaProspek());
            intent.putExtra("EMAIL", prospek.getEmail());
            intent.putExtra("NO_HP", prospek.getNoHp());
            intent.putExtra("ALAMAT", prospek.getAlamat());
            intent.putExtra("REFERENSI_PROYEK", prospek.getReferensiProyek());
            intent.putExtra("STATUS_NPWP", prospek.getStatusNpwp());
            intent.putExtra("STATUS_BPJS", prospek.getStatusBpjs());

            context.startActivity(intent);
        });

        // Tombol Hapus
        holder.btnDelete.setOnClickListener(v -> {
            // Sekarang hapus menggunakan nama_penginput + nama_prospek
            deleteProspek(prospek.getNamaPenginput(), prospek.getNamaProspek(), position);
        });
    }

    private void deleteProspek(String namaPenginput, String namaProspek, int position) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<BasicResponse> call = apiService.deleteProspekByData(namaPenginput, namaProspek);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse deleteResponse = response.body();
                    if (deleteResponse.isSuccess()) {
                        prospekList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
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
