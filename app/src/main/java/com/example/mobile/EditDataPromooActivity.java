package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataPromooActivity extends AppCompatActivity {
    private EditText editTextNamaPromo, editTextPenginput;
    private Spinner spinnerReferensi;
    private Button btnSimpan, btnBatal, btnPilihGambar;
    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar topAppBar;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private int promoId;
    private String currentImageBase64;
    private String originalImageBase64;
    private static final int PICK_IMAGE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_promoo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initViews();
        receiveIntentData();
        setupAutoData();
        setupListeners();
        setupNavigation();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        topAppBar = findViewById(R.id.topAppBar);
        editTextNamaPromo = findViewById(R.id.editTextNama);
        editTextPenginput = findViewById(R.id.editTextProspek);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        btnPilihGambar = findViewById(R.id.btnInputPromo);
    }

    private void setupNavigation() {
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(EditDataPromooActivity.this, NewBeranda.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
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
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            promoId = intent.getIntExtra("PROMO_ID", -1);
            String promoTitle = intent.getStringExtra("PROMO_TITLE");
            String promoInputter = intent.getStringExtra("PROMO_INPUTTER");
            String promoReference = intent.getStringExtra("PROMO_REFERENCE");
            currentImageBase64 = intent.getStringExtra("PROMO_IMAGE");

            originalImageBase64 = currentImageBase64;

            editTextNamaPromo.setText(promoTitle);
            editTextPenginput.setText(promoInputter);
            setSpinnerSelection(promoReference);

            Log.d("EditPromo", "Editing Promo ID: " + promoId);
            Log.d("EditPromo", "Original image length: " +
                    (originalImageBase64 != null ? originalImageBase64.length() : "null"));
        }
    }

    private void setupAutoData() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        if (!username.isEmpty()) {
            editTextPenginput.setText(username);
        }
    }

    private void setSpinnerSelection(String reference) {
        if (reference != null && !reference.isEmpty()) {
            for (int i = 0; i < spinnerReferensi.getCount(); i++) {
                String item = spinnerReferensi.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase(reference)) {
                    spinnerReferensi.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(v -> {
            updatePromo();
        });

        btnBatal.setOnClickListener(v -> {
            finish();
        });

        btnPilihGambar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            processSelectedImage(imageUri);
        }
    }

    private void processSelectedImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                currentImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                baos.close();
                if (inputStream != null) {
                    inputStream.close();
                }

                Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();
                Log.d("EditPromo", "New image length: " + currentImageBase64.length());
            } else {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePromo() {
        String namaPromo = editTextNamaPromo.getText().toString().trim();
        String penginput = editTextPenginput.getText().toString().trim();
        String referensi = spinnerReferensi.getSelectedItem().toString();

        // Validasi dasar
        if (namaPromo.isEmpty() || penginput.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        if (referensi.equals("Pilih Referensi Proyek") || referensi.isEmpty()) {
            Toast.makeText(this, "Harap pilih referensi proyek", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deteksi perubahan
        String originalTitle = getIntent().getStringExtra("PROMO_TITLE");
        String originalInputter = getIntent().getStringExtra("PROMO_INPUTTER");
        String originalReference = getIntent().getStringExtra("PROMO_REFERENCE");

        boolean dataChanged = !namaPromo.equals(originalTitle) ||
                !penginput.equals(originalInputter) ||
                !referensi.equals(originalReference) ||
                isImageChanged();

        Log.d("EditPromo", "=== PERUBAHAN DATA ===");
        Log.d("EditPromo", "Nama berubah: " + !namaPromo.equals(originalTitle));
        Log.d("EditPromo", "Penginput berubah: " + !penginput.equals(originalInputter));
        Log.d("EditPromo", "Referensi berubah: " + !referensi.equals(originalReference));
        Log.d("EditPromo", "Gambar berubah: " + isImageChanged());
        Log.d("EditPromo", "Ada perubahan: " + dataChanged);

        if (!dataChanged) {
            Toast.makeText(this, "Tidak ada perubahan data", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageToSend = currentImageBase64;
        if (!isImageChanged()) {
            imageToSend = null;
            Log.d("EditPromo", "Gambar tidak berubah, kirim null ke server");
        }

        showLoading(true);

        Log.d("EditPromo", "=== DATA AKHIR UNTUK SERVER ===");
        Log.d("EditPromo", "ID: " + promoId);
        Log.d("EditPromo", "Nama: " + namaPromo);
        Log.d("EditPromo", "Penginput: " + penginput);
        Log.d("EditPromo", "Referensi: " + referensi);
        Log.d("EditPromo", "Gambar dikirim: " + (imageToSend != null ? imageToSend.length() + " chars" : "null"));

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updatePromo(
                promoId,
                namaPromo,
                penginput,
                referensi,
                imageToSend
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();

                    if (basicResponse.isSuccess()) {
                        // DAPATKAN USERNAME YANG SEDANG LOGIN
                        String currentUser = editTextPenginput.getText().toString().trim();

                        // ✅ TAMBAHKAN: TAMPILKAN LOCAL NOTIFICATION
                        showLocalUpdateNotification(namaPromo, currentUser);

                        Toast.makeText(EditDataPromooActivity.this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();

                        // KIRIM DATA YANG LENGKAP KE NewBeranda - TAMBAHKAN UPDATED_USER
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("UPDATED_PROMO_ID", promoId);
                        resultIntent.putExtra("UPDATED_IMAGE", isImageChanged() ? currentImageBase64 : null);
                        resultIntent.putExtra("UPDATED_TITLE", namaPromo);
                        resultIntent.putExtra("UPDATED_USER", currentUser); // KIRIM USER INFO
                        resultIntent.putExtra("IS_SUCCESS", true);

                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("IS_SUCCESS", false);
                        resultIntent.putExtra("ERROR_MESSAGE", basicResponse.getMessage());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("IS_SUCCESS", false);
                    resultIntent.putExtra("ERROR_MESSAGE", "Error response dari server: " + response.code());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("IS_SUCCESS", false);
                resultIntent.putExtra("ERROR_MESSAGE", "Koneksi gagal: " + t.getMessage());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    // ✅ METHOD BARU: TAMPILKAN LOCAL NOTIFICATION UNTUK UPDATE
    private void showLocalUpdateNotification(String promoName, String updatedBy) {
        try {
            String title = "Promo Diupdate ✏️";
            String body = "Promo \"" + promoName + "\" berhasil diupdate";

            if (updatedBy != null && !updatedBy.isEmpty()) {
                body += " oleh " + updatedBy;
            }

            // Tampilkan local notification
            NotificationHelper.showSimpleNotification(this, title, body);
            Log.d("EditPromo", "Local update notification shown: " + body);

        } catch (Exception e) {
            Log.e("EditPromo", "Error showing local update notification: " + e.getMessage());
        }
    }

    // Method untuk cek perubahan gambar
    private boolean isImageChanged() {
        if (originalImageBase64 == null && currentImageBase64 == null) {
            return false;
        }
        if (originalImageBase64 == null && currentImageBase64 != null) {
            return true;
        }
        if (originalImageBase64 != null && currentImageBase64 == null) {
            return true;
        }
        return !originalImageBase64.equals(currentImageBase64);
    }

    private void showLoading(boolean isLoading) {
        btnSimpan.setEnabled(!isLoading);
        btnSimpan.setText(isLoading ? "Loading..." : "Ubah");
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}