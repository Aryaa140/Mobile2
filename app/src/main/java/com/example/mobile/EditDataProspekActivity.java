package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EditDataProspekActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp, editTextAlamat;
    private Spinner spinnerReferensi, spinnerNPWP, spinnerBPJS; // TAMBAHAN: Spinner untuk NPWP dan BPJS
    private Button btnEdit, btnBatal;
    private DatabaseHelper databaseHelper;
    private int prospekId;
    private Prospek prospek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_prospek);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Inisialisasi view
        editTextPenginput = findViewById(R.id.editTextProspek);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        spinnerNPWP = findViewById(R.id.spinnerRole2); // TAMBAHAN: Spinner NPWP
        spinnerBPJS = findViewById(R.id.spinnerRole3); // TAMBAHAN: Spinner BPJS
        btnEdit = findViewById(R.id.btnEdit);
        btnBatal = findViewById(R.id.btnBatal);

        databaseHelper = new DatabaseHelper(this);

        // Ambil data prospekId dari intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("PROSPEK_ID")) {
            prospekId = intent.getIntExtra("PROSPEK_ID", -1);
            if (prospekId != -1) {
                loadProspekData();
            } else {
                Toast.makeText(this, "Data prospek tidak valid", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Data prospek tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Setup button listeners
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDataProspek();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Kembali ke activity sebelumnya
            }
        });

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent backIntent = new Intent(EditDataProspekActivity.this, LihatDataProspekActivity.class);
            startActivity(backIntent);
            finish();
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadProspekData() {
        prospek = databaseHelper.getProspekById(prospekId);

        if (prospek != null) {
            // Isi form dengan data lama
            editTextPenginput.setText(prospek.getPenginput());
            editTextNama.setText(prospek.getNama());
            editTextEmail.setText(prospek.getEmail());
            editTextNoHp.setText(prospek.getNoHp());
            editTextAlamat.setText(prospek.getAlamat());

            // Set spinner referensi
            setSpinnerSelection(spinnerReferensi, prospek.getReferensi());

            // TAMBAHAN: Set spinner NPWP dan BPJS
            setSpinnerSelection(spinnerNPWP, prospek.getStatusNpwp());
            setSpinnerSelection(spinnerBPJS, prospek.getStatusBpjs());
        } else {
            Toast.makeText(this, "Gagal memuat data prospek", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Method untuk set selection spinner berdasarkan value
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null && !value.isEmpty()) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).toString().equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateDataProspek() {
        // Ambil data dari form
        String penginput = editTextPenginput.getText().toString().trim();
        String nama = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String referensi = spinnerReferensi.getSelectedItem().toString();
        String statusNpwp = spinnerNPWP.getSelectedItem().toString(); // TAMBAHAN: Status NPWP
        String statusBpjs = spinnerBPJS.getSelectedItem().toString(); // TAMBAHAN: Status BPJS

        // Validasi input
        if (penginput.isEmpty()) {
            editTextPenginput.setError("Nama penginput harus diisi");
            editTextPenginput.requestFocus();
            return;
        }

        if (nama.isEmpty()) {
            editTextNama.setError("Nama lengkap harus diisi");
            editTextNama.requestFocus();
            return;
        }

        if (noHp.isEmpty()) {
            editTextNoHp.setError("No. Handphone harus diisi");
            editTextNoHp.requestFocus();
            return;
        }

        if (alamat.isEmpty()) {
            editTextAlamat.setError("Alamat harus diisi");
            editTextAlamat.requestFocus();
            return;
        }

        // Validasi format email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            return;
        }

        // Update data ke database dengan parameter baru
        int result = databaseHelper.updateProspek(prospekId, penginput, nama, email, noHp, alamat, referensi, statusNpwp, statusBpjs);

        if (result > 0) {
            Toast.makeText(this, "Data prospek berhasil diupdate", Toast.LENGTH_SHORT).show();

            // Kembali ke halaman lihat data dengan result OK
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Gagal mengupdate data prospek", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}