package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahProspekActivity extends AppCompatActivity {

    Button Simpan, Batal;
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp, editTextAlamat;
    private Spinner spinnerReferensi, spinnerNPWP, spinnerBPJS;
    private Button btnSimpan, btnBatal;
    private SharedPreferences sharedPreferences;

    // Keys untuk SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambahprospek);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Simpan = findViewById(R.id.btnSimpan);
        Batal = findViewById(R.id.btnBatal);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(TambahProspekActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        Batal.setOnClickListener(v -> {
            Intent intent = new Intent(TambahProspekActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
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

        // Inisialisasi view
        editTextPenginput = findViewById(R.id.editTextProspek);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        spinnerNPWP = findViewById(R.id.spinnerNPWP);
        spinnerBPJS = findViewById(R.id.spinnerBPJS);

        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Setup spinner referensi dari database
        /*setupReferensiSpinner();*/

        // OTOMATIS ISI NAMA PENGINPUT BERDASARKAN USER YANG LOGIN
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        if (!TextUtils.isEmpty(username)) {
            editTextPenginput.setText(username);
            editTextPenginput.setEnabled(false);
        }

        // Filtering karakter khusus untuk nomor HP
        editTextNoHp.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String originalString = s.toString();
                String cleanedString = originalString.replaceAll("[^0-9+]", "");

                if (!originalString.equals(cleanedString)) {
                    s.replace(0, s.length(), cleanedString);
                }
                isFormatting = false;
            }
        });

        // Setup listeners
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanDataProspek();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TambahProspekActivity.this, BerandaActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*private void setupReferensiSpinner() {
        // Ambil data referensi dari database (contoh)
        String[] referensiOptions = {"Website", "Media Sosial", "Rekomendasi", "Iklan", "Lainnya"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, referensiOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReferensi.setAdapter(adapter);
    }*/

    private void simpanDataProspek() {
        String penginput = editTextPenginput.getText().toString().trim();
        String nama = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String referensi = spinnerReferensi.getSelectedItem().toString();
        String statusNpwp = spinnerNPWP.getSelectedItem().toString();
        String statusBpjs = spinnerBPJS.getSelectedItem().toString();

        // Validasi input
        if (TextUtils.isEmpty(penginput)) {
            editTextPenginput.setError("Nama penginput harus diisi");
            editTextPenginput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nama)) {
            editTextNama.setError("Nama lengkap harus diisi");
            editTextNama.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(noHp)) {
            editTextNoHp.setError("No. Handphone harus diisi");
            editTextNoHp.requestFocus();
            return;
        }

        if (noHp.matches(".*[^0-9+].*")) {
            editTextNoHp.setError("Nomor HP hanya boleh mengandung angka dan tanda +");
            editTextNoHp.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(alamat)) {
            editTextAlamat.setError("Alamat harus diisi");
            editTextAlamat.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            return;
        }

        // Simpan data ke MySQL melalui API
        simpanProspekKeMySQL(penginput, nama, email, noHp, alamat, referensi, statusNpwp, statusBpjs);
    }

    private void simpanProspekKeMySQL(String penginput, String nama, String email, String noHp,
                                      String alamat, String referensi, String statusNpwp, String statusBpjs) {

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.tambahProspek(
                penginput, nama, email, noHp, alamat, referensi, statusNpwp, statusBpjs
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse prospekResponse = response.body();

                    if (prospekResponse.isSuccess()) {
                        Toast.makeText(TambahProspekActivity.this, "Data prospek berhasil disimpan", Toast.LENGTH_SHORT).show();
                        clearForm();

                        Intent intent = new Intent(TambahProspekActivity.this, BerandaActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(TambahProspekActivity.this, "Gagal: " + prospekResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TambahProspekActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(TambahProspekActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        editTextNama.setText("");
        editTextEmail.setText("");
        editTextNoHp.setText("");
        editTextAlamat.setText("");
        spinnerReferensi.setSelection(0);
        spinnerNPWP.setSelection(0);
        spinnerBPJS.setSelection(0);
    }
}