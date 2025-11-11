package com.example.mobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
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
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height untuk resize

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

        // PERBAIKAN: Set content view dulu sebelum init views
        setContentView(R.layout.activity_input_promo);

        // PERBAIKAN: EdgeToEdge setelah setContentView
        EdgeToEdge.enable(this);
        // Inisialisasi calendar untuk date picker
        calendar = Calendar.getInstance();

        // Inisialisasi SharedPreferences
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

            // TAMBAHKAN INISIALISASI EDIT TEXT KADALUWARSA
            editTextKadaluwarsa = findViewById(R.id.editTextKadaluwarsa);
            if (editTextKadaluwarsa == null) {
                throw new RuntimeException("editTextKadaluwarsa not found");
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
    // TAMBAHKAN METHOD UNTUK SETUP DATE PICKER
    private void setupDatePicker() {
        editTextKadaluwarsa.setOnClickListener(v -> showDatePickerDialog());
    }

    // TAMBAHKAN METHOD UNTUK MENAMPILKAN DATE PICKER DIALOG
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Format tanggal menjadi yyyy-MM-dd
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String selectedDate = dateFormat.format(calendar.getTime());
                    editTextKadaluwarsa.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimal date ke hari ini (tidak boleh memilih tanggal sebelum hari ini)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    // TAMBAHKAN METHOD VALIDASI TANGGAL KADALUWARSA
    private boolean validateKadaluwarsa(String kadaluwarsa) {
        if (kadaluwarsa == null || kadaluwarsa.trim().isEmpty()) {
            editTextKadaluwarsa.setError("Tanggal kadaluwarsa harus diisi");
            editTextKadaluwarsa.requestFocus();
            return false;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiryDate = dateFormat.parse(kadaluwarsa);
            Date today = new Date();

            // Validasi: tanggal kadaluwarsa tidak boleh sebelum hari ini
            if (expiryDate.before(today)) {
                editTextKadaluwarsa.setError("Tanggal kadaluwarsa tidak boleh sebelum hari ini");
                editTextKadaluwarsa.requestFocus();
                return false;
            }

            return true;
        } catch (ParseException e) {
            editTextKadaluwarsa.setError("Format tanggal tidak valid");
            editTextKadaluwarsa.requestFocus();
            return false;
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

            setupSpinnerWithDefaultData();

            // Load data referensi proyek
            loadProyekDataFromAPI();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up form: " + e.getMessage());
            Toast.makeText(this, "Error setup form", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpinnerWithDefaultData() {
        try {
            // Setup adapter dengan data default
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
            // Tetap gunakan data default yang sudah ada
            Toast.makeText(InputPromoActivity.this,
                    errorMessage + ", menggunakan data default", Toast.LENGTH_LONG).show();

            // Fallback ke data lokal jika API gagal
            loadFallbackProyekData();
        });
    }


    private void updateSpinnerWithProyekData(List<Proyek> proyekData) {
        try {
            if (proyekData != null && !proyekData.isEmpty()) {
                proyekList.clear();
                proyekList.addAll(proyekData);

                // Update spinner adapter dengan data proyek
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
            // Fallback ke data dari resource jika API gagal
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
                // Tampilkan loading
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

            // Decode dengan options untuk mengurangi memory usage
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            // Hitung sample size untuk resize
            options.inSampleSize = calculateInSampleSize(options, 800, 600);
            options.inJustDecodeBounds = false;

            // Tutup stream dan buka lagi
            inputStream.close();
            inputStream = getContentResolver().openInputStream(uri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (bitmap == null) {
                throw new IOException("Failed to decode bitmap");
            }

            // Compress image dengan kualitas optimal
            outputStream = new ByteArrayOutputStream();
            boolean compressResult = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

            if (!compressResult) {
                throw new IOException("Failed to compress bitmap");
            }

            byte[] imageBytes = outputStream.toByteArray();
            String base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Debug logging
            Log.d(TAG, "✅ Image conversion successful");
            Log.d(TAG, "📊 Original dimensions: " + options.outWidth + "x" + options.outHeight);
            Log.d(TAG, "📊 Base64 length: " + base64.length());
            Log.d(TAG, "📊 First 50 chars: " + (base64.length() > 50 ? base64.substring(0, 50) : base64));
            Log.d(TAG, "📊 Last 50 chars: " + (base64.length() > 50 ? base64.substring(base64.length() - 50) : base64));

            // Bersihkan memory
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

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
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
            callApiSimpanPromo(namaPromo, namaPenginput, referensiProyek, imageBase64,kadaluwarsa);

        } catch (Exception e) {
            Log.e(TAG, "Error in simpanPromo: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resetButtonState();
        }
    }

    // ✅ PERBAIKAN: Ganti method callApiSimpanPromo
    private void callApiSimpanPromo(String namaPromo, String namaPenginput, String referensiProyek, String imageBase64, String kadaluwarsa) {
        Log.d(TAG, "=== CALL API SIMPAN PROMO ===");
        Log.d(TAG, "Kadaluwarsa: " + kadaluwarsa);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.tambahPromo(
                namaPromo,
                namaPenginput,
                referensiProyek,
                imageBase64,
                kadaluwarsa
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "✅ Response Code: " + response.code());
                resetButtonState();
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse basicResponse = response.body();
                        if (basicResponse.isSuccess()) {
                            Log.d(TAG, "Promo berhasil disimpan: " + namaPromo);

                            // ✅ Tampilkan local notification saja
                            showLocalSuccessNotification(namaPromo, namaPenginput);

                            // ✅ FIX: TUNGGU SEBENTAR KEMUDIAN AMBIL DATA PROMO TERBARU
                            new Handler().postDelayed(() -> {
                                loadLatestPromoAndSaveHistori(namaPromo, namaPenginput);
                            }, 1000);

                        } else {
                            Log.e(TAG, "❌ Gagal simpan promo: " + basicResponse.getMessage());
                            resetButtonState();
                            Toast.makeText(InputPromoActivity.this,
                                    "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "❌ Error response: " + response.code());
                        resetButtonState();
                        Toast.makeText(InputPromoActivity.this,
                                "Error response server", Toast.LENGTH_SHORT).show();
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
                Log.e(TAG, "❌ Network failure: " + t.getMessage());
                resetButtonState();
                Toast.makeText(InputPromoActivity.this,
                        "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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


    // ✅ PERBAIKAN: Method untuk ambil promo terbaru dan simpan histori
    private void loadLatestPromoAndSaveHistori(String promoName, String username) {
        Log.d(TAG, "🔄 Loading latest promo data for: " + promoName);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && !promoResponse.getData().isEmpty()) {
                        // Cari promo yang baru dibuat berdasarkan nama dan penginput
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getNamaPromo().equals(promoName) &&
                                    promo.getNamaPenginput().equals(username)) {

                                Log.d(TAG, "✅ Found new promo - ID: " + promo.getIdPromo() +
                                        ", Image Length: " + (promo.getGambarBase64() != null ?
                                        promo.getGambarBase64().length() : 0));

                                // ✅ SIMPAN HISTORI DENGAN GAMBAR LENGKAP DARI SERVER
                                savePromoHistoriWithCompleteImage(
                                        promo.getIdPromo(),
                                        promoName,
                                        username,
                                        promo.getGambarBase64()
                                );
                                return;
                            }
                        }
                        // Fallback: jika tidak ditemukan, coba simpan tanpa gambar
                        Log.w(TAG, "⚠️ Promo not found, saving without image");
                        savePromoHistoriWithCompleteImage(-1, promoName, username, null);
                    }
                } else {
                    Log.e(TAG, "❌ Failed to load promo data");
                    finishWithError("Gagal memuat data promo");
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error loading promo data: " + t.getMessage());
                finishWithError("Error: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Simpan histori dengan gambar lengkap
    private void savePromoHistoriWithCompleteImage(int promoId, String title, String penginput, String completeImage) {
        Log.d(TAG, "💾 Saving histori with complete image - ID: " + promoId +
                ", Image Length: " + (completeImage != null ? completeImage.length() : 0));

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Validasi gambar sebelum dikirim
        String finalImageData = validateImageForHistori(completeImage);

        Call<BasicResponse> call = apiService.addPromoHistori(
                "add_promo_histori",
                promoId,
                title,
                penginput,
                "Ditambahkan",
                finalImageData
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Histori berhasil disimpan dengan gambar lengkap");
                        sendRefreshBroadcast();
                        finishWithSuccess();
                    } else {
                        Log.e(TAG, "❌ Gagal simpan histori: " + basicResponse.getMessage());
                        finishWithError("Histori gagal: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ Error response histori: " + response.code());
                    finishWithError("Error response histori");
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error histori: " + t.getMessage());
                finishWithError("Network error: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Validasi gambar untuk histori
    private String validateImageForHistori(String imageData) {
        if (imageData == null || imageData.trim().isEmpty()) {
            Log.w(TAG, "⚠️ No image data provided");
            return "";
        }

        String cleanData = imageData.trim();

        // Cek data terpotong
        if (cleanData.endsWith("..") || cleanData.endsWith("...")) {
            Log.e(TAG, "❌ Image data truncated");
            return "";
        }

        // Cek panjang minimum untuk gambar yang valid
        if (cleanData.length() < 100) {
            Log.w(TAG, "⚠️ Image data too short: " + cleanData.length());
            return "";
        }

        // Cek format base64
        if (!cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$")) {
            Log.e(TAG, "❌ Invalid base64 format");
            return "";
        }

        Log.d(TAG, "✅ Image validated - Length: " + cleanData.length());
        return cleanData;
    }

    // ✅ METHOD BARU: Finish dengan success
    private void finishWithSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(InputPromoActivity.this,
                    "✅ Promo berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // ✅ METHOD BARU: Finish dengan error
    private void finishWithError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(InputPromoActivity.this, message, Toast.LENGTH_LONG).show();
            finish();
        });
    }

    // ✅ PERBAIKAN: Method untuk simpan histori dengan gambar lengkap
    private void savePromoHistoriWithCompleteImage(int promoId, String title, String penginput) {
        Log.d(TAG, "🔄 Loading complete image for histori - Promo ID: " + promoId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        // Cari promo yang baru dibuat
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getIdPromo() == promoId) {
                                String completeImage = promo.getGambarBase64();
                                Log.d(TAG, "✅ Found complete image, length: " +
                                        (completeImage != null ? completeImage.length() : 0));

                                // Simpan histori dengan gambar lengkap
                                saveHistoriWithCompleteImage(promoId, title, penginput, completeImage);
                                return;
                            }
                        }
                        // Fallback: simpan tanpa gambar
                        saveHistoriWithCompleteImage(promoId, title, penginput, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error loading promo for histori: " + t.getMessage());
                saveHistoriWithCompleteImage(promoId, title, penginput, null);
            }
        });
    }

    // ✅ METHOD BARU: Simpan histori dengan gambar yang sudah dipastikan lengkap
    private void saveHistoriWithCompleteImage(int promoId, String title, String penginput, String imageData) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Validasi image data
        String finalImageData = validateAndPrepareImageData(imageData);

        Log.d(TAG, "📤 Saving histori with image - Length: " +
                (finalImageData != null ? finalImageData.length() : 0));

        Call<BasicResponse> call = apiService.addPromoHistori(
                "add_promo_histori",
                promoId,
                title,
                penginput,
                "Ditambahkan",
                finalImageData
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Histori dengan gambar berhasil disimpan");
                        // Refresh NewsActivity
                        sendRefreshBroadcast();
                    } else {
                        Log.e(TAG, "❌ Gagal simpan histori: " + basicResponse.getMessage());
                    }
                }
                finish();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error saving histori: " + t.getMessage());
                finish();
            }
        });
    }

    // ✅ METHOD BARU: Validasi dan persiapan data gambar
    private String validateAndPrepareImageData(String imageData) {
        if (imageData == null || imageData.trim().isEmpty()) {
            Log.w(TAG, "⚠️ No image data provided");
            return "";
        }

        String cleanData = imageData.trim();

        // Cek data terpotong
        if (cleanData.endsWith("..") || cleanData.endsWith("...")) {
            Log.e(TAG, "❌ Image data truncated, using empty");
            return "";
        }

        // Cek panjang minimum
        if (cleanData.length() < 100) {
            Log.w(TAG, "⚠️ Image data too short: " + cleanData.length());
            return "";
        }

        // Cek format base64
        if (!cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$")) {
            Log.e(TAG, "❌ Invalid base64 format");
            return "";
        }

        Log.d(TAG, "✅ Image data validated, length: " + cleanData.length());
        return cleanData;
    }

    // ✅ METHOD BARU: Kirim broadcast untuk refresh
    private void sendRefreshBroadcast() {
        Intent refreshIntent = new Intent("REFRESH_NEWS_DATA");
        sendBroadcast(refreshIntent);
        Log.d(TAG, "📢 Refresh broadcast sent to NewsActivity");
    }

    // ✅ METHOD BARU: SIMPAN HISTORI LANGSUNG TANPA LOAD ULANG
    private void savePromoHistoriDirectly(String promoName, String username, String imageBase64) {
        Log.d(TAG, "=== SAVE HISTORI WITH IMAGE DEBUG ===");
        Log.d(TAG, "📝 Promo Name: " + promoName);
        Log.d(TAG, "👤 Username: " + username);
        Log.d(TAG, "🖼 Image Length: " + (imageBase64 != null ? imageBase64.length() : 0));

        // Validasi image data sebelum kirim
        String finalImageData = validateAndCleanImageData(imageBase64);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<BasicResponse> call = apiService.addPromoHistori(
                "add_promo_histori",
                1, // temporary ID
                promoName,
                username,
                "Ditambahkan",
                finalImageData
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Histori dengan gambar berhasil disimpan");
                        // Refresh NewsActivity
                        refreshNewsActivity();
                    } else {
                        Log.e(TAG, "❌ Histori gagal: " + basicResponse.getMessage());
                    }
                }
                finish();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error: " + t.getMessage());
                finish();
            }
        });
    }

    // ✅ METHOD BARU: VALIDASI DAN CLEAN IMAGE DATA
    private String validateAndCleanImageData(String imageData) {
        if (imageData == null || imageData.trim().isEmpty()) {
            Log.w(TAG, "⚠️ Image data is null or empty");
            return "";
        }

        String cleanData = imageData.trim();

        // Cek jika data terpotong
        if (cleanData.endsWith("..") || cleanData.endsWith("...")) {
            Log.e(TAG, "❌ Image data truncated, using empty");
            return "";
        }

        // Cek panjang minimum
        if (cleanData.length() < 100) {
            Log.w(TAG, "⚠️ Image data too short: " + cleanData.length());
            return "";
        }

        Log.d(TAG, "✅ Image data validated, length: " + cleanData.length());
        return cleanData;
    }

    // ✅ METHOD BARU: REFRESH NEWS ACTIVITY
    private void refreshNewsActivity() {
        // Kirim broadcast untuk refresh NewsActivity
        Intent refreshIntent = new Intent("REFRESH_NEWS_DATA");
        sendBroadcast(refreshIntent);
    }

    // METHOD 2: Gunakan @Body request
    private void tryMethod2(String promoName, String username, String imageBase64, int promoId) {
        Log.d(TAG, "🔄 Trying Method 2: @Body Request");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "add_promo_histori");
        requestBody.put("promo_id", promoId);
        requestBody.put("title", promoName);
        requestBody.put("penginput", username);
        requestBody.put("status", "Ditambahkan");
        requestBody.put("image_data", imageBase64 != null ? imageBase64 : "");

        Call<BasicResponse> call = apiService.addPromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "=== HISTORI RESPONSE METHOD 2 ===");
                Log.d(TAG, "📡 Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    Log.d(TAG, "✅ Success: " + basicResponse.isSuccess());
                    Log.d(TAG, "✅ Message: " + basicResponse.getMessage());

                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "🎉 HISTORI BERHASIL DISIMPAN (Method 2)!");
                    } else {
                        Log.e(TAG, "❌ Gagal simpan histori method 2: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ Error response histori method 2: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error method 2: " + t.getMessage());
            }
        });
    }
    private void loadLatestPromoAndSaveHistori(String promoName, String username, String imageBase64) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && !promoResponse.getData().isEmpty()) {
                        // Cari promo yang baru dibuat (berdasarkan nama)
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getNamaPromo().equals(promoName)) {
                                // ✅ FIX: PANGGIL METHOD DENGAN PARAMETER YANG BENAR
                                savePromoToHistori(
                                        promo.getIdPromo(), // ✅ promoId
                                        promoName,          // ✅ title
                                        username,           // ✅ penginput
                                        "Ditambahkan",      // ✅ status
                                        imageBase64         // ✅ imageData
                                );
                                Log.d(TAG, "✅ Histori saved for promo ID: " + promo.getIdPromo());
                                break;
                            }
                        }
                        finish();
                    } else {
                        Log.e(TAG, "No promo data found");
                        finish();
                    }
                } else {
                    Log.e(TAG, "Failed to load promo data");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e(TAG, "Error loading promo data: " + t.getMessage());
                finish(); // Tetap tutup activity meski gagal load
            }
        });
    }
    private void savePromoToHistori(int promoId, String title, String penginput, String status, String imageData) {
        Log.d(TAG, "=== SAVE PROMO TO HISTORI DEBUG ===");
        Log.d(TAG, "📊 Promo ID: " + promoId);
        Log.d(TAG, "📊 Title: " + title);
        Log.d(TAG, "📊 Penginput: " + penginput);
        Log.d(TAG, "📊 Image Data Length: " + (imageData != null ? imageData.length() : 0));

        if (imageData != null) {
            Log.d(TAG, "📊 First 100 chars: " + imageData.substring(0, Math.min(100, imageData.length())));
            Log.d(TAG, "📊 Last 50 chars: " + imageData.substring(Math.max(0, imageData.length() - 50)));

            // Cek jika data terpotong
            if (imageData.length() < 1000) {
                Log.w(TAG, "⚠️ WARNING: Image data seems too short for a real image");
            }
            if (imageData.endsWith("..") || imageData.endsWith("...")) {
                Log.e(TAG, "❌ ERROR: Image data is TRUNCATED!");
            }
        }

        // ✅ FIX: GUNAKAN RETROFIT DENGAN @Body UNTUK HINDARI PEMOTONGAN
        saveHistoriWithBodyRequest(promoId, title, penginput, status, imageData);
    }

    // ✅ METHOD BARU: GUNAKAN @Body REQUEST UNTUK HINDARI PEMOTONGAN DATA
    private void saveHistoriWithBodyRequest(int promoId, String title, String penginput, String status, String imageData) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Buat request object
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "add_promo_histori");
        requestBody.put("promo_id", promoId);
        requestBody.put("title", title);
        requestBody.put("penginput", penginput);
        requestBody.put("status", status);
        requestBody.put("image_data", imageData);

        Log.d(TAG, "📤 Sending histori request with body size: " +
                (imageData != null ? imageData.length() : 0) + " chars");

        Call<BasicResponse> call = apiService.addPromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Histori saved successfully");

                        // Debug response tambahan
                        if (basicResponse.getMessage().contains("image_stored")) {
                            Log.d(TAG, "📊 Server response: " + basicResponse.getMessage());
                        }
                    } else {
                        Log.e(TAG, "❌ Failed to save histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ Server error: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "❌ Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error reading error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: AMBIL GAMBAR LENGKAP DARI PROMO
    private void loadCompleteImageFromPromo(int promoId, String title, String penginput, String status) {
        Log.d(TAG, "🔄 Loading complete image from promo ID: " + promoId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && promoResponse.getData() != null) {
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getIdPromo() == promoId) {
                                String completeImage = promo.getGambarBase64();
                                if (completeImage != null && completeImage.length() > 500) {
                                    Log.d(TAG, "✅ Found complete image, length: " + completeImage.length());
                                    callHistoriApi(promoId, title, penginput, status, completeImage);
                                } else {
                                    Log.w(TAG, "❌ Promo image also too short: " +
                                            (completeImage != null ? completeImage.length() : 0));
                                    callHistoriApi(promoId, title, penginput, status, null);
                                }
                                return;
                            }
                        }
                        Log.w(TAG, "❌ Promo not found with ID: " + promoId);
                        callHistoriApi(promoId, title, penginput, status, null);
                    }
                } else {
                    Log.e(TAG, "❌ Failed to load promo data");
                    callHistoriApi(promoId, title, penginput, status, null);
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error loading promo: " + t.getMessage());
                callHistoriApi(promoId, title, penginput, status, null);
            }
        });
    }

    // ✅ METHOD BARU: PANGGIL API HISTORI
    private void callHistoriApi(int promoId, String title, String penginput, String status, String imageData) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.addPromoHistori(
                "add_promo_histori",
                promoId,
                title,
                penginput,
                status,
                imageData
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Histori saved successfully for promo ID: " + promoId);
                    } else {
                        Log.e(TAG, "❌ Failed to save histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error saving histori: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: SIMPAN DENGAN GAMBAR YANG SUDAH DIVALIDASI
    private void savePromoToHistoriWithImage(int promoId, String title, String penginput, String status, String imageData) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.addPromoHistori(
                "add_promo_histori",
                promoId,
                title,
                penginput,
                status,
                imageData
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Histori dengan gambar lengkap berhasil disimpan");
                } else {
                    Log.e(TAG, "❌ Gagal menyimpan histori dengan gambar lengkap");
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error menyimpan histori dengan gambar: " + t.getMessage());
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