package com.example.mobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.view.View;
import androidx.appcompat.widget.Toolbar;

public class LihatDataUserpActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private UserProspekAdapter adapter;
    private List<DatabaseHelper.UserProspek> userProspekList;
    private List<DatabaseHelper.UserProspek> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_userp);

        // Inisialisasi database helper
        dbHelper = new DatabaseHelper(this);

        // Inisialisasi view
        Toolbar toolbar = findViewById(R.id.topAppBar);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userProspekList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserProspekAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadUserProspekData();

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserProspekData() {
        userProspekList = dbHelper.getAllUserProspek();
        filteredList.clear();
        filteredList.addAll(userProspekList);
        adapter.notifyDataSetChanged();
    }

    private void filterData(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userProspekList);
        } else {
            for (DatabaseHelper.UserProspek userProspek : userProspekList) {
                if (userProspek.getNama().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(userProspek);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showEditDialog(DatabaseHelper.UserProspek userProspek) {
        // Buat dialog custom dengan layout yang khusus untuk edit
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.activity_edit_data_userp, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Inisialisasi view dari dialog
        Toolbar toolbar = dialogView.findViewById(R.id.topAppBar);
        EditText editTextPenginput = dialogView.findViewById(R.id.editTextPenginput);
        EditText editTextNama = dialogView.findViewById(R.id.editTextNama);
        EditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        EditText editTextNoHp = dialogView.findViewById(R.id.editTextNoHp);
        EditText editTextAlamat = dialogView.findViewById(R.id.editTextAlamat);
        Spinner spinnerProyek = dialogView.findViewById(R.id.spinnerRoleRefrensiProyek);
        EditText editTextUangTandaJadi = dialogView.findViewById(R.id.editTextUangPengadaan);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpan);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);

        // Set judul toolbar
        toolbar.setTitle("Edit Data User Prospek");
        toolbar.setNavigationOnClickListener(v -> dialog.dismiss());

        // Isi data saat ini
        editTextPenginput.setText(userProspek.getPenginput());
        editTextNama.setText(userProspek.getNama());
        editTextEmail.setText(userProspek.getEmail());
        editTextNoHp.setText(userProspek.getNoHp());
        editTextAlamat.setText(userProspek.getAlamat());
        editTextUangTandaJadi.setText(String.valueOf(userProspek.getUangTandaJadi()));

        // Non-aktifkan field yang tidak boleh diubah
        editTextPenginput.setEnabled(false);

        // Load data proyek untuk spinner
        List<String> proyekList = dbHelper.getAllNamaProyek();
        ArrayAdapter<String> proyekAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, proyekList);
        proyekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyek.setAdapter(proyekAdapter);

        // Set proyek yang dipilih sebelumnya
        if (userProspek.getNamaProyek() != null && !userProspek.getNamaProyek().isEmpty()) {
            int position = proyekAdapter.getPosition(userProspek.getNamaProyek());
            if (position >= 0) {
                spinnerProyek.setSelection(position);
            }
        }

        // Set hint yang sesuai
        editTextNama.setHint("Nama Lengkap");
        editTextEmail.setHint("Alamat Email");
        editTextNoHp.setHint("Nomor Handphone");
        editTextAlamat.setHint("Alamat Rumah");
        editTextUangTandaJadi.setHint("Jumlah Uang Tanda Jadi");

        // Listener untuk button Simpan
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = editTextNama.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String noHp = editTextNoHp.getText().toString().trim();
                String alamat = editTextAlamat.getText().toString().trim();
                String namaProyek = spinnerProyek.getSelectedItem().toString();
                String uangTandaJadiStr = editTextUangTandaJadi.getText().toString().trim();

                // Validasi input
                if (nama.isEmpty() || uangTandaJadiStr.isEmpty()) {
                    Toast.makeText(LihatDataUserpActivity.this,
                            "Nama dan uang tanda jadi harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (namaProyek.isEmpty() || namaProyek.equals("Pilih Refrensi Proyek")) {
                    Toast.makeText(LihatDataUserpActivity.this,
                            "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double uangTandaJadi = Double.parseDouble(uangTandaJadiStr);

                    // Update data
                    int result = dbHelper.updateUserProspek(
                            userProspek.getUserProspekId(),
                            userProspek.getPenginput(), // penginput tidak bisa diubah
                            nama,
                            email,
                            noHp,
                            alamat,
                            namaProyek,
                            uangTandaJadi
                    );

                    if (result > 0) {
                        Toast.makeText(LihatDataUserpActivity.this,
                                "Data berhasil diupdate", Toast.LENGTH_SHORT).show();
                        loadUserProspekData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LihatDataUserpActivity.this,
                                "Gagal mengupdate data", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(LihatDataUserpActivity.this,
                            "Format uang tanda jadi tidak valid", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener untuk button Batal
        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Sembunyikan bottom navigation di dialog
        View bottomNav = dialogView.findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        // Sembunyikan field yang tidak diperlukan
        View editTextNamaProspek = dialogView.findViewById(R.id.editTextNamaProspek);
        if (editTextNamaProspek != null) {
            editTextNamaProspek.setVisibility(View.GONE);
        }

        // Sembunyikan spinner prospek jika ada
        View spinnerRoleProspek = dialogView.findViewById(R.id.spinnerRoleRefrensiProyek);
        if (spinnerRoleProspek != null) {
            spinnerRoleProspek.setVisibility(View.GONE);
        }
    }
    private void showDeleteConfirmation(DatabaseHelper.UserProspek userProspek) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus data " + userProspek.getNama() + "?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    int result = dbHelper.deleteUserProspek(userProspek.getUserProspekId());
                    if (result > 0) {
                        Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                        loadUserProspekData();
                    } else {
                        Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    // Adapter class
    private class UserProspekAdapter extends RecyclerView.Adapter<UserProspekAdapter.ViewHolder> {

        private List<DatabaseHelper.UserProspek> userProspekList;

        public UserProspekAdapter(List<DatabaseHelper.UserProspek> userProspekList) {
            this.userProspekList = userProspekList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_userp, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DatabaseHelper.UserProspek userProspek = userProspekList.get(position);

            holder.tvPenginput.setText("Penginput: " + userProspek.getPenginput());
            holder.tvTanggal.setText("Tanggal: " + userProspek.getTanggalBuat());
            holder.tvNama.setText("Nama: " + userProspek.getNama());
            holder.tvEmail.setText("Email: " + userProspek.getEmail());
            holder.tvNoHp.setText("No. HP: " + userProspek.getNoHp());
            holder.tvAlamat.setText("Alamat: " + userProspek.getAlamat());
            holder.tvJumlahUangTandaJadi.setText("Uang Tanda Jadi: Rp " + userProspek.getUangTandaJadi());

            holder.btnEdit.setOnClickListener(v -> showEditDialog(userProspek));
            holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(userProspek));
        }

        @Override
        public int getItemCount() {
            return userProspekList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPenginput, tvTanggal, tvNama, tvEmail, tvNoHp, tvAlamat, tvJumlahUangTandaJadi;
            MaterialButton btnEdit, btnDelete;

            public ViewHolder(View itemView) {
                super(itemView);
                tvPenginput = itemView.findViewById(R.id.tvPenginput);
                tvTanggal = itemView.findViewById(R.id.tvTanggal);
                tvNama = itemView.findViewById(R.id.tvNama);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvNoHp = itemView.findViewById(R.id.tvNoHp);
                tvAlamat = itemView.findViewById(R.id.tvAlamat);
                tvJumlahUangTandaJadi = itemView.findViewById(R.id.tvJumlahUangTandaJadi);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProspekData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}