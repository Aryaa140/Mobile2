package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataProspekActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private EditText editTextProspek, editTextNama, editTextEmail, editTextNoHp, editTextAlamat;
    private Spinner spinnerRole, spinnerRole2, spinnerRole3;
    private Button btnEdit, btnBatal;
    private SharedPreferences sharedPreferences;

    // Simpan data lama untuk digunakan sebagai identifier
    private String oldNamaProspek, oldNoHp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_prospek);

        // Inisialisasi SharedPreferences (Remember Me)
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        editTextProspek = findViewById(R.id.editTextProspek);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerRole2 = findViewById(R.id.spinnerRole2);
        spinnerRole3 = findViewById(R.id.spinnerRole3);
        btnEdit = findViewById(R.id.btnEdit);
        btnBatal = findViewById(R.id.btnBatal);

        // Ambil username dari SharedPreferences
        String username = sharedPreferences.getString("username", "");
        if (!username.isEmpty()) {
            editTextProspek.setText(username);
        }
        editTextProspek.setEnabled(false); // biar tidak bisa diedit manual

        // Ambil data dari Intent (yang dikirim adapter)
        Intent intent = getIntent();
        if (intent != null) {
            // SIMPAN DATA LAMA SEBAGAI IDENTIFIER
            oldNamaProspek = intent.getStringExtra("NAMA_PROSPEK");
            oldNoHp = intent.getStringExtra("NO_HP");

            // Isi otomatis field dengan data lama
            editTextNama.setText(oldNamaProspek);
            editTextEmail.setText(intent.getStringExtra("EMAIL"));
            editTextNoHp.setText(oldNoHp);
            editTextAlamat.setText(intent.getStringExtra("ALAMAT"));

            setSpinnerSelection(spinnerRole, intent.getStringExtra("REFERENSI_PROYEK"));
            setSpinnerSelection(spinnerRole2, intent.getStringExtra("STATUS_NPWP"));
            setSpinnerSelection(spinnerRole3, intent.getStringExtra("STATUS_BPJS"));
        }

        // Filter input No HP
        editTextNoHp.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String original = s.toString();
                String cleaned = original.replaceAll("[^0-9+]", "");
                if (!original.equals(cleaned)) {
                    s.replace(0, s.length(), cleaned);
                }
                isFormatting = false;
            }
        });

        // Tombol edit
        btnEdit.setOnClickListener(v -> updateDataProspek());
        btnBatal.setOnClickListener(v -> finish());

        // Tombol back appbar
        TopAppBar.setNavigationOnClickListener(v -> finish());

        // Bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // Handling insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateDataProspek() {
        String namaPenginput = editTextProspek.getText().toString().trim();
        String namaProspek = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String referensiProyek = spinnerRole.getSelectedItem().toString();
        String statusNpwp = spinnerRole2.getSelectedItem().toString();
        String statusBpjs = spinnerRole3.getSelectedItem().toString();

        if (validateInput(namaProspek, noHp, alamat, email)) {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<BasicResponse> call = apiService.updateProspek(
                    oldNamaProspek,     // Nama prospek lama sebagai identifier
                    oldNoHp,            // No HP lama sebagai identifier
                    namaPenginput,      // data baru
                    namaProspek,        // data baru
                    email,              // data baru
                    noHp,               // data baru
                    alamat,             // data baru
                    referensiProyek,    // data baru
                    statusNpwp,         // data baru
                    statusBpjs          // data baru
            );

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            Toast.makeText(EditDataProspekActivity.this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditDataProspekActivity.this, "Gagal: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditDataProspekActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Toast.makeText(EditDataProspekActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateInput(String namaProspek, String noHp, String alamat, String email) {
        if (namaProspek.isEmpty()) {
            editTextNama.setError("Nama Prospek harus diisi");
            editTextNama.requestFocus();
            return false;
        }
        if (noHp.isEmpty()) {
            editTextNoHp.setError("No. HP harus diisi");
            editTextNoHp.requestFocus();
            return false;
        }
        if (noHp.matches(".*[^0-9+].*")) {
            editTextNoHp.setError("Hanya angka dan + yang diperbolehkan");
            editTextNoHp.requestFocus();
            return false;
        }
        if (alamat.isEmpty()) {
            editTextAlamat.setError("Alamat harus diisi");
            editTextAlamat.requestFocus();
            return false;
        }
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            return false;
        }
        return true;
    }
}