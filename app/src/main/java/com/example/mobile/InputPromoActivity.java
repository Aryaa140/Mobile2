package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputPromoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private MaterialToolbar topAppBar;
    private EditText editTextNamaPromo, editTextPenginput;
    private Spinner spinnerReferensi;
    private Button btnPilihGambar, btnSimpan, btnBatal;
    private SharedPreferences sharedPreferences;
    private Uri imageUri;
    private String imageBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_promo_activity);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Inisialisasi views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup form
        setupForm();

        // Setup buttons
        setupButtons();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editTextNamaPromo = findViewById(R.id.editTextNama);
        editTextPenginput = findViewById(R.id.editTextProspek);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        btnPilihGambar = findViewById(R.id.btnInputPromo);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupForm() {
        // Auto-isi nama penginput dari SharedPreferences
        String username = sharedPreferences.getString("username", "");
        editTextPenginput.setText(username);
        editTextPenginput.setEnabled(false);

        // Load data referensi proyek (jika ada)
        loadReferensiData();
    }

    private void loadReferensiData() {
        // Jika Anda memiliki data referensi dari database, load di sini
        // Contoh sederhana menggunakan array resources
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.opsi_spinnerProspek,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReferensi.setAdapter(adapter);
    }

    private void setupButtons() {
        btnPilihGambar.setOnClickListener(v -> pilihGambar());

        btnSimpan.setOnClickListener(v -> simpanPromo());

        btnBatal.setOnClickListener(v -> finish());
    }

    private void pilihGambar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            btnPilihGambar.setText("Gambar Terpilih");

            // Convert image to Base64
            try {
                imageBase64 = convertImageToBase64(imageUri);
                Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private String convertImageToBase64(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void simpanPromo() {
        String namaPromo = editTextNamaPromo.getText().toString().trim();
        String namaPenginput = editTextPenginput.getText().toString().trim();
        String referensiProyek = spinnerReferensi.getSelectedItem().toString();

        // Validasi input
        if (namaPromo.isEmpty()) {
            editTextNamaPromo.setError("Nama promo harus diisi");
            editTextNamaPromo.requestFocus();
            return;
        }

        if (imageBase64 == null || imageBase64.isEmpty()) {
            Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Panggil API untuk menyimpan data
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.tambahPromo(
                namaPromo,
                namaPenginput,
                referensiProyek,
                imageBase64
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Toast.makeText(InputPromoActivity.this, "Promo berhasil disimpan", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(InputPromoActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputPromoActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");
                Toast.makeText(InputPromoActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("InputPromo", "Error: " + t.getMessage());
            }
        });
    }
}