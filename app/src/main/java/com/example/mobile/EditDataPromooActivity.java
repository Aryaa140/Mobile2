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
import java.util.HashMap;
import java.util.Map;

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
                        savePromoUpdateToHistori(promoId, namaPromo, penginput, currentImageBase64);
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
    private void savePromoUpdateToHistori(int promoId, String title, String penginput, String imageData) {
        Log.d("EditPromo", "=== SAVE UPDATE HISTORI WITH IMAGE ===");

        // Dapatkan gambar yang valid dari promo yang sudah diupdate
        loadValidImageFromUpdatedPromo(promoId, title, penginput);
    }

    // ✅ METHOD BARU: Ambil gambar valid dari promo yang sudah diupdate
    private void loadValidImageFromUpdatedPromo(int promoId, String title, String penginput) {
        Log.d("EditPromo", "🔄 Loading valid image from updated promo ID: " + promoId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && promoResponse.getData() != null) {

                        // Cari promo yang baru diupdate
                        Promo updatedPromo = null;
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getIdPromo() == promoId) {
                                updatedPromo = promo;
                                break;
                            }
                        }

                        if (updatedPromo != null) {
                            String serverImage = updatedPromo.getGambarBase64();
                            Log.d("EditPromo", "✅ Found updated promo: " + updatedPromo.getNamaPromo());
                            Log.d("EditPromo", "📷 Server image length: " + (serverImage != null ? serverImage.length() : 0));

                            // Gunakan gambar dari server yang sudah terupdate
                            saveUpdateHistoriWithValidImage(promoId, title, penginput, serverImage);
                        } else {
                            Log.e("EditPromo", "❌ Updated promo not found in server response");
                            // Fallback: gunakan gambar lokal
                            saveUpdateHistoriWithValidImage(promoId, title, penginput, currentImageBase64);
                        }
                    } else {
                        Log.e("EditPromo", "❌ Failed to load promo data from server");
                        saveUpdateHistoriWithValidImage(promoId, title, penginput, currentImageBase64);
                    }
                } else {
                    Log.e("EditPromo", "❌ Error loading promo data: " + response.code());
                    saveUpdateHistoriWithValidImage(promoId, title, penginput, currentImageBase64);
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e("EditPromo", "❌ Network error loading promo: " + t.getMessage());
                saveUpdateHistoriWithValidImage(promoId, title, penginput, currentImageBase64);
            }
        });
    }

    // ✅ METHOD BARU: Simpan histori dengan gambar yang sudah divalidasi
    private void saveUpdateHistoriWithValidImage(int promoId, String title, String penginput, String imageData) {
        Log.d("EditPromo", "💾 Saving update histori with validated image");

        // Validasi dan bersihkan data gambar
        String finalImageData = validateAndPrepareImageForNews(imageData);

        Log.d("EditPromo", "📊 Final image data for News: " +
                (finalImageData != null ? "Length=" + finalImageData.length() : "NULL"));

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Gunakan @Body request untuk menghindari pemotongan data
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update_promo_histori");
        requestBody.put("promo_id", promoId);
        requestBody.put("title", title);
        requestBody.put("penginput", penginput);
        requestBody.put("status", "Diubah");
        requestBody.put("image_data", finalImageData != null ? finalImageData : "");

        Log.d("EditPromo", "📤 Sending update histori request with body");

        Call<BasicResponse> call = apiService.updatePromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("EditPromo", "✅ Update histori berhasil disimpan dengan gambar");

                        // Simpan juga ke SharedPreferences untuk backup
                        saveUpdateInfoToPrefs(promoId, title, penginput, finalImageData);

                        // Kirim broadcast untuk refresh NewsActivity
                        sendRefreshBroadcastToNews();

                    } else {
                        Log.e("EditPromo", "❌ Gagal menyimpan update histori: " + basicResponse.getMessage());
                        // Coba method alternatif
                        tryAlternativeUpdateHistori(promoId, title, penginput, finalImageData);
                    }
                } else {
                    Log.e("EditPromo", "❌ Error response update histori: " + response.code());
                    tryAlternativeUpdateHistori(promoId, title, penginput, finalImageData);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("EditPromo", "❌ Network error update histori: " + t.getMessage());
                tryAlternativeUpdateHistori(promoId, title, penginput, finalImageData);
            }
        });
    }

    // ✅ METHOD BARU: Validasi dan persiapan gambar untuk News
    private String validateAndPrepareImageForNews(String imageData) {
        if (imageData == null || imageData.trim().isEmpty()) {
            Log.w("EditPromo", "⚠️ No image data to prepare for News");
            return null;
        }

        String cleanData = imageData.trim();

        // Cek kriteria validitas gambar
        if (cleanData.length() < 500) {
            Log.w("EditPromo", "⚠️ Image data too short for News: " + cleanData.length());
            return null;
        }

        if (cleanData.equals("null") || cleanData.equals("NULL")) {
            Log.w("EditPromo", "⚠️ Image data is string 'null'");
            return null;
        }

        if (cleanData.endsWith("..") || cleanData.endsWith("...")) {
            Log.w("EditPromo", "⚠️ Image data appears truncated");
            return null;
        }

        // Cek format base64
        if (!cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$")) {
            Log.w("EditPromo", "⚠️ Invalid base64 format");
            return null;
        }

        // Test decode untuk memastikan valid
        try {
            byte[] decoded = Base64.decode(cleanData, Base64.DEFAULT);
            if (decoded == null || decoded.length == 0) {
                Log.w("EditPromo", "⚠️ Base64 decode returned empty");
                return null;
            }

            // Cek signature gambar
            if (decoded.length >= 4) {
                // JPEG signature: FF D8 FF
                if ((decoded[0] & 0xFF) == 0xFF && (decoded[1] & 0xFF) == 0xD8 && (decoded[2] & 0xFF) == 0xFF) {
                    Log.d("EditPromo", "✅ Valid JPEG image for News");
                }
                // PNG signature: 89 50 4E 47
                else if ((decoded[0] & 0xFF) == 0x89 && decoded[1] == 0x50 && decoded[2] == 0x4E && decoded[3] == 0x47) {
                    Log.d("EditPromo", "✅ Valid PNG image for News");
                }
                else {
                    Log.w("EditPromo", "⚠️ Unknown image format, but base64 is valid");
                }
            }

            Log.d("EditPromo", "✅ Image prepared for News - Original: " + cleanData.length() +
                    " chars, Decoded: " + decoded.length + " bytes");

            return cleanData;

        } catch (IllegalArgumentException e) {
            Log.e("EditPromo", "❌ Invalid base64 data: " + e.getMessage());
            return null;
        }
    }

    // ✅ METHOD BARU: Simpan info update ke SharedPreferences
    private void saveUpdateInfoToPrefs(int promoId, String title, String penginput, String imageData) {
        SharedPreferences prefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("last_updated_promo_id", promoId);
        editor.putString("last_updated_title", title);
        editor.putString("last_updated_inputter", penginput);
        editor.putString("last_updated_status", "Diubah");

        // Simpan image data hanya jika tidak terlalu besar
        if (imageData != null && imageData.length() < 10000) {
            editor.putString("last_updated_image", imageData);
        } else {
            editor.putString("last_updated_image", "");
        }

        editor.putLong("last_update_time", System.currentTimeMillis());
        editor.apply();

        Log.d("EditPromo", "💾 Update info saved to prefs - ID: " + promoId);
    }

    // ✅ METHOD BARU: Coba method alternatif untuk update histori
    private void tryAlternativeUpdateHistori(int promoId, String title, String penginput, String imageData) {
        Log.d("EditPromo", "🔄 Trying alternative method for update histori");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Coba dengan @FormUrlEncoded sebagai fallback
        Call<BasicResponse> call = apiService.updatePromoHistori(
                "update_promo_histori",
                promoId,
                title,
                penginput,
                "Diubah",
                imageData != null ? imageData : ""
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("EditPromo", "✅ Alternative update histori berhasil");
                        sendRefreshBroadcastToNews();
                    } else {
                        Log.e("EditPromo", "❌ Alternative juga gagal: " + basicResponse.getMessage());
                        // Tetap kirim broadcast meski gagal simpan histori
                        sendRefreshBroadcastToNews();
                    }
                } else {
                    Log.e("EditPromo", "❌ Alternative response error: " + response.code());
                    // Tetap kirim broadcast
                    sendRefreshBroadcastToNews();
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("EditPromo", "❌ Alternative network error: " + t.getMessage());
                // Tetap kirim broadcast
                sendRefreshBroadcastToNews();
            }
        });
    }
    private void sendRefreshBroadcastToNews() {
        Intent refreshIntent = new Intent("REFRESH_NEWS_DATA");
        sendBroadcast(refreshIntent);
        Log.d("EditPromo", "📢 Refresh broadcast sent to NewsActivity");
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