package com.example.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ProspekAdapter extends RecyclerView.Adapter<ProspekAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Prospek> prospekList;

    public ProspekAdapter(Context context, ArrayList<Prospek> prospekList) {
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
        Prospek prospek = prospekList.get(position);

        // Set data ke TextView
        holder.tvPenginput.setText("Penginput: " + prospek.getPenginput());
        holder.tvNama.setText("Nama: " + prospek.getNama());
        holder.tvEmail.setText("Email: " + prospek.getEmail());
        holder.tvNoHp.setText("No. HP: " + prospek.getNoHp());
        holder.tvAlamat.setText("Alamat: " + prospek.getAlamat());
        holder.tvTanggal.setText("Tanggal: " + formatTanggal(prospek.getTanggalBuat()));

        holder.btnEdit.setOnClickListener(v ->
                Toast.makeText(context, "Edit " + prospek.getNama(), Toast.LENGTH_SHORT).show()
        );

        holder.btnDelete.setOnClickListener(v ->
                Toast.makeText(context, "Hapus " + prospek.getNama(), Toast.LENGTH_SHORT).show()
        );
    }

    // Method untuk memformat tanggal (opsional)
    private String formatTanggal(String tanggal) {
        if (tanggal == null || tanggal.isEmpty()) {
            return "-";
        }
        // Anda bisa menambahkan logic formatting tanggal di sini
        return tanggal;
    }

    @Override
    public int getItemCount() {
        return prospekList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPenginput, tvNama, tvEmail, tvNoHp, tvAlamat, tvTanggal;
        MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // Method untuk update data
    public void updateData(ArrayList<Prospek> newProspekList) {
        prospekList.clear();
        prospekList.addAll(newProspekList);
        notifyDataSetChanged();
    }
}