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

public class PemohonAdapter extends RecyclerView.Adapter<PemohonAdapter.PemohonViewHolder> implements Filterable {

    private Context context;
    private List<DatabaseHelper.Pemohon> pemohonList;
    private List<DatabaseHelper.Pemohon> pemohonListFiltered;
    private DatabaseHelper databaseHelper;

    public PemohonAdapter(Context context, List<DatabaseHelper.Pemohon> pemohonList) {
        this.context = context;
        this.pemohonList = pemohonList;
        this.pemohonListFiltered = pemohonList;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public PemohonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pemohon, parent, false);
        return new PemohonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PemohonViewHolder holder, int position) {
        DatabaseHelper.Pemohon pemohon = pemohonListFiltered.get(position);

        // Format uang muka ke format Rupiah
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String uangMukaFormatted = formatRupiah.format(pemohon.getUangMuka());

        holder.tvPenginput.setText("Penginput: " + pemohon.getNamaPemohon());
        holder.tvTanggal.setText("Tanggal: " + pemohon.getTanggalPengajuan());
        holder.tvNamaPemohon.setText("Nama Pemohon: " + pemohon.getNamaPemohon());
        holder.tvEmail.setText("Email: " + pemohon.getEmailPemohon());
        holder.tvNoHp.setText("No. HP: " + pemohon.getNoHpPemohon());
        holder.tvAlamat.setText("Alamat: " + pemohon.getAlamatPemohon());
        holder.tvRefrensiProyek.setText("Referensi Proyek: " + pemohon.getReferensiProyek());
        holder.tvUnitHunian.setText("Nama Unit Hunian: " + pemohon.getReferensiUnitHunian());
        holder.tvTipeUnitHunian.setText("Tipe Unit Hunian: " + pemohon.getTipeUnitHunian());
        holder.tvTipeStatusPembayaran.setText("Status Pembayaran: " + pemohon.getStatusPembayaran());
        holder.tvJumlahUangMuka.setText("Jumlah Uang Muka: " + uangMukaFormatted);

        holder.btnEdit.setOnClickListener(v -> {
            // Intent ke activity edit pemohon
            Intent intent = new Intent(context, EditDataPemohonActivity.class);
            intent.putExtra("PEMOHON_ID", pemohon.getPemohonId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Tampilkan dialog konfirmasi hapus
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Data Pemohon")
                    .setMessage("Apakah Anda yakin ingin menghapus data pemohon " + pemohon.getNamaPemohon() + "?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        int result = databaseHelper.deletePemohon(pemohon.getPemohonId());
                        if (result > 0) {
                            Toast.makeText(context, "Data pemohon berhasil dihapus", Toast.LENGTH_SHORT).show();
                            // Refresh data
                            pemohonList.remove(pemohon);
                            pemohonListFiltered.remove(pemohon);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Gagal menghapus data pemohon", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return pemohonListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    pemohonListFiltered = pemohonList;
                } else {
                    List<DatabaseHelper.Pemohon> filteredList = new ArrayList<>();
                    for (DatabaseHelper.Pemohon row : pemohonList) {
                        if (row.getNamaPemohon().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getReferensiProyek().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getReferensiUnitHunian().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getStatusPembayaran().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    pemohonListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = pemohonListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                pemohonListFiltered = (ArrayList<DatabaseHelper.Pemohon>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class PemohonViewHolder extends RecyclerView.ViewHolder {
        TextView tvPenginput, tvTanggal, tvNamaPemohon, tvEmail, tvNoHp, tvAlamat;
        TextView tvRefrensiProyek, tvUnitHunian, tvTipeUnitHunian, tvTipeStatusPembayaran, tvJumlahUangMuka;
        MaterialButton btnEdit, btnDelete;

        public PemohonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvNamaPemohon = itemView.findViewById(R.id.tvNamaPemohon);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvNoHp = itemView.findViewById(R.id.tvNoHp);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvRefrensiProyek = itemView.findViewById(R.id.tvRefrensiProyek);
            tvUnitHunian = itemView.findViewById(R.id.tvUnitHunian);
            tvTipeUnitHunian = itemView.findViewById(R.id.tvTipeUnitHunian);
            tvTipeStatusPembayaran = itemView.findViewById(R.id.tvTipeStatusPembayaran);
            tvJumlahUangMuka = itemView.findViewById(R.id.tvJumlahUangMuka);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}