package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class TambahUserpActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner spinnerRoleProspek, spinnerRoleRefrensiProyek;
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp,
            editTextAlamat, editTextUangTandaJadi;
    private Button btnSimpan, btnBatal;
    private int selectedProspekId = -1;
    private String selectedNamaProyek = "";
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambahuserp);

        // Inisialisasi database helper
        dbHelper = new DatabaseHelper(this);

        // Inisialisasi view
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
        spinnerRoleProspek = findViewById(R.id.spinnerRoleProspek);
        spinnerRoleRefrensiProyek = findViewById(R.id.spinnerRoleRefrensiProyek);
        editTextPenginput = findViewById(R.id.editTextPenginput);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        editTextUangTandaJadi = findViewById(R.id.editTextUangPengadaan); // Tetap gunakan ID yang sama di XML
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Load data untuk spinner
        loadProspekData();
        loadProyekData();

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(TambahUserpActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, BerandaActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Listener untuk spinner prospek
        spinnerRoleProspek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedNama = parent.getItemAtPosition(position).toString();
                loadProspekDetails(selectedNama);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Listener untuk spinner proyek
        spinnerRoleRefrensiProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNamaProyek = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedNamaProyek = "";
            }
        });

        // Setup button listeners
        btnSimpan.setOnClickListener(v -> simpanUserProspek());
        btnBatal.setOnClickListener(v -> finish());

        // EdgeToEdge insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadProspekData() {
        List<String> prospekNamaList = dbHelper.getAllProspekNama();

        if (prospekNamaList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data prospek tersedia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, prospekNamaList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleProspek.setAdapter(adapter);
    }

    private void loadProyekData() {
        List<String> proyekNamaList = dbHelper.getAllNamaProyek();

        if (proyekNamaList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data proyek tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, proyekNamaList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleRefrensiProyek.setAdapter(adapter);
    }

    private void loadProspekDetails(String nama) {
        // Hapus DatabaseHelper. dan gunakan class Prospek langsung
        Prospek prospek = dbHelper.getProspekByNama(nama);

        if (prospek != null) {
            selectedProspekId = prospek.getProspekId();
            editTextPenginput.setText(prospek.getPenginput());
            editTextNama.setText(prospek.getNama());
            editTextEmail.setText(prospek.getEmail());
            editTextNoHp.setText(prospek.getNoHp());
            editTextAlamat.setText(prospek.getAlamat());

            // Non-aktifkan edit text yang diisi otomatis dari prospek
            editTextPenginput.setEnabled(false);
            editTextNama.setEnabled(false);
            editTextEmail.setEnabled(false);
            editTextNoHp.setEnabled(false);
            editTextAlamat.setEnabled(false);
        }
    }

    private void simpanUserProspek() {
        // Validasi input
        if (selectedProspekId == -1) {
            Toast.makeText(this, "Pilih prospek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedNamaProyek.isEmpty()) {
            Toast.makeText(this, "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String uangTandaJadiStr = editTextUangTandaJadi.getText().toString().trim();
        if (uangTandaJadiStr.isEmpty()) {
            Toast.makeText(this, "Uang tanda jadi harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double uangTandaJadi = Double.parseDouble(uangTandaJadiStr);

            // Pindahkan data dari prospek ke user_prospek dan hapus dari prospek
            boolean success = dbHelper.migrateAndDeleteProspek(selectedProspekId, selectedNamaProyek, uangTandaJadi);

            if (success) {
                Toast.makeText(this, "Data berhasil ditambahkan ke user prospek", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal menambahkan data", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format uang tanda jadi tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}