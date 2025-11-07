package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditFasilitasHunianActivity extends AppCompatActivity {

    private EditText editNamaFasilitas, editJumlahFasilitas;
    private Button btnUpdate, btnBatal;
    private MaterialToolbar topAppBar;

    private String namaHunian;
    private FasilitasHunianItem fasilitasData;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_fasilitas_hunian);

        // Terima data dari intent
        Intent intent = getIntent();
        namaHunian = intent.getStringExtra("NAMA_HUNIAN");
        fasilitasData = (FasilitasHunianItem) intent.getSerializableExtra("FASILITAS_DATA");

        Log.d("EditFasilitas", "Menerima data - Nama: " + namaHunian);

        if (namaHunian == null || fasilitasData == null) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews();
        setupToolbar();
        populateData();
        setupListeners();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editNamaFasilitas = findViewById(R.id.editNamaFasilitas);
        editJumlahFasilitas = findViewById(R.id.editJumlahFasilitas);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBatal = findViewById(R.id.btnBatal);
    }

    private void setupToolbar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Fasilitas");
        }

        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void populateData() {
        if (fasilitasData != null) {
            editNamaFasilitas.setText(fasilitasData.getNamaFasilitas());
            editJumlahFasilitas.setText(String.valueOf(fasilitasData.getJumlah()));
        }
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFasilitas();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateFasilitas() {
        String namaFasilitas = editNamaFasilitas.getText().toString().trim();
        String jumlahStr = editJumlahFasilitas.getText().toString().trim();

        if (namaFasilitas.isEmpty()) {
            editNamaFasilitas.setError("Nama fasilitas tidak boleh kosong");
            return;
        }

        if (jumlahStr.isEmpty()) {
            editJumlahFasilitas.setError("Jumlah tidak boleh kosong");
            return;
        }

        int jumlah = Integer.parseInt(jumlahStr);

        if (jumlah <= 0) {
            editJumlahFasilitas.setError("Jumlah harus lebih dari 0");
            return;
        }

        // ========== PERBAIKAN: VALIDASI TAMBAHAN ==========
        if (!validateInput(namaFasilitas, jumlah)) {
            return;
        }

        // ========== PERBAIKAN: SIMPAN DATA LAMA UNTUK BACKUP ==========
        FasilitasHunianItem oldFasilitas = new FasilitasHunianItem();
        oldFasilitas.setIdFasilitas(fasilitasData.getIdFasilitas());
        oldFasilitas.setNamaFasilitas(fasilitasData.getNamaFasilitas());
        oldFasilitas.setJumlah(fasilitasData.getJumlah());
        oldFasilitas.setNamaHunian(fasilitasData.getNamaHunian());

        // Update data fasilitas
        fasilitasData.setNamaFasilitas(namaFasilitas);
        fasilitasData.setJumlah(jumlah);

        // ========== PERBAIKAN: KIRIM DATA TEMPORARY ==========
        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_FASILITAS", fasilitasData);
        resultIntent.putExtra("OLD_FASILITAS", oldFasilitas);
        setResult(RESULT_OK, resultIntent);
        finish();

        // ========== KODE LAMA TETAP ADA (DICOMMENT) ==========
        /*
        // Tampilkan loading
        Toast.makeText(this, "Mengupdate fasilitas...", Toast.LENGTH_SHORT).show();

        // Panggil API untuk update fasilitas
        Call<BasicResponse> call = apiService.updateFasilitasHunian(
                "updateFasilitas",
                fasilitasData.getIdFasilitas(),
                namaFasilitas,
                jumlah
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse updateResponse = response.body();
                    if (updateResponse.isSuccess()) {
                        Toast.makeText(EditFasilitasHunianActivity.this, "Fasilitas berhasil diupdate", Toast.LENGTH_SHORT).show();

                        // Kembali ke activity sebelumnya dengan result OK
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EditFasilitasHunianActivity.this, "Gagal update fasilitas: " + updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditFasilitasHunianActivity.this, "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(EditFasilitasHunianActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EditFasilitas", "Error updating fasilitas: " + t.getMessage());
            }
        });
        */
        // ========== AKHIR KODE LAMA ==========
    }

    // ========== PERBAIKAN: TAMBAH METHOD VALIDASI ==========
    private boolean validateInput(String namaFasilitas, int jumlah) {
        if (namaFasilitas.length() < 2) {
            editNamaFasilitas.setError("Nama fasilitas minimal 2 karakter");
            return false;
        }

        if (jumlah > 1000) {
            editJumlahFasilitas.setError("Jumlah terlalu besar (maksimal 1000)");
            return false;
        }

        // Cek apakah ada perubahan data
        if (namaFasilitas.equals(fasilitasData.getNamaFasilitas()) &&
                jumlah == fasilitasData.getJumlah()) {
            Toast.makeText(this, "Tidak ada perubahan data", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // ========== PERBAIKAN: TAMBAH HANDLER UNTUK PERUBAHAN DATA ==========
    private void checkForChanges() {
        String currentNama = editNamaFasilitas.getText().toString().trim();
        String currentJumlah = editJumlahFasilitas.getText().toString().trim();

        boolean isChanged = !currentNama.equals(fasilitasData.getNamaFasilitas()) ||
                !currentJumlah.equals(String.valueOf(fasilitasData.getJumlah()));

        btnUpdate.setEnabled(isChanged);
    }

    // ========== PERBAIKAN: TAMBAH TEXT WATCHER UNTUK DETEKSI PERUBAHAN ==========
    private void setupTextWatchers() {
        android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkForChanges();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        editNamaFasilitas.addTextChangedListener(textWatcher);
        editJumlahFasilitas.addTextChangedListener(textWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ========== PERBAIKAN: SETUP TEXT WATCHER SAAT ACTIVITY RESUME ==========
        setupTextWatchers();
        checkForChanges(); // Initial check
    }

    @Override
    public void onBackPressed() {
        // ========== PERBAIKAN: TAMBAH KONFIRMASI JIKA ADA PERUBAHAN ==========
        String currentNama = editNamaFasilitas.getText().toString().trim();
        String currentJumlah = editJumlahFasilitas.getText().toString().trim();

        boolean isChanged = !currentNama.equals(fasilitasData.getNamaFasilitas()) ||
                !currentJumlah.equals(String.valueOf(fasilitasData.getJumlah()));

        if (isChanged) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Batal Edit")
                    .setMessage("Perubahan yang belum disimpan akan hilang. Yakin ingin batal?")
                    .setPositiveButton("Ya", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Tidak", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}