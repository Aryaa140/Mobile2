package com.example.mobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataPromooActivity extends AppCompatActivity {
    private EditText editTextNamaPromo, editTextPenginput, editTextKadaluwarsa;
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
    private String originalKadaluwarsa;
    private static final int PICK_IMAGE_REQUEST = 100;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_promoo);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initViews();
        receiveIntentData();
        loadProyekDataForSpinner();
        setupAutoData();
        setupListeners();
        setupNavigation();
        setupDatePicker();

        Log.d("EditPromo", "=== ✅ ACTIVITY CREATED WITH PROYEK DATA ===");
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        topAppBar = findViewById(R.id.topAppBar);
        editTextNamaPromo = findViewById(R.id.editTextNama);
        editTextPenginput = findViewById(R.id.editTextProspek);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        btnPilihGambar = findViewById(R.id.btnInputPromo);
        editTextKadaluwarsa = findViewById(R.id.editTextKadaluwarsa);
    }

    private void setupDatePicker() {
        editTextKadaluwarsa.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        try {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }

            if (originalKadaluwarsa != null && !originalKadaluwarsa.isEmpty() && !originalKadaluwarsa.equals("null")) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date existingDate = dateFormat.parse(originalKadaluwarsa);
                    if (existingDate != null) {
                        calendar.setTime(existingDate);
                        Log.d("EditPromo", "✅ Set calendar to existing date: " + originalKadaluwarsa);
                    }
                } catch (ParseException e) {
                    Log.e("EditPromo", "Error parsing existing date: " + e.getMessage());
                    calendar = Calendar.getInstance();
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            Log.d("EditPromo", "📅 DatePicker - Year: " + year + ", Month: " + month + ", Day: " + day);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String selectedDate = dateFormat.format(selectedCalendar.getTime());

                        editTextKadaluwarsa.setText(selectedDate);
                        Log.d("EditPromo", "✅ Date selected: " + selectedDate);
                    },
                    year, month, day
            );

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();

            Log.d("EditPromo", "✅ DatePickerDialog shown successfully");

        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error showing DatePickerDialog: " + e.getMessage());
            Toast.makeText(this, "Error menampilkan date picker", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateKadaluwarsa(String kadaluwarsa) {
        if (kadaluwarsa == null || kadaluwarsa.trim().isEmpty()) {
            editTextKadaluwarsa.setError("Tanggal kadaluwarsa harus diisi");
            editTextKadaluwarsa.requestFocus();
            return false;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateFormat.setLenient(false);

            Date expiryDate = dateFormat.parse(kadaluwarsa);

            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date todayStart = todayCal.getTime();

            if (expiryDate.before(todayStart)) {
                editTextKadaluwarsa.setError("Tanggal kadaluwarsa tidak boleh sebelum hari ini");
                editTextKadaluwarsa.requestFocus();
                return false;
            }

            return true;
        } catch (ParseException e) {
            editTextKadaluwarsa.setError("Format tanggal tidak valid. Gunakan format YYYY-MM-DD");
            editTextKadaluwarsa.requestFocus();
            return false;
        }
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
            originalKadaluwarsa = intent.getStringExtra("PROMO_KADALUWARSA");

            Log.d("EditPromo", "Received KADALUWARSA from intent: " + originalKadaluwarsa);

            originalImageBase64 = currentImageBase64;

            editTextNamaPromo.setText(promoTitle);
            editTextPenginput.setText(promoInputter);

            if (originalKadaluwarsa != null && !originalKadaluwarsa.isEmpty() && !originalKadaluwarsa.equals("null")) {
                editTextKadaluwarsa.setText(originalKadaluwarsa);
                Log.d("EditPromo", "Kadaluwarsa set to EditText: " + originalKadaluwarsa);
            } else {
                editTextKadaluwarsa.setText("");
                Log.w("EditPromo", "Kadaluwarsa is null, empty, or 'null' string");
            }

            setSpinnerSelection(promoReference);

            Log.d("EditPromo", "Editing Promo ID: " + promoId);
            Log.d("EditPromo", "Kadaluwarsa: " + originalKadaluwarsa);
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

    // PERBAIKAN METHOD processSelectedImage
    private void processSelectedImage(Uri imageUri) {
        try {
            Log.d("EditPromo", "📷 Processing selected image: " + imageUri);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Gagal membuka gambar", Toast.LENGTH_SHORT).show();
                return;
            }

            // PERBAIKAN: Decode bitmap dengan options yang optimal
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            // Hitung sample size
            options.inSampleSize = calculateInSampleSize(options, 800, 800);
            options.inJustDecodeBounds = false;

            // Tutup dan buka stream lagi
            inputStream.close();
            inputStream = getContentResolver().openInputStream(imageUri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

            if (bitmap != null) {
                // PERBAIKAN: Compress dengan kualitas yang baik
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos); // Kualitas 85%
                byte[] imageBytes = baos.toByteArray();

                // PERBAIKAN: Encode ke base64 tanpa line breaks
                currentImageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                baos.close();
                inputStream.close();

                // Debug gambar hasil
                Log.d("EditPromo", "✅ Gambar berhasil diproses");
                Log.d("EditPromo", "📊 Bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                Log.d("EditPromo", "📊 Base64 length: " + currentImageBase64.length());
                Log.d("EditPromo", "📊 Base64 first 50: " + currentImageBase64.substring(0, Math.min(50, currentImageBase64.length())));

                Toast.makeText(this, "Gambar berhasil dipilih (" + imageBytes.length/1024 + " KB)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
                Log.e("EditPromo", "❌ Bitmap is null after decoding");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("EditPromo", "❌ Error processing image: " + e.getMessage());
        }
    }

    // ✅ TAMBAHKAN METHOD calculateInSampleSize
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d("EditPromo", "📏 Calculated sample size: " + inSampleSize + " for " + width + "x" + height);
        return inSampleSize;
    }

    private void updatePromo() {
        String namaPromo = editTextNamaPromo.getText().toString().trim();
        String penginput = editTextPenginput.getText().toString().trim();
        String referensi = spinnerReferensi.getSelectedItem().toString();
        String kadaluwarsa = editTextKadaluwarsa.getText().toString().trim();

        if (!validateKadaluwarsa(kadaluwarsa)) {
            return;
        }

        String imageToSend = determineImageToSend();

        Log.d("EditPromo", "🎯 Final image decision - Length: " +
                (imageToSend != null ? imageToSend.length() : "NULL") +
                ", Kadaluwarsa: " + kadaluwarsa);

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updatePromo(
                promoId,
                namaPromo,
                penginput,
                referensi,
                imageToSend,
                kadaluwarsa // ✅ Pastikan kadaluwarsa dikirim
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();

                    if (basicResponse.isSuccess()) {
                        Log.d("EditPromo", "🎉 UPDATE PROMO SUCCESS");

                        // ✅ PERBAIKAN: Kirim broadcast dengan data LENGKAP termasuk kadaluwarsa
                        sendCompleteUpdateBroadcast(namaPromo, penginput, imageToSend, kadaluwarsa);

                        Toast.makeText(EditDataPromooActivity.this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("UPDATED_PROMO_ID", promoId);
                        resultIntent.putExtra("UPDATED_TITLE", namaPromo);
                        resultIntent.putExtra("UPDATED_IMAGE", imageToSend);
                        resultIntent.putExtra("UPDATED_USER", penginput);
                        resultIntent.putExtra("UPDATED_KADALUWARSA", kadaluwarsa); // ✅ TAMBAHKAN
                        resultIntent.putExtra("IS_SUCCESS", true);
                        setResult(RESULT_OK, resultIntent);

                        finish();

                    } else {
                        Log.e("EditPromo", "❌ Server response failed: " + basicResponse.getMessage());
                        Toast.makeText(EditDataPromooActivity.this, "Gagal update: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("EditPromo", "❌ HTTP Error: " + response.code());
                    Toast.makeText(EditDataPromooActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Log.e("EditPromo", "❌ Network Error: " + t.getMessage());
                Toast.makeText(EditDataPromooActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ PERBAIKAN: Method broadcast dengan kadaluwarsa
    private void sendCompleteUpdateBroadcast(String title, String user, String imageData, String kadaluwarsa) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "PROMO_UPDATED");
            broadcastIntent.putExtra("PROMO_ID", promoId);
            broadcastIntent.putExtra("UPDATED_IMAGE", imageData != null ? imageData : "");
            broadcastIntent.putExtra("UPDATED_TITLE", title);
            broadcastIntent.putExtra("UPDATED_USER", user);
            broadcastIntent.putExtra("UPDATED_KADALUWARSA", kadaluwarsa); // ✅ TAMBAHKAN
            broadcastIntent.putExtra("STATUS", "Diubah");
            broadcastIntent.putExtra("SOURCE", "EditPromoActivity");

            sendBroadcast(broadcastIntent);
            Log.d("EditPromo", "📢 Complete broadcast sent - ID: " + promoId +
                    ", Kadaluwarsa: " + kadaluwarsa);
        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error sending complete broadcast: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: Tentukan gambar mana yang akan dikirim
    private String determineImageToSend() {
        // Prioritas 1: Gambar baru yang dipilih user
        if (currentImageBase64 != null && isValidImageData(currentImageBase64)) {
            Log.d("EditPromo", "🎯 Using NEW image - Length: " + currentImageBase64.length());
            return currentImageBase64;
        }

        // Prioritas 2: Gambar original (jika tidak ada gambar baru)
        if (originalImageBase64 != null && isValidImageData(originalImageBase64)) {
            Log.d("EditPromo", "🎯 Using ORIGINAL image - Length: " + originalImageBase64.length());
            return originalImageBase64;
        }

        // Prioritas 3: Fallback - load dari server (jika perlu)
        Log.e("EditPromo", "⚠️ No valid image found, using empty string");
        return ""; // Jangan return null, karena PHP akan error
    }

    private void savePromoUpdateToHistoriWithImage(String title, String penginput, String imageData, String kadaluwarsa) {
        Log.d("EditPromo", "💾 Saving update histori with kadaluwarsa: " + kadaluwarsa);

        String finalImageData = validateImageForHistori(imageData);

        if (finalImageData.isEmpty()) {
            Log.w("EditPromo", "🔄 Skip save histori - no valid image data");
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update_promo_histori");
        requestBody.put("promo_id", promoId);
        requestBody.put("title", title);
        requestBody.put("penginput", penginput);
        requestBody.put("status", "Diubah");
        requestBody.put("image_data", finalImageData);
        requestBody.put("kadaluwarsa", kadaluwarsa); // ✅ KIRIM KADALUWARSA

        Log.d("EditPromo", "📤 Sending update histori - Image: " + finalImageData.length() + " chars, Kadaluwarsa: " + kadaluwarsa);

        Call<BasicResponse> call = apiService.updatePromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("EditPromo", "✅ Update histori berhasil dengan gambar");
                    } else {
                        Log.e("EditPromo", "❌ Gagal simpan update histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e("EditPromo", "❌ Error response update histori: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("EditPromo", "❌ Network error update histori: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Validasi gambar untuk histori
    private String validateImageForHistori(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return "";
        }

        String cleanData = imageData.trim();

        // Kriteria validasi untuk histori
        boolean isValid = cleanData.length() >= 500 && // Minimal 500 karakter untuk gambar yang valid
                !cleanData.equals("null") &&
                !cleanData.equals("NULL") &&
                !cleanData.endsWith("..") &&
                !cleanData.endsWith("...") &&
                cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$");

        Log.d("EditPromo", "🖼️ Histori image validation - Length: " + cleanData.length() + ", Valid: " + isValid);

        return isValid ? cleanData : "";
    }

    // ✅ METHOD BARU: Simpan histori update dengan gambar yang benar
    private void savePromoUpdateToHistoriWithCorrectImage(String title, String penginput, String kadaluwarsa) {
        Log.d("EditPromo", "💾 Saving update histori with CORRECT image");

        // ✅ TENTUKAN GAMBAR YANG AKAN DIKIRIM
        String imageForHistori;
        if (currentImageBase64 != null && isValidImageData(currentImageBase64)) {
            imageForHistori = currentImageBase64;
            Log.d("EditPromo", "📷 Using CURRENT image for histori - Length: " + currentImageBase64.length());
        } else if (originalImageBase64 != null && isValidImageData(originalImageBase64)) {
            imageForHistori = originalImageBase64;
            Log.d("EditPromo", "📷 Using ORIGINAL image for histori - Length: " + originalImageBase64.length());
        } else {
            imageForHistori = "";
            Log.d("EditPromo", "📷 No valid image for histori");
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update_promo_histori");
        requestBody.put("promo_id", promoId);
        requestBody.put("title", title);
        requestBody.put("penginput", penginput);
        requestBody.put("status", "Diubah");
        requestBody.put("image_data", imageForHistori);
        requestBody.put("kadaluwarsa", kadaluwarsa);

        Log.d("EditPromo", "📤 Sending update histori - Image length: " + imageForHistori.length());

        Call<BasicResponse> call = apiService.updatePromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("EditPromo", "✅ Update histori berhasil dengan gambar");
                    } else {
                        Log.e("EditPromo", "❌ Gagal simpan update histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e("EditPromo", "❌ Error response update histori: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("EditPromo", "❌ Network error update histori: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Kirim broadcast lengkap ke NewsActivity
    private void sendCompleteUpdateBroadcast(String title, String user, String imageData) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "PROMO_UPDATED");
            broadcastIntent.putExtra("PROMO_ID", promoId);
            broadcastIntent.putExtra("UPDATED_IMAGE", imageData);
            broadcastIntent.putExtra("UPDATED_TITLE", title);
            broadcastIntent.putExtra("UPDATED_USER", user);
            broadcastIntent.putExtra("STATUS", "Diubah");
            broadcastIntent.putExtra("SOURCE", "EditPromoActivity");

            sendBroadcast(broadcastIntent);
            Log.d("EditPromo", "📢 Complete broadcast sent - ID: " + promoId +
                    ", Image: " + (imageData != null ? imageData.length() + " chars" : "null"));
        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error sending complete broadcast: " + e.getMessage());
        }
    }


    // ✅ METHOD BARU: Kirim broadcast dengan gambar
    private void sendRefreshNewsBroadcastWithImage(String imageData) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("PROMO_ID", promoId);
            broadcastIntent.putExtra("UPDATED_IMAGE", imageData);
            broadcastIntent.putExtra("UPDATED_TITLE", editTextNamaPromo.getText().toString());
            broadcastIntent.putExtra("UPDATED_USER", editTextPenginput.getText().toString());
            broadcastIntent.putExtra("STATUS", "Diubah");
            broadcastIntent.putExtra("SOURCE", "EditPromoActivity");

            sendBroadcast(broadcastIntent);
            Log.d("EditPromo", "📢 Refresh broadcast sent with image - Length: " + imageData.length());
        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error sending broadcast: " + e.getMessage());
        }
    }

    // ✅ PERBAIKAN: Validasi gambar yang lebih baik
    private boolean isValidImageData(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return false;
        }

        String cleanData = imageData.trim();

        // Kriteria validasi gambar
        boolean isValid = cleanData.length() >= 100 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL") &&
                cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$");

        Log.d("EditPromo", "🖼️ Image validation - Length: " + cleanData.length() + ", Valid: " + isValid);
        return isValid;
    }

    // ✅ METHOD BARU: Kirim broadcast ke NewsActivity
    private void sendRefreshNewsBroadcast() {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "PROMO_UPDATED");
            broadcastIntent.putExtra("PROMO_ID", promoId);
            broadcastIntent.putExtra("SOURCE", "EditPromoActivity");
            sendBroadcast(broadcastIntent);
            Log.d("EditPromo", "📢 Refresh broadcast sent to NewsActivity");
        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error sending broadcast: " + e.getMessage());
        }
    }


    // ✅ METHOD BARU: Kirim broadcast dengan data lengkap
    private void sendRefreshBroadcastToNewsWithData(int promoId, String imageData, String title, String user) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("PROMO_ID", promoId);
            broadcastIntent.putExtra("UPDATED_IMAGE", imageData != null ? imageData : "");
            broadcastIntent.putExtra("PROMO_TITLE", title);
            broadcastIntent.putExtra("PENGINPUT", user);
            broadcastIntent.putExtra("STATUS", "Diubah");

            sendBroadcast(broadcastIntent);
            Log.d("EditPromo", "📢 Additional broadcast sent to NewsActivity - ID: " + promoId);
        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error sending additional broadcast: " + e.getMessage());
        }
    }

    // ✅ PERBAIKAN METHOD isImageChanged() - LEBIH AKURAT
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

        // PERBAIKAN: Bandingkan string base64 secara akurat
        boolean changed = !originalImageBase64.equals(currentImageBase64);
        Log.d("EditPromo", "🔍 Image Changed Check: " + changed);
        Log.d("EditPromo", "Original starts with: " + (originalImageBase64 != null ? originalImageBase64.substring(0, Math.min(50, originalImageBase64.length())) : "null"));
        Log.d("EditPromo", "Current starts with: " + (currentImageBase64 != null ? currentImageBase64.substring(0, Math.min(50, currentImageBase64.length())) : "null"));

        return changed;
    }

    // ✅ PERBAIKAN METHOD: Simpan histori update dengan pengecekan duplikasi yang lebih ketat
    private void savePromoUpdateToHistori(String title, String penginput, String imageData, String kadaluwarsa) {
        Log.d("EditPromo", "=== SAVE UPDATE HISTORI DENGAN ANTI-DUPLIKASI ===");

        // ✅ PERBAIKAN: Cek apakah gambar benar-benar berubah
        if (!isImageReallyChanged(imageData)) {
            Log.d("EditPromo", "🔄 Skip save histori - no significant changes detected");
            return;
        }

        // ✅ PERBAIKAN: Validasi gambar sebelum kirim ke histori
        String finalImageData = validateImageDataForHistori(imageData);

        // ✅ PERBAIKAN: Jika gambar tidak valid, jangan simpan histori
        if (finalImageData.isEmpty()) {
            Log.d("EditPromo", "🔄 Skip save histori - no valid image data");
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "update_promo_histori");
        requestBody.put("promo_id", promoId);
        requestBody.put("title", title);
        requestBody.put("penginput", penginput);
        requestBody.put("status", "Diubah");
        requestBody.put("image_data", finalImageData);
        requestBody.put("kadaluwarsa", kadaluwarsa);

        Log.d("EditPromo", "📤 Sending SINGLE update histori - ID: " + promoId + ", Image: " +
                finalImageData.length() + " chars");

        Call<BasicResponse> call = apiService.updatePromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("EditPromo", "✅ SINGLE update histori berhasil disimpan");

                    } else {
                        Log.e("EditPromo", "❌ Gagal simpan update histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e("EditPromo", "❌ Error response update histori: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("EditPromo", "❌ Network error update histori: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Cek apakah gambar benar-benar berubah
    private boolean isImageReallyChanged(String newImageData) {
        if (originalImageBase64 == null && newImageData == null) {
            return false;
        }
        if (originalImageBase64 == null && newImageData != null) {
            return true;
        }
        if (originalImageBase64 != null && newImageData == null) {
            return true;
        }

        // ✅ PERBAIKAN: Bandingkan hanya 100 karakter pertama untuk efisiensi
        String originalShort = originalImageBase64.length() > 100 ?
                originalImageBase64.substring(0, 100) : originalImageBase64;
        String newShort = newImageData.length() > 100 ?
                newImageData.substring(0, 100) : newImageData;

        boolean changed = !originalShort.equals(newShort);
        Log.d("EditPromo", "🔍 Image really changed: " + changed);

        return changed;
    }

    // ✅ METHOD BARU: Validasi gambar untuk histori
    private String validateImageDataForHistori(String imageData) {
        if (imageData == null || imageData.isEmpty() || imageData.equals("null")) {
            Log.d("EditPromo", "🖼️ No valid image data for histori");
            return "";
        }

        String cleanData = imageData.trim();

        // Validasi base64 - kriteria lebih ketat
        if (cleanData.length() < 500 ||
                cleanData.equals("null") ||
                cleanData.equals("NULL") ||
                !cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$")) {
            Log.w("EditPromo", "🖼️ Invalid image data for histori, using empty");
            return "";
        }

        Log.d("EditPromo", "🖼️ Valid image data for histori: " + cleanData.length() + " chars");
        return cleanData;
    }

    private void loadProyekDataForSpinner() {
        Log.d("EditPromo", "🔄 Loading proyek data for spinner...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProyekResponse> call = apiService.getProyekForPromo();

        call.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProyekResponse proyekResponse = response.body();

                    if (proyekResponse.isSuccess() && proyekResponse.getData() != null) {
                        List<Proyek> proyekList = proyekResponse.getData();
                        setupProyekSpinner(proyekList);

                        Log.d("EditPromo", "✅ Loaded " + proyekList.size() + " proyek items");

                        setSpinnerSelectionFromIntent();

                    } else {
                        Log.e("EditPromo", "❌ Failed to load proyek data: " +
                                (proyekResponse != null ? proyekResponse.getMessage() : "null response"));
                        setupDefaultSpinner();
                    }
                } else {
                    Log.e("EditPromo", "❌ Error response loading proyek: " + response.code());
                    setupDefaultSpinner();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Log.e("EditPromo", "❌ Network error loading proyek: " + t.getMessage());
                setupDefaultSpinner();
            }
        });
    }

    private void setupProyekSpinner(List<Proyek> proyekList) {
        try {
            List<String> proyekNames = new ArrayList<>();
            proyekNames.add("Pilih Referensi Proyek");

            for (Proyek proyek : proyekList) {
                if (proyek.getNamaProyek() != null && !proyek.getNamaProyek().isEmpty()) {
                    proyekNames.add(proyek.getNamaProyek());
                    Log.d("EditPromo", "📝 Added proyek to spinner: " + proyek.getNamaProyek());
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    proyekNames
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReferensi.setAdapter(adapter);

            Log.d("EditPromo", "✅ Spinner setup with " + proyekNames.size() + " items");

        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error setting up proyek spinner: " + e.getMessage());
            setupDefaultSpinner();
        }
    }

    private void setupDefaultSpinner() {
        try {
            String[] defaultItems = {"Pilih Referensi Proyek", "Proyek A", "Proyek B", "Proyek C"};
            ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    defaultItems
            );
            defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerReferensi.setAdapter(defaultAdapter);

            Log.w("EditPromo", "⚠ Using default spinner due to loading failure");

            setSpinnerSelectionFromIntent();

        } catch (Exception e) {
            Log.e("EditPromo", "❌ Critical error setting up default spinner: " + e.getMessage());
        }
    }

    private void setSpinnerSelectionFromIntent() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                String promoReference = intent.getStringExtra("PROMO_REFERENCE");
                if (promoReference != null && !promoReference.isEmpty()) {
                    setSpinnerSelection(promoReference);
                    Log.d("EditPromo", "✅ Spinner selection set to: " + promoReference);
                } else {
                    Log.w("EditPromo", "⚠ No reference data from intent");
                    spinnerReferensi.setSelection(0);
                }
            }
        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error setting spinner selection: " + e.getMessage());
            spinnerReferensi.setSelection(0);
        }
    }

    private void setSpinnerSelection(String reference) {
        if (reference == null || reference.isEmpty()) {
            Log.w("EditPromo", "⚠ Reference is null or empty, setting to default");
            spinnerReferensi.setSelection(0);
            return;
        }

        try {
            ArrayAdapter adapter = (ArrayAdapter) spinnerReferensi.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    String item = adapter.getItem(i).toString();
                    if (item.equalsIgnoreCase(reference)) {
                        spinnerReferensi.setSelection(i);
                        Log.d("EditPromo", "✅ Spinner set to position: " + i + " - " + item);
                        return;
                    }
                }

                Log.w("EditPromo", "⚠ Reference '" + reference + "' not found in spinner, setting to default");
                spinnerReferensi.setSelection(0);
            }
        } catch (Exception e) {
            Log.e("EditPromo", "❌ Error in setSpinnerSelection: " + e.getMessage());
            spinnerReferensi.setSelection(0);
        }
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