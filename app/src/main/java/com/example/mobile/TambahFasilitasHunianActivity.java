package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahFasilitasHunianActivity extends AppCompatActivity {

    private EditText editNamaFasilitas, editJumlahFasilitas;
    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private String namaHunian;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_fasilitas_hunian);

        // Terima data dari intent
        Intent intent = getIntent();
        namaHunian = intent.getStringExtra("NAMA_HUNIAN");

        if (namaHunian == null) {
            Toast.makeText(this, "Data hunian tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editNamaFasilitas = findViewById(R.id.editNamaFasilitas);
        editJumlahFasilitas = findViewById(R.id.editJumlahFasilitas);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
    }

    private void setupToolbar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tambah Fasilitas");
        }

        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tambahFasilitas();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void tambahFasilitas() {
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

        // ========== PERBAIKAN: TAMBAH MODE TEMPORARY ==========
        // Buat objek fasilitas baru untuk temporary data
        FasilitasHunianItem newFasilitas = new FasilitasHunianItem();
        newFasilitas.setNamaFasilitas(namaFasilitas);
        newFasilitas.setJumlah(jumlah);
        newFasilitas.setNamaHunian(namaHunian);

        // Kembalikan ke activity sebelumnya dengan data temporary
        Intent resultIntent = new Intent();
        resultIntent.putExtra("NEW_FASILITAS", newFasilitas);
        setResult(RESULT_OK, resultIntent);
        finish();

        // ========== KODE LAMA TETAP ADA (DICOMMENT) ==========
        /*
        // Tampilkan loading
        Toast.makeText(this, "Menambahkan fasilitas...", Toast.LENGTH_SHORT).show();

        // Panggil API untuk tambah fasilitas
        Call<BasicResponse> call = apiService.addFasilitasHunian(
                "addFasilitas",
                namaHunian,
                namaFasilitas,
                jumlah
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse tambahResponse = response.body();
                    if (tambahResponse.isSuccess()) {
                        Toast.makeText(TambahFasilitasHunianActivity.this, "Fasilitas berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                        // Kembali ke activity sebelumnya dengan result OK
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(TambahFasilitasHunianActivity.this, "Gagal menambah fasilitas: " + tambahResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TambahFasilitasHunianActivity.this, "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(TambahFasilitasHunianActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        */
        // ========== AKHIR KODE LAMA ==========
    }

    // ========== PERBAIKAN: TAMBAH METHOD UNTUK VALIDASI LANJUTAN ==========
    private boolean validateInput(String namaFasilitas, int jumlah) {
        if (namaFasilitas.length() < 2) {
            editNamaFasilitas.setError("Nama fasilitas minimal 2 karakter");
            return false;
        }

        if (jumlah > 1000) {
            editJumlahFasilitas.setError("Jumlah terlalu besar (maksimal 1000)");
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        // ========== PERBAIKAN: TAMBAH KONFIRMASI JIKA ADA INPUT ==========
        String namaFasilitas = editNamaFasilitas.getText().toString().trim();
        String jumlahStr = editJumlahFasilitas.getText().toString().trim();

        if (!namaFasilitas.isEmpty() || !jumlahStr.isEmpty()) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Batal Tambah Fasilitas")
                    .setMessage("Data yang sudah diinput akan hilang. Yakin ingin batal?")
                    .setPositiveButton("Ya", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Tidak", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}