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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputPromoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "InputPromoActivity";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height untuk resize

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

        // PERBAIKAN: Set content view dulu sebelum init views
        setContentView(R.layout.activity_input_promo);

        // PERBAIKAN: EdgeToEdge setelah setContentView
        EdgeToEdge.enable(this);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Inisialisasi views dengan try-catch
        try {
            initViews();
            setupToolbar();
            setupForm();
            setupButtons();
        } catch (Exception e) {
            Log.e(TAG, "Error during initialization: " + e.getMessage());
            Toast.makeText(this, "Error inisialisasi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        try {
            topAppBar = findViewById(R.id.topAppBar);
            if (topAppBar == null) {
                throw new RuntimeException("topAppBar not found");
            }

            editTextNamaPromo = findViewById(R.id.editTextNama);
            if (editTextNamaPromo == null) {
                throw new RuntimeException("editTextNama not found");
            }

            editTextPenginput = findViewById(R.id.editTextProspek);
            if (editTextPenginput == null) {
                throw new RuntimeException("editTextProspek not found");
            }

            spinnerReferensi = findViewById(R.id.spinnerRole);
            if (spinnerReferensi == null) {
                throw new RuntimeException("spinnerRole not found");
            }

            btnPilihGambar = findViewById(R.id.btnInputPromo);
            if (btnPilihGambar == null) {
                throw new RuntimeException("btnInputPromo not found");
            }

            btnSimpan = findViewById(R.id.btnSimpan);
            if (btnSimpan == null) {
                throw new RuntimeException("btnSimpan not found");
            }

            btnBatal = findViewById(R.id.btnBatal);
            if (btnBatal == null) {
                throw new RuntimeException("btnBatal not found");
            }

            Log.d(TAG, "All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            throw e; // Re-throw untuk ditangkap di caller
        }
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            // PERBAIKAN: Tambahkan konfirmasi sebelum keluar
            if (isDataChanged()) {
                showUnsavedChangesDialog();
            } else {
                onBackPressed();
            }
        });
    }

    private void setupForm() {
        try {
            // Auto-isi nama penginput dari SharedPreferences
            String username = sharedPreferences.getString("username", "");
            editTextPenginput.setText(username);
            editTextPenginput.setEnabled(false);

            // Load data referensi proyek
            loadReferensiData();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up form: " + e.getMessage());
            Toast.makeText(this, "Error setup form", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadReferensiData() {
        try {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.opsi_spinnerRefrensiProyek,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReferensi.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error loading spinner data: " + e.getMessage());
            // Fallback: buat adapter dengan data default
            String[] defaultData = {"Pilih Referensi Proyek", "Proyek A", "Proyek B", "Proyek C"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultData);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReferensi.setAdapter(adapter);
        }
    }

    private void setupButtons() {
        btnPilihGambar.setOnClickListener(v -> pilihGambar());
        btnSimpan.setOnClickListener(v -> simpanPromo());
        btnBatal.setOnClickListener(v -> {
            if (isDataChanged()) {
                showUnsavedChangesDialog();
            } else {
                finish();
            }
        });
    }

    private void pilihGambar() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Log.e(TAG, "Error opening image picker: " + e.getMessage());
            Toast.makeText(this, "Tidak dapat membuka galeri", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                btnPilihGambar.setText("Gambar Terpilih");
                imageBase64 = convertImageToBase64(imageUri);
                Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Gambar Base64 length: " + (imageBase64 != null ? imageBase64.length() : 0));
            } catch (Exception e) {
                Log.e(TAG, "Error processing image: " + e.getMessage());
                Toast.makeText(this, "Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                btnPilihGambar.setText("Pilih Gambar");
                imageBase64 = null;
            }
        }
    }

    private String convertImageToBase64(Uri uri) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Cannot open input stream from URI");
            }

            // Decode dengan options untuk mengurangi memory usage
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            // Hitung sample size untuk resize
            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
            options.inJustDecodeBounds = false;

            // Tutup stream dan buka lagi
            inputStream.close();
            inputStream = getContentResolver().openInputStream(uri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap == null) {
                throw new IOException("Failed to decode bitmap");
            }

            // Compress image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream); // Kurangi kualitas jadi 70%

            byte[] imageBytes = outputStream.toByteArray();

            // Bersihkan memory
            bitmap.recycle();

            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream: " + e.getMessage());
                }
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void simpanPromo() {
        try {
            String namaPromo = editTextNamaPromo.getText().toString().trim();
            String namaPenginput = editTextPenginput.getText().toString().trim();
            String referensiProyek = spinnerReferensi.getSelectedItem() != null ?
                    spinnerReferensi.getSelectedItem().toString() : "";

            // Validasi input
            if (namaPromo.isEmpty()) {
                editTextNamaPromo.setError("Nama promo harus diisi");
                editTextNamaPromo.requestFocus();
                return;
            }

            if (referensiProyek.equals("Pilih Referensi Proyek") || referensiProyek.isEmpty()) {
                Toast.makeText(this, "Pilih referensi proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageBase64 == null || imageBase64.isEmpty()) {
                Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tampilkan loading
            btnSimpan.setEnabled(false);
            btnSimpan.setText("Menyimpan...");

            // Panggil API
            callApiSimpanPromo(namaPromo, namaPenginput, referensiProyek, imageBase64);

        } catch (Exception e) {
            Log.e(TAG, "Error in simpanPromo: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resetButtonState();
        }
    }

    private void callApiSimpanPromo(String namaPromo, String namaPenginput, String referensiProyek, String imageBase64) {
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
                resetButtonState();

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        String username = loginPrefs.getString("username", "User");

                        // ✅ GUNAKAN METHOD YANG SUDAH ADA
                        String message = "Promo \"" + namaPromo + "\" telah ditambahkan oleh " + username;
                        NotificationUtils.showInfoNotification(InputPromoActivity.this, "Promo Ditambahkan", message);

                        finish();
                    } else {
                        // Untuk error server, cukup toast saja (opsional)
                        Toast.makeText(InputPromoActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputPromoActivity.this, "Error dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                resetButtonState();
                Toast.makeText(InputPromoActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetButtonState() {
        btnSimpan.setEnabled(true);
        btnSimpan.setText("Simpan");
    }

    private boolean isDataChanged() {
        String namaPromo = editTextNamaPromo.getText().toString().trim();
        return !namaPromo.isEmpty() || (imageBase64 != null && !imageBase64.isEmpty());
    }

    private void showUnsavedChangesDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Perubahan Belum Disimpan")
                .setMessage("Anda memiliki perubahan yang belum disimpan. Yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> finish())
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isDataChanged()) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
        }
    }
}