package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataProspekActivity extends AppCompatActivity {
    MaterialToolbar topAppBar;
    BottomNavigationView bottomNavigationView;

    private EditText editTextProspek, editTextNama, editTextEmail, editTextNoHp, editTextAlamat;
    private Spinner spinnerRole, spinnerRole2, spinnerRole3, spinnerStatusLib;
    private Button btnEdit, btnBatal; // HAPUS btnHistoriStatus
    private SharedPreferences sharedPreferences;

    private List<String> statusLibList = new ArrayList<>();
    private ArrayAdapter<String> statusLibAdapter;

    // Array untuk spinner statis
    private String[] referensiProyekOptions = {"Pribadi", "Marketing", "Facebook", "Instagram", "Website", "Lainnya"};
    private String[] npwpOptions = {"Ya", "Tidak"};
    private String[] bpjsOptions = {"Ya", "Tidak"};

    // Simpan data lama untuk identifier
    private String oldNamaProspek = "";
    private String oldNoHp = "";
    private String oldStatusLib = "";
    private int idProspek = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_prospek);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupSpinners();
        loadIntentData();
        loadStatusLibData();
        setupListeners();
    }

    private void initViews() {
        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Inisialisasi view
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        editTextProspek = findViewById(R.id.editTextProspek);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);

        spinnerRole = findViewById(R.id.spinnerRole);
        spinnerRole2 = findViewById(R.id.spinnerRole2);
        spinnerRole3 = findViewById(R.id.spinnerRole3);
        spinnerStatusLib = findViewById(R.id.spinnerStatusLib);

        btnEdit = findViewById(R.id.btnEdit);
        btnBatal = findViewById(R.id.btnBatal);
        // HAPUS: btnHistoriStatus = findViewById(R.id.btnHistoriStatus);

        // Set nama penginput dari SharedPreferences
        String username = sharedPreferences.getString("username", "");
        editTextProspek.setText(username);
        editTextProspek.setEnabled(false);
    }

    private void setupSpinners() {
        // Setup spinner untuk referensi proyek
        ArrayAdapter<String> referensiAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, referensiProyekOptions);
        referensiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(referensiAdapter);

        // Setup spinner untuk NPWP
        ArrayAdapter<String> npwpAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, npwpOptions);
        npwpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole2.setAdapter(npwpAdapter);

        // Setup spinner untuk BPJS
        ArrayAdapter<String> bpjsAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, bpjsOptions);
        bpjsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole3.setAdapter(bpjsAdapter);

        // Setup spinner untuk Status Lib (akan diisi dari API)
        statusLibAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusLibList);
        statusLibAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusLib.setAdapter(statusLibAdapter);
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Ambil semua data dari intent
            idProspek = intent.getIntExtra("ID_PROSPEK", -1);
            oldNamaProspek = intent.getStringExtra("NAMA_PROSPEK");
            oldNoHp = intent.getStringExtra("NO_HP");
            oldStatusLib = intent.getStringExtra("STATUS_LIB");

            // Isi data ke form
            editTextNama.setText(oldNamaProspek);
            editTextEmail.setText(intent.getStringExtra("EMAIL"));
            editTextNoHp.setText(oldNoHp);
            editTextAlamat.setText(intent.getStringExtra("ALAMAT"));

            // Set spinner selection
            setSpinnerSelection(spinnerRole, intent.getStringExtra("REFERENSI_PROYEK"));
            setSpinnerSelection(spinnerRole2, intent.getStringExtra("STATUS_NPWP"));
            setSpinnerSelection(spinnerRole3, intent.getStringExtra("STATUS_BPJS"));

            // Status lib akan di-set setelah data diambil dari API
        }
    }

    private void loadStatusLibData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<StatusLibResponse> call = apiService.getStatusLib();

        call.enqueue(new Callback<StatusLibResponse>() {
            @Override
            public void onResponse(Call<StatusLibResponse> call, Response<StatusLibResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatusLibResponse statusLibResponse = response.body();
                    if (statusLibResponse.isSuccess()) {
                        statusLibList.clear();
                        statusLibList.add("Pilih Status Lib"); // Item pertama sebagai placeholder
                        statusLibList.addAll(statusLibResponse.getStatusList());
                        statusLibAdapter.notifyDataSetChanged();

                        // Set selection berdasarkan data dari intent
                        if (oldStatusLib != null && !oldStatusLib.isEmpty()) {
                            setSpinnerSelection(spinnerStatusLib, oldStatusLib);
                        } else {
                            spinnerStatusLib.setSelection(0);
                        }
                    } else {
                        showToast("Gagal memuat data status lib: " + statusLibResponse.getMessage());
                    }
                } else {
                    showToast("Error response dari server");
                }
            }

            @Override
            public void onFailure(Call<StatusLibResponse> call, Throwable t) {
                showToast("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    private void setupListeners() {
        // Filter input No HP (hanya angka dan +)
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

                String original = s.toString();
                String cleaned = original.replaceAll("[^0-9+]", "");

                if (!original.equals(cleaned)) {
                    s.replace(0, s.length(), cleaned);
                }

                isFormatting = false;
            }
        });

        // Tombol Simpan
        btnEdit.setOnClickListener(v -> updateDataProspek());

        // Tombol Batal
        btnBatal.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // HAPUS SEMUA KODE TOMBOL HISTORI STATUS

        // Tombol back pada toolbar
        topAppBar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(this, NewBeranda.class);
            } else if (id == R.id.nav_folder) {
                intent = new Intent(this, LihatDataActivity.class);
            } else if (id == R.id.nav_news) {
                intent = new Intent(this, NewsActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null && !value.isEmpty()) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    private void updateDataProspek() {
        // Validasi input
        if (!validateInput()) {
            return;
        }

        // Ambil data dari form
        String namaPenginput = editTextProspek.getText().toString().trim();
        String namaProspek = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String referensiProyek = spinnerRole.getSelectedItem().toString();
        String statusNpwp = spinnerRole2.getSelectedItem().toString();
        String statusBpjs = spinnerRole3.getSelectedItem().toString();
        String newStatusLib = spinnerStatusLib.getSelectedItem().toString();

        // Cek jika status lib berubah
        boolean statusLibChanged = !newStatusLib.equals(oldStatusLib) &&
                spinnerStatusLib.getSelectedItemPosition() > 0;

        // Panggil API update prospek
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updateProspek(
                oldNamaProspek,
                oldNoHp,
                namaPenginput,
                namaProspek,
                email,
                noHp,
                alamat,
                referensiProyek,
                statusNpwp,
                statusBpjs,
                newStatusLib
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        // Jika status lib berubah, simpan histori
                        if (statusLibChanged && idProspek != -1) {
                            simpanHistoriStatusLib(oldStatusLib, newStatusLib);
                        }

                        showToast("Data berhasil diupdate");
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showToast("Gagal: " + basicResponse.getMessage());
                    }
                } else {
                    showToast("Error response dari server");
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showToast("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    private void simpanHistoriStatusLib(String statusLama, String statusBaru) {
        if (idProspek == -1) return;

        // Format: "Cold berubah menjadi Follow up"
        String perubahanStatus = statusLama + " berubah menjadi " + statusBaru;
        String tanggalPerubahan = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.simpanHistoriStatusLib(
                idProspek,
                perubahanStatus,
                tanggalPerubahan
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                // Log untuk debugging
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("HistoriStatus", "Histori berhasil disimpan: " + response.body().getMessage());
                } else {
                    Log.e("HistoriStatus", "Gagal menyimpan histori");
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("HistoriStatus", "Error: " + t.getMessage());
            }
        });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validasi Nama Prospek
        if (editTextNama.getText().toString().trim().isEmpty()) {
            editTextNama.setError("Nama Prospek harus diisi");
            editTextNama.requestFocus();
            isValid = false;
        }

        // Validasi No HP
        String noHp = editTextNoHp.getText().toString().trim();
        if (noHp.isEmpty()) {
            editTextNoHp.setError("No. HP harus diisi");
            editTextNoHp.requestFocus();
            isValid = false;
        } else if (!noHp.matches("[0-9+]+")) {
            editTextNoHp.setError("Hanya angka dan + yang diperbolehkan");
            editTextNoHp.requestFocus();
            isValid = false;
        }

        // Validasi Alamat
        if (editTextAlamat.getText().toString().trim().isEmpty()) {
            editTextAlamat.setError("Alamat harus diisi");
            editTextAlamat.requestFocus();
            isValid = false;
        }

        // Validasi Email (optional)
        String email = editTextEmail.getText().toString().trim();
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            isValid = false;
        }

        // Validasi Status Lib (tidak boleh item placeholder)
        if (spinnerStatusLib.getSelectedItemPosition() == 0) {
            showToast("Status Lib harus dipilih");
            isValid = false;
        }

        return isValid;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}