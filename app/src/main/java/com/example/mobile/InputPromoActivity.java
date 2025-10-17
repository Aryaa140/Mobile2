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
    private static final int MAX_IMAGE_SIZE = 1024;

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
        setContentView(R.layout.activity_input_promo);

        EdgeToEdge.enable(this);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        initViews();
        setupToolbar();
        setupForm();
        setupButtons();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        try {
            topAppBar = findViewById(R.id.topAppBar);
            editTextNamaPromo = findViewById(R.id.editTextNama);
            editTextPenginput = findViewById(R.id.editTextProspek);
            spinnerReferensi = findViewById(R.id.spinnerRole);
            btnPilihGambar = findViewById(R.id.btnInputPromo);
            btnSimpan = findViewById(R.id.btnSimpan);
            btnBatal = findViewById(R.id.btnBatal);

            Log.d(TAG, "All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error inisialisasi komponen", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupToolbar() {
        try {
            if (topAppBar != null) {
                topAppBar.setNavigationOnClickListener(v -> {
                    if (isDataChanged()) {
                        showUnsavedChangesDialog();
                    } else {
                        onBackPressed();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage());
        }
    }

    private void setupForm() {
        try {
            // Auto-isi nama penginput dari SharedPreferences
            String username = sharedPreferences.getString("username", "");
            if (editTextPenginput != null) {
                editTextPenginput.setText(username);
                editTextPenginput.setEnabled(false);
            }

            loadReferensiData();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up form: " + e.getMessage());
        }
    }

    private void loadReferensiData() {
        try {
            if (spinnerReferensi != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        this,
                        R.array.opsi_spinnerRefrensiProyek,
                        android.R.layout.simple_spinner_item
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReferensi.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading spinner data: " + e.getMessage());
            if (spinnerReferensi != null) {
                String[] defaultData = {"Pilih Referensi Proyek", "Proyek A", "Proyek B", "Proyek C"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultData);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReferensi.setAdapter(adapter);
            }
        }
    }

    private void setupButtons() {
        try {
            if (btnPilihGambar != null) {
                btnPilihGambar.setOnClickListener(v -> pilihGambar());
            }

            if (btnSimpan != null) {
                btnSimpan.setOnClickListener(v -> simpanPromo());
            }

            if (btnBatal != null) {
                btnBatal.setOnClickListener(v -> {
                    if (isDataChanged()) {
                        showUnsavedChangesDialog();
                    } else {
                        finish();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up buttons: " + e.getMessage());
        }
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
                if (btnPilihGambar != null) {
                    btnPilihGambar.setText("Gambar Terpilih");
                }
                imageBase64 = convertImageToBase64(imageUri);
                Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Gambar Base64 length: " + (imageBase64 != null ? imageBase64.length() : 0));
            } catch (Exception e) {
                Log.e(TAG, "Error processing image: " + e.getMessage());
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_LONG).show();
                if (btnPilihGambar != null) {
                    btnPilihGambar.setText("Pilih Gambar");
                }
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

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
            options.inJustDecodeBounds = false;

            inputStream.close();
            inputStream = getContentResolver().openInputStream(uri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap == null) {
                throw new IOException("Failed to decode bitmap");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

            byte[] imageBytes = outputStream.toByteArray();

            bitmap.recycle();
            outputStream.close();

            return Base64.encodeToString(imageBytes, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e(TAG, "Error in convertImageToBase64: " + e.getMessage());
            throw e;
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
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error calculating inSampleSize: " + e.getMessage());
            return 1;
        }
    }

    private void simpanPromo() {
        try {
            if (editTextNamaPromo == null || spinnerReferensi == null || btnSimpan == null) {
                Toast.makeText(this, "Error: Komponen tidak terinisialisasi", Toast.LENGTH_SHORT).show();
                return;
            }

            String namaPromo = editTextNamaPromo.getText().toString().trim();
            String namaPenginput = editTextPenginput != null ? editTextPenginput.getText().toString().trim() : "";
            String referensiProyek = spinnerReferensi.getSelectedItem() != null ?
                    spinnerReferensi.getSelectedItem().toString() : "";

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

            btnSimpan.setEnabled(false);
            btnSimpan.setText("Menyimpan...");

            callApiSimpanPromo(namaPromo, namaPenginput, referensiProyek, imageBase64);

        } catch (Exception e) {
            Log.e(TAG, "Error in simpanPromo: " + e.getMessage());
            Toast.makeText(this, "Error saat menyimpan promo", Toast.LENGTH_LONG).show();
            resetButtonState();
        }
    }

    private void callApiSimpanPromo(String namaPromo, String namaPenginput, String referensiProyek, String imageBase64) {
        try {
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

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            BasicResponse basicResponse = response.body();
                            if (basicResponse.isSuccess()) {
                                runOnUiThread(() -> {
                                    Toast.makeText(InputPromoActivity.this, "Promo berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                                });

                                Log.d(TAG, "Promo berhasil disimpan: " + namaPromo);

                                // ✅ Tampilkan local notification saja
                                showLocalSuccessNotification(namaPromo, namaPenginput);

                                // ✅ FCM NOTIFICATION AKAN DIKIRIM OTOMATIS OLEH PHP
                                // TIDAK PERLU POLLING LAGI

                                new android.os.Handler().postDelayed(() -> {
                                    runOnUiThread(() -> {
                                        if (!isFinishing() && !isDestroyed()) {
                                            finish();
                                        }
                                    });
                                }, 1500);

                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(InputPromoActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                String errorMsg = "Error dari server: " + response.code();
                                Toast.makeText(InputPromoActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in API response handling: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(InputPromoActivity.this, "Error memproses response", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    resetButtonState();
                    Log.e(TAG, "Network error: " + t.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(InputPromoActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            resetButtonState();
            Log.e(TAG, "Error calling API: " + e.getMessage());
            runOnUiThread(() -> {
                Toast.makeText(this, "Error sistem", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showLocalSuccessNotification(String promoName, String addedBy) {
        try {
            String title = "Promo Ditambahkan ✅";
            String body = "Promo \"" + promoName + "\" berhasil ditambahkan";

            if (this != null && !isFinishing() && !isDestroyed()) {
                NotificationHelper.showPromoNotification(
                        this,
                        title,
                        body,
                        null
                );
                Log.d(TAG, "Local notification shown: " + body);
            } else {
                Log.w(TAG, "Activity not available for showing notification");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing local notification: " + e.getMessage());
        }
    }

    private void resetButtonState() {
        try {
            if (btnSimpan != null) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resetting button state: " + e.getMessage());
        }
    }

    private boolean isDataChanged() {
        try {
            if (editTextNamaPromo != null) {
                String namaPromo = editTextNamaPromo.getText().toString().trim();
                return !namaPromo.isEmpty() || (imageBase64 != null && !imageBase64.isEmpty());
            }
            return imageBase64 != null && !imageBase64.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Error checking data changes: " + e.getMessage());
            return false;
        }
    }

    private void showUnsavedChangesDialog() {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Perubahan Belum Disimpan")
                    .setMessage("Anda memiliki perubahan yang belum disimpan. Yakin ingin keluar?")
                    .setPositiveButton("Ya", (dialog, which) -> finish())
                    .setNegativeButton("Tidak", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog: " + e.getMessage());
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (isDataChanged()) {
                showUnsavedChangesDialog();
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onBackPressed: " + e.getMessage());
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageBase64 = null;
        imageUri = null;
    }
}