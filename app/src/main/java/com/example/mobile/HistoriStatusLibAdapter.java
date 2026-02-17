package com.example.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoriStatusLibAdapter extends RecyclerView.Adapter<HistoriStatusLibAdapter.ViewHolder> {

    private Context context;
    private List<HistoriStatusLib> historiList;

    public HistoriStatusLibAdapter(Context context, List<HistoriStatusLib> historiList) {
        this.context = context;
        this.historiList = historiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_histori_status_lib, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoriStatusLib histori = historiList.get(position);

        // Format data dengan label yang jelas
        holder.tvIdHistori.setText("ID Histori: " + histori.getIdHistoriLib());
        holder.tvPerubahan.setText("Perubahan: " +
                (histori.getPerubahanStatus() != null ? histori.getPerubahanStatus() : "-"));
        holder.tvNamaProspek.setText("Nama Prospek: " +
                (histori.getNamaProspek() != null ? histori.getNamaProspek() : "-"));
        holder.tvPenginput.setText("Penginput: " +
                (histori.getNamaPenginput() != null ? histori.getNamaPenginput() : "-"));
        holder.tvTanggal.setText("Tanggal: " +
                (histori.getTanggalPerubahan() != null ? histori.getTanggalPerubahan() : "-"));

        holder.btnDeleteHistori.setOnClickListener(v -> {
            Toast.makeText(context,
                    "Hapus histori ID: " + histori.getIdHistoriLib(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return historiList != null ? historiList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdHistori, tvPerubahan, tvNamaProspek, tvPenginput, tvTanggal;
        Button btnDeleteHistori;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdHistori = itemView.findViewById(R.id.tvIdHistori);
            tvPerubahan = itemView.findViewById(R.id.tvPerubahan);
            tvNamaProspek = itemView.findViewById(R.id.tvNamaProspek);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            btnDeleteHistori = itemView.findViewById(R.id.btnDeleteHistori);
        }
    }
}