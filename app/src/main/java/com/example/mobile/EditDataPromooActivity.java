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
    private String originalImageBase64; // PERBAIKAN: Simpan gambar original
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
                startActivity(new Intent(this, BerandaActivity.class));
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

            // PERBAIKAN: Simpan gambar original untuk comparison
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

            // Decode bitmap dengan optimization
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

            if (bitmap != null) {
                // Compress ke JPEG
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                currentImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Clean up
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

        // PERBAIKAN: Deteksi perubahan yang lebih akurat
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

        // PERBAIKAN: Tentukan apa yang akan dikirim ke server
        String imageToSend = currentImageBase64;

        // Jika gambar tidak berubah, kirim null ke server (biarkan server handle)
        if (!isImageChanged()) {
            imageToSend = null;
            Log.d("EditPromo", "Gambar tidak berubah, kirim null ke server");
        }

        showLoading(true);

        // Debug final data
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
                        Toast.makeText(EditDataPromooActivity.this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();

                        // PERBAIKAN: Selalu kirim gambar terbaru ke BerandaActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("UPDATED_PROMO_ID", promoId);

                        // Jika gambar diubah, kirim yang baru; jika tidak, kirim yang original
                        String imageToReturn = isImageChanged() ? currentImageBase64 : originalImageBase64;
                        resultIntent.putExtra("UPDATED_IMAGE", imageToReturn);

                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(EditDataPromooActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("EditPromo", "Server error: " + basicResponse.getMessage());
                    }
                } else {
                    Toast.makeText(EditDataPromooActivity.this, "Error response dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditDataPromooActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EditPromo", "Network error: " + t.getMessage());
            }
        });
    }

    // PERBAIKAN: Method untuk cek perubahan gambar
    private boolean isImageChanged() {
        if (originalImageBase64 == null && currentImageBase64 == null) {
            return false; // Keduanya null, tidak ada perubahan
        }
        if (originalImageBase64 == null && currentImageBase64 != null) {
            return true; // Dari null ke ada gambar
        }
        if (originalImageBase64 != null && currentImageBase64 == null) {
            return true; // Dari ada gambar ke null
        }
        // Bandingkan string base64 (gunakan substring untuk efisiensi)
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

