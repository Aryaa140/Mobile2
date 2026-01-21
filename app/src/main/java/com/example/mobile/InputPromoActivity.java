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

public class InputPromoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "InputPromoActivity";
    private static final int MAX_IMAGE_SIZE = 1024;

    private MaterialToolbar topAppBar;
    private EditText editTextNamaPromo, editTextPenginput, editTextKadaluwarsa;
    private Spinner spinnerReferensi;
    private Button btnPilihGambar, btnSimpan, btnBatal;
    private SharedPreferences sharedPreferences;
    private Uri imageUri;
    private String imageBase64;
    private List<Proyek> proyekList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_promo);

        EdgeToEdge.enable(this);
        calendar = Calendar.getInstance();

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        try {
            initViews();
            setupToolbar();
            setupForm();
            setupButtons();
            setupDatePicker();
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
            editTextNamaPromo = findViewById(R.id.editTextNama);
            editTextPenginput = findViewById(R.id.editTextProspek);
            editTextKadaluwarsa = findViewById(R.id.editTextKadaluwarsa);
            spinnerReferensi = findViewById(R.id.spinnerRole);
            btnPilihGambar = findViewById(R.id.btnInputPromo);
            btnSimpan = findViewById(R.id.btnSimpan);
            btnBatal = findViewById(R.id.btnBatal);

            Log.d(TAG, "All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            throw e;
        }
    }

    private void setupDatePicker() {
        editTextKadaluwarsa.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String selectedDate = dateFormat.format(calendar.getTime());
                    editTextKadaluwarsa.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private boolean validateKadaluwarsa(String kadaluwarsa) {
        if (kadaluwarsa == null || kadaluwarsa.trim().isEmpty()) {
            editTextKadaluwarsa.setError("Tanggal kadaluwarsa harus diisi");
            editTextKadaluwarsa.requestFocus();
            return false;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiryDate = dateFormat.parse(kadaluwarsa);

            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date today = todayCal.getTime();

            if (expiryDate.before(today)) {
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

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            if (isDataChanged()) {
                showUnsavedChangesDialog();
            } else {
                onBackPressed();
            }
        });
    }

    private void setupForm() {
        try {
            String username = sharedPreferences.getString("username", "");
            editTextPenginput.setText(username);
            editTextPenginput.setEnabled(false);

            setupSpinnerWithDefaultData();
            loadProyekDataFromAPI();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up form: " + e.getMessage());
            Toast.makeText(this, "Error setup form", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinnerWithDefaultData() {
        try {
            List<String> defaultOptions = new ArrayList<>();
            defaultOptions.add("Pilih Referensi Proyek");

            spinnerAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    defaultOptions
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (spinnerReferensi != null) {
                spinnerReferensi.setAdapter(spinnerAdapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up default spinner: " + e.getMessage());
        }
    }

    private void loadProyekDataFromAPI() {
        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<ProyekResponse> call = apiService.getProyek();

            call.enqueue(new Callback<ProyekResponse>() {
                @Override
                public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ProyekResponse proyekResponse = response.body();
                        if (proyekResponse.isSuccess()) {
                            updateSpinnerWithProyekData(proyekResponse.getData());
                        } else {
                            handleProyekDataError("Gagal mengambil data proyek: " + proyekResponse.getMessage());
                        }
                    } else {
                        handleProyekDataError("Error response dari server: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ProyekResponse> call, Throwable t) {
                    handleProyekDataError("Koneksi gagal: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading proyek data from API: " + e.getMessage());
            handleProyekDataError("Error sistem: " + e.getMessage());
        }
    }

    private void handleProyekDataError(String errorMessage) {
        Log.e(TAG, "Proyek data error: " + errorMessage);
        runOnUiThread(() -> {
            Toast.makeText(InputPromoActivity.this,
                    errorMessage + ", menggunakan data default", Toast.LENGTH_LONG).show();
            loadFallbackProyekData();
        });
    }

    private void updateSpinnerWithProyekData(List<Proyek> proyekData) {
        try {
            if (proyekData != null && !proyekData.isEmpty()) {
                proyekList.clear();
                proyekList.addAll(proyekData);

                List<String> proyekNames = new ArrayList<>();
                proyekNames.add("Pilih Referensi Proyek");

                for (Proyek proyek : proyekData) {
                    proyekNames.add(proyek.getNamaProyek());
                }

                runOnUiThread(() -> {
                    if (spinnerAdapter != null && spinnerReferensi != null) {
                        spinnerAdapter.clear();
                        spinnerAdapter.addAll(proyekNames);
                        spinnerAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Spinner updated with " + proyekData.size() + " proyek items");
                    }
                });
            } else {
                handleProyekDataError("Tidak ada data proyek tersedia");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating spinner with proyek data: " + e.getMessage());
            handleProyekDataError("Error memproses data proyek");
        }
    }

    private void loadFallbackProyekData() {
        try {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.opsi_spinnerRefrensiProyek,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            if (spinnerReferensi != null) {
                spinnerReferensi.setAdapter(adapter);
            }
            Log.d(TAG, "Using fallback proyek data from resources");
        } catch (Exception e) {
            Log.e(TAG, "Error loading fallback proyek data: " + e.getMessage());
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
                btnPilihGambar.setText("Memproses gambar...");
                btnPilihGambar.setEnabled(false);

                new Thread(() -> {
                    try {
                        imageBase64 = convertImageToBase64(imageUri);

                        runOnUiThread(() -> {
                            btnPilihGambar.setText("Gambar Terpilih ✅");
                            btnPilihGambar.setEnabled(true);
                            Toast.makeText(this, "✅ Gambar berhasil diproses", Toast.LENGTH_SHORT).show();
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error processing image: " + e.getMessage());
                            btnPilihGambar.setText("Pilih Gambar");
                            btnPilihGambar.setEnabled(true);
                            Toast.makeText(this, "❌ Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            imageBase64 = null;
                        });
                    }
                }).start();

            } catch (Exception e) {
                Log.e(TAG, "Error in onActivityResult: " + e.getMessage());
                btnPilihGambar.setText("Pilih Gambar");
                btnPilihGambar.setEnabled(true);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String convertImageToBase64(Uri uri) throws IOException {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Cannot open input stream from URI");
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            options.inSampleSize = calculateInSampleSize(options, 800, 600);
            options.inJustDecodeBounds = false;

            inputStream.close();
            inputStream = getContentResolver().openInputStream(uri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap == null) {
                throw new IOException("Failed to decode bitmap");
            }

            outputStream = new ByteArrayOutputStream();
            boolean compressResult = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

            if (!compressResult) {
                throw new IOException("Failed to compress bitmap");
            }

            byte[] imageBytes = outputStream.toByteArray();
            String base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            Log.d(TAG, "✅ Image conversion successful");
            Log.d(TAG, "📊 Original dimensions: " + options.outWidth + "x" + options.outHeight);
            Log.d(TAG, "📊 Base64 length: " + base64.length());

            bitmap.recycle();

            return base64;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error converting image: " + e.getMessage());
            throw new IOException("Gagal mengkonversi gambar: " + e.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams: " + e.getMessage());
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
            String kadaluwarsa = editTextKadaluwarsa.getText().toString().trim();

            if (namaPromo.isEmpty()) {
                editTextNamaPromo.setError("Nama promo harus diisi");
                editTextNamaPromo.requestFocus();
                return;
            }

            if (referensiProyek.equals("Pilih Referensi Proyek") || referensiProyek.isEmpty()) {
                Toast.makeText(this, "Pilih referensi proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validateKadaluwarsa(kadaluwarsa)) {
                return;
            }

            if (imageBase64 == null || imageBase64.isEmpty()) {
                Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSimpan.setEnabled(false);
            btnSimpan.setText("Menyimpan...");

            callApiSimpanPromo(namaPromo, namaPenginput, referensiProyek, imageBase64, kadaluwarsa);

        } catch (Exception e) {
            Log.e(TAG, "Error in simpanPromo: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resetButtonState();
        }
    }

    // DI InputPromoActivity.java - PERBAIKI METHOD callApiSimpanPromo
    private void callApiSimpanPromo(String namaPromo, String namaPenginput, String referensiProyek, String imageBase64, String kadaluwarsa) {
        Log.d(TAG, "=== CALL API SIMPAN PROMO ===");
        Log.d(TAG, "📅 Kadaluwarsa yang dikirim: " + kadaluwarsa);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.tambahPromo(
                namaPromo,
                namaPenginput,
                referensiProyek,
                imageBase64,
                kadaluwarsa // ✅ PASTIKAN INI DIKIRIM
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "✅ Response Code: " + response.code());

                try {
                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse basicResponse = response.body();
                        if (basicResponse.isSuccess()) {
                            Log.d(TAG, "Promo berhasil disimpan: " + namaPromo);

                            runOnUiThread(() -> {
                                Toast.makeText(InputPromoActivity.this,
                                        "✅ Promo berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                            });

                            // ✅ PERBAIKAN: Simpan histori dengan PROMO_ID yang benar dan KADALUWARSA
                            savePromoHistoriWithActualId(namaPromo, namaPenginput, imageBase64, kadaluwarsa);

                        } else {
                            Log.e(TAG, "❌ Gagal simpan promo: " + basicResponse.getMessage());
                            resetButtonState();
                            runOnUiThread(() -> {
                                Toast.makeText(InputPromoActivity.this,
                                        "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    } else {
                        Log.e(TAG, "❌ Error response: " + response.code());
                        resetButtonState();
                        runOnUiThread(() -> {
                            Toast.makeText(InputPromoActivity.this,
                                    "Error response server", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in API response handling: " + e.getMessage());
                    resetButtonState();
                    runOnUiThread(() -> {
                        Toast.makeText(InputPromoActivity.this, "Error memproses response", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network failure: " + t.getMessage());
                resetButtonState();
                runOnUiThread(() -> {
                    Toast.makeText(InputPromoActivity.this,
                            "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ✅ METHOD BARU: Simpan histori dengan promo_id yang sebenarnya
    private void savePromoHistoriWithActualId(String promoName, String username, String imageBase64, String kadaluwarsa) {
        Log.d(TAG, "🔄 Mencari promo_id untuk histori...");

        // Tunggu 2 detik lalu cari promo_id yang baru dibuat
        new Handler().postDelayed(() -> {
            findAndSavePromoHistori(promoName, username, imageBase64, kadaluwarsa);
        }, 2000);
    }

    // ✅ METHOD BARU: Cari promo_id dan simpan histori
    private void findAndSavePromoHistori(String promoName, String username, String imageBase64, String kadaluwarsa) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && promoResponse.getData() != null) {

                        // Cari promo yang baru dibuat
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getNamaPromo().equals(promoName) &&
                                    promo.getNamaPenginput().equals(username)) {

                                int actualPromoId = promo.getIdPromo();
                                Log.d(TAG, "✅ Found actual promo ID: " + actualPromoId);

                                // Simpan histori dengan promo_id yang benar
                                savePromoHistoriFinal(actualPromoId, promoName, username, imageBase64, kadaluwarsa);
                                return;
                            }
                        }

                        // Jika tidak ditemukan, simpan dengan promo_id = 0
                        Log.w(TAG, "⚠️ Promo not found, saving with promo_id = 0");
                        savePromoHistoriFinal(0, promoName, username, imageBase64, kadaluwarsa);

                    } else {
                        Log.e(TAG, "❌ Failed to get promo data");
                        savePromoHistoriFinal(0, promoName, username, imageBase64, kadaluwarsa);
                    }
                } else {
                    Log.e(TAG, "❌ Error getting promo data");
                    savePromoHistoriFinal(0, promoName, username, imageBase64, kadaluwarsa);
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error getting promo data");
                savePromoHistoriFinal(0, promoName, username, imageBase64, kadaluwarsa);
            }
        });
    }

    // ✅ METHOD BARU: Simpan histori final dengan semua data
    private void savePromoHistoriFinal(int promoId, String title, String penginput, String imageData, String kadaluwarsa) {
        Log.d(TAG, "💾 Saving final histori - PromoID: " + promoId + ", Kadaluwarsa: " + kadaluwarsa);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "add_promo_histori");
        requestBody.put("promo_id", promoId);
        requestBody.put("title", title);
        requestBody.put("penginput", penginput);
        requestBody.put("status", "Ditambahkan");
        requestBody.put("image_data", imageData != null ? imageData : "");
        requestBody.put("kadaluwarsa", kadaluwarsa);

        Call<BasicResponse> call = apiService.addPromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Histori berhasil disimpan dengan kadaluwarsa: " + kadaluwarsa);

                        // Kirim broadcast untuk refresh NewsActivity
                        sendCompleteBroadcastToNews(title, penginput, imageData, kadaluwarsa, promoId);

                    } else {
                        Log.e(TAG, "❌ Gagal simpan histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ Error response histori: " + response.code());
                }
                redirectToNewBeranda();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error histori: " + t.getMessage());
                redirectToNewBeranda();
            }
        });
    }

    // ✅ METHOD BARU: Kirim broadcast lengkap
    private void sendCompleteBroadcastToNews(String title, String user, String imageData, String kadaluwarsa, int promoId) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "NEW_PROMO_ADDED");
            broadcastIntent.putExtra("PROMO_ID", promoId);
            broadcastIntent.putExtra("PROMO_TITLE", title);
            broadcastIntent.putExtra("PENGINPUT", user);
            broadcastIntent.putExtra("IMAGE_DATA", imageData);
            broadcastIntent.putExtra("KADALUWARSA", kadaluwarsa);
            broadcastIntent.putExtra("STATUS", "Ditambahkan");
            broadcastIntent.putExtra("SOURCE", "InputPromoActivity");

            sendBroadcast(broadcastIntent);
            Log.d(TAG, "📢 Complete broadcast sent - Title: " + title + ", Kadaluwarsa: " + kadaluwarsa);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending complete broadcast: " + e.getMessage());
        }
    }

    private void savePromoHistoriDirect(String promoName, String username, String imageBase64) {
        Log.d(TAG, "💾 Saving promo histori directly...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "add_promo_histori");
        requestBody.put("promo_id", 0);
        requestBody.put("title", promoName);
        requestBody.put("penginput", username);
        requestBody.put("status", "Ditambahkan");
        requestBody.put("image_data", imageBase64 != null ? imageBase64 : "");

        // ✅ PERBAIKAN KRITIS: Tambahkan kadaluwarsa dengan benar
        String kadaluwarsa = editTextKadaluwarsa.getText().toString().trim();
        requestBody.put("kadaluwarsa", kadaluwarsa);

        Log.d(TAG, "📤 Sending histori with title: " + promoName + ", kadaluwarsa: " + kadaluwarsa);

        Call<BasicResponse> call = apiService.addPromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Histori berhasil disimpan dengan kadaluwarsa: " + kadaluwarsa);

                        // Kirim broadcast dengan data lengkap
                        sendCompleteBroadcastToNews(promoName, username, imageBase64, kadaluwarsa);

                    } else {
                        Log.e(TAG, "❌ Gagal simpan histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ Error response histori: " + response.code());
                }
                redirectToNewBeranda();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error histori: " + t.getMessage());
                redirectToNewBeranda();
            }
        });
    }

    // ✅ METHOD BARU: Kirim broadcast lengkap ke NewsActivity
    private void sendCompleteBroadcastToNews(String title, String user, String imageData, String kadaluwarsa) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "NEW_PROMO_ADDED");
            broadcastIntent.putExtra("PROMO_TITLE", title);
            broadcastIntent.putExtra("PENGINPUT", user);
            broadcastIntent.putExtra("IMAGE_DATA", imageData);
            broadcastIntent.putExtra("KADALUWARSA", kadaluwarsa);
            broadcastIntent.putExtra("STATUS", "Ditambahkan");
            broadcastIntent.putExtra("SOURCE", "InputPromoActivity");

            sendBroadcast(broadcastIntent);
            Log.d(TAG, "📢 Complete broadcast sent to NewsActivity - Title: " + title + ", Kadaluwarsa: " + kadaluwarsa);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending complete broadcast: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: Kirim broadcast untuk refresh NewsActivity
    private void sendRefreshNewsBroadcast() {
        try {
            Intent refreshIntent = new Intent("REFRESH_NEWS_DATA");
            refreshIntent.putExtra("ACTION", "NEW_PROMO_ADDED");
            refreshIntent.putExtra("SOURCE", "InputPromoActivity");
            sendBroadcast(refreshIntent);
            Log.d(TAG, "📢 Refresh broadcast sent to NewsActivity");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending broadcast: " + e.getMessage());
        }
    }

    // DI INPUTPROMOACTIVITY.JAVA - GANTI METHOD updatePromoIdInHistori
    private void updatePromoIdInHistori(String promoName, String username) {
        Log.d(TAG, "🔄 Updating promo_id in histori for: " + promoName);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && promoResponse.getData() != null) {

                        // Cari promo yang baru dibuat berdasarkan nama dan penginput
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getNamaPromo().equals(promoName) &&
                                    promo.getNamaPenginput().equals(username)) {

                                int actualPromoId = promo.getIdPromo();
                                Log.d(TAG, "✅ Found actual promo ID: " + actualPromoId);

                                // PERBAIKAN: Langsung kirim broadcast dengan promo_id yang benar
                                sendRefreshBroadcastWithPromoId(actualPromoId);
                                break;
                            }
                        }
                    }
                }
                // Redirect setelah selesai
                redirectToNewBeranda();
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error getting promo data: " + t.getMessage());
                redirectToNewBeranda();
            }
        });
    }

    // PERBAIKAN: Method untuk kirim broadcast dengan promo_id
    private void sendRefreshBroadcastWithPromoId(int promoId) {
        Intent refreshIntent = new Intent("REFRESH_NEWS_DATA");
        refreshIntent.putExtra("PROMO_ID", promoId);
        refreshIntent.putExtra("ACTION", "NEW_PROMO");
        sendBroadcast(refreshIntent);
        Log.d(TAG, "📢 Refresh broadcast sent with Promo ID: " + promoId);
    }

    private void sendRefreshBroadcast() {
        Intent refreshIntent = new Intent("REFRESH_NEWS_DATA");
        sendBroadcast(refreshIntent);
        Log.d(TAG, "📢 Refresh broadcast sent to NewsActivity");
    }

    private void redirectToNewBeranda() {
        runOnUiThread(() -> {
            Intent intent = new Intent(InputPromoActivity.this, NewBeranda.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
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