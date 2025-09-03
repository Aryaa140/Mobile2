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
        holder.tvNama.setText(prospek.getNama());
        holder.tvEmail.setText(prospek.getEmail());
        holder.tvNoHp.setText(prospek.getNoHp());
        holder.tvAlamat.setText(prospek.getAlamat());

        holder.btnEdit.setOnClickListener(v ->
                Toast.makeText(context, "Edit " + prospek.getNama(), Toast.LENGTH_SHORT).show()
        );

        holder.btnDelete.setOnClickListener(v ->
                Toast.makeText(context, "Hapus " + prospek.getNama(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return prospekList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvEmail, tvNoHp, tvAlamat;
        MaterialButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
