package com.example.mobile;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Intent;
public class LihatDataUserpActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private UserProspekAdapter adapter;
    private List<DatabaseHelper.UserProspek> userProspekList;
    private List<DatabaseHelper.UserProspek> filteredList;
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_userp);

        // Inisialisasi database helper
        dbHelper = new DatabaseHelper(this);

        // Inisialisasi view
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userProspekList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserProspekAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadUserProspekData();

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataUserpActivity.this, LihatDataActivity.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

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

    // Method untuk format angka dengan separator
    private String formatCurrency(double amount) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###");
        return formatter.format(amount);
    }

    private void showEditDialog(DatabaseHelper.UserProspek userProspek) {
        // Gunakan Intent untuk membuka activity edit
        Intent intent = new Intent(LihatDataUserpActivity.this, EditDataUserpActivity.class);
        intent.putExtra("USER_PROSPEK_ID", userProspek.getUserProspekId());
        intent.putExtra("PENGINPUT", userProspek.getPenginput());
        intent.putExtra("NAMA", userProspek.getNama()); // TETAP KIRIM NAMA MESKIPUN TIDAK DITAMPILKAN
        intent.putExtra("EMAIL", userProspek.getEmail());
        intent.putExtra("NO_HP", userProspek.getNoHp());
        intent.putExtra("ALAMAT", userProspek.getAlamat());
        intent.putExtra("NAMA_PROYEK", userProspek.getNamaProyek());
        intent.putExtra("UANG_TANDA_JADI", userProspek.getUangTandaJadi());
        startActivityForResult(intent, 1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Refresh data setelah edit
            loadUserProspekData();
        }
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

            // Format uang tanda jadi dengan separator
            String formattedUang = "Uang Tanda Jadi: Rp " + formatCurrency(userProspek.getUangTandaJadi());
            holder.tvJumlahUangTandaJadi.setText(formattedUang);

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