package com.example.mobile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UnitHunianAdapter extends RecyclerView.Adapter<UnitHunianAdapter.UnitHunianViewHolder> implements Filterable {

    private Context context;
    private List<DatabaseHelper.UnitHunian> unitHunianList;
    private List<DatabaseHelper.UnitHunian> unitHunianListFiltered;
    private DatabaseHelper databaseHelper;

    public UnitHunianAdapter(Context context, List<DatabaseHelper.UnitHunian> unitHunianList) {
        this.context = context;
        this.unitHunianList = unitHunianList;
        this.unitHunianListFiltered = unitHunianList;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public UnitHunianViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit_hunian, parent, false);
        return new UnitHunianViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitHunianViewHolder holder, int position) {
        DatabaseHelper.UnitHunian unitHunian = unitHunianListFiltered.get(position);

        // Format harga ke format Rupiah
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String hargaFormatted = formatRupiah.format(unitHunian.getHargaUnit());

        holder.tvNamaUnitHunian.setText("Nama Unit Hunian: " + unitHunian.getNamaUnit());
        holder.tvRefrensiProyek.setText("Referensi Proyek: " + unitHunian.getReferensiProyek());
        holder.tvHargaUnit.setText("Harga Unit: " + hargaFormatted);

        holder.btnEdit.setOnClickListener(v -> {
            // Intent ke activity edit unit hunian
            Intent intent = new Intent(context, EditDataUnitHunianActivity.class);
            intent.putExtra("UNIT_ID", unitHunian.getUnitId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Tampilkan dialog konfirmasi hapus
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Unit Hunian")
                    .setMessage("Apakah Anda yakin ingin menghapus unit hunian " + unitHunian.getNamaUnit() + "?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        int result = databaseHelper.deleteUnitHunian(unitHunian.getUnitId());
                        if (result > 0) {
                            Toast.makeText(context, "Unit hunian berhasil dihapus", Toast.LENGTH_SHORT).show();
                            // Refresh data
                            unitHunianList.remove(unitHunian);
                            unitHunianListFiltered.remove(unitHunian);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Gagal menghapus unit hunian", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return unitHunianListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    unitHunianListFiltered = unitHunianList;
                } else {
                    List<DatabaseHelper.UnitHunian> filteredList = new ArrayList<>();
                    for (DatabaseHelper.UnitHunian row : unitHunianList) {
                        if (row.getNamaUnit().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getReferensiProyek().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    unitHunianListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = unitHunianListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                unitHunianListFiltered = (ArrayList<DatabaseHelper.UnitHunian>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class UnitHunianViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaUnitHunian, tvRefrensiProyek, tvHargaUnit;
        MaterialButton btnEdit, btnDelete;

        public UnitHunianViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaUnitHunian = itemView.findViewById(R.id.tvNamaUnitHunian);
            tvRefrensiProyek = itemView.findViewById(R.id.tvRefrensiProyek);
            tvHargaUnit = itemView.findViewById(R.id.tvHargaUnit);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}