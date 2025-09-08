package com.example.mobile;

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

import java.util.List;

public class TambahUserpActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner spinnerRoleProspek, spinnerRoleRefrensiProyek;
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp,
            editTextAlamat, editTextReferensi, editTextUangPengadaan;
    private Button btnSimpan, btnBatal;

    private int selectedProspekId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambahuserp);

        // Inisialisasi database helper
        dbHelper = new DatabaseHelper(this);

        // Inisialisasi view
        Toolbar toolbar = findViewById(R.id.topAppBar);
        spinnerRoleProspek = findViewById(R.id.spinnerRoleProspek);
        editTextPenginput = findViewById(R.id.editTextPenginput);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        spinnerRoleRefrensiProyek = findViewById(R.id.spinnerRoleRefrensiProyek);
        editTextUangPengadaan = findViewById(R.id.editTextUangPengadaan);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        toolbar.setNavigationOnClickListener(v -> finish());

        loadProspekData();

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

    private void loadProspekDetails(String nama) {
        // PERBAIKAN: Gunakan Prospek (bukan DatabaseHelper.Prospek)
        Prospek prospek = dbHelper.getProspekByNama(nama);

        if (prospek != null) {
            selectedProspekId = prospek.getProspekId(); // PERBAIKAN: getProspekId() bukan getId()
            editTextPenginput.setText(prospek.getPenginput());
            editTextNama.setText(prospek.getNama());
            editTextEmail.setText(prospek.getEmail());
            editTextNoHp.setText(prospek.getNoHp());
            editTextAlamat.setText(prospek.getAlamat());
            editTextReferensi.setText(prospek.getReferensi());
        }
    }

    private void simpanUserProspek() {
        // Validasi input
        if (selectedProspekId == -1) {
            Toast.makeText(this, "Pilih prospek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String uangPengadaanStr = editTextUangPengadaan.getText().toString().trim();
        if (uangPengadaanStr.isEmpty()) {
            Toast.makeText(this, "Uang pengadaan harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double uangPengadaan = Double.parseDouble(uangPengadaanStr);

            // Pindahkan data dari prospek ke user_prospek dan hapus dari prospek
            boolean success = dbHelper.migrateAndDeleteProspek(selectedProspekId, uangPengadaan);

            if (success) {
                Toast.makeText(this, "Data berhasil ditambahkan ke user prospek", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal menambahkan data", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format uang pengadaan tidak valid", Toast.LENGTH_SHORT).show();
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