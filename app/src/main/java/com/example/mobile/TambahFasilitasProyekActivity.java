package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahFasilitasProyekActivity extends AppCompatActivity {

    private static final String TAG = "TambahFasilitasProyek";
    private static final int PICK_IMAGE_REQUEST = 1;

    private MaterialToolbar topAppBar;
    private EditText editNamaFasilitas;
    private ImageView imagePreviewFasilitas;
    private Button btnPilihGambarFasilitas, btnSimpan, btnBatal;

    private ApiService apiService;
    private String namaProyek;
    private Bitmap selectedFasilitasBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_fasilitas_proyek);

        // Terima data proyek dari intent
        Intent intent = getIntent();
        namaProyek = intent.getStringExtra("NAMA_PROYEK");

        if (namaProyek == null) {
            Toast.makeText(this, "Data proyek tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Menerima data proyek: " + namaProyek);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editNamaFasilitas = findViewById(R.id.editNamaFasilitas);
        imagePreviewFasilitas = findViewById(R.id.imagePreviewFasilitas);
        btnPilihGambarFasilitas = findViewById(R.id.btnPilihGambarFasilitas);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
    }

    private void setupToolbar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tambah Fasilitas - " + namaProyek);
        }

        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupListeners() {
        btnPilihGambarFasilitas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pilihGambar();
            }
        });

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

    private void pilihGambar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                selectedFasilitasBitmap = bitmap;
                imagePreviewFasilitas.setImageBitmap(bitmap);
                Toast.makeText(this, "Gambar fasilitas berhasil dipilih", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void tambahFasilitas() {
        String namaFasilitas = editNamaFasilitas.getText().toString().trim();

        if (namaFasilitas.isEmpty()) {
            editNamaFasilitas.setError("Nama fasilitas tidak boleh kosong");
            return;
        }

        if (selectedFasilitasBitmap == null) {
            Toast.makeText(this, "Pilih gambar fasilitas terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ DAPATKAN USER INFO DARI SHAREDPREFERENCES
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String namaUser = sharedPreferences.getString("nama_user", username);

        // Jika nama_user tidak ada, gunakan username
        if (namaUser.isEmpty()) {
            namaUser = username;
        }

        Log.d(TAG, "User info - Username: " + username + ", Nama: " + namaUser);

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Konversi bitmap ke base64
        String gambarBase64 = bitmapToBase64(selectedFasilitasBitmap);

        Log.d(TAG, "Menambah fasilitas - Proyek: " + namaProyek +
                ", Fasilitas: " + namaFasilitas +
                ", Username: " + username +
                ", Created By: " + namaUser);

        // ✅ PERBAIKAN: Gunakan API dengan parameter yang lengkap
        Call<BasicResponse> call = apiService.addFasilitas(
                "addFasilitas",
                namaFasilitas,
                namaProyek,
                gambarBase64,
                username,      // ✅ Parameter ke-5: username
                namaUser       // ✅ Parameter ke-6: created_by
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse tambahResponse = response.body();
                    if (tambahResponse.isSuccess()) {
                        Toast.makeText(TambahFasilitasProyekActivity.this,
                                "Fasilitas berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                        // ✅ TAMPILKAN INFO FCM JIKA ADA
                        if (tambahResponse.getFcmNotification() != null) {
                            Log.d(TAG, "FCM Notification Result: " +
                                    tambahResponse.getFcmNotification());
                        }

                        // Kembali ke activity sebelumnya dengan result OK
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(TambahFasilitasProyekActivity.this,
                                "Gagal menambah fasilitas: " + tambahResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Error response: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(TambahFasilitasProyekActivity.this,
                            errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");
                Toast.makeText(TambahFasilitasProyekActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error adding fasilitas: " + t.getMessage());
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to base64: " + e.getMessage());
            return "";
        }
    }

    @Override
    public void onBackPressed() {
        // Konfirmasi jika ada data yang sudah diinput
        String namaFasilitas = editNamaFasilitas.getText().toString().trim();

        if (!namaFasilitas.isEmpty() || selectedFasilitasBitmap != null) {
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