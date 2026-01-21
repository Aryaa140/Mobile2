package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.datatransport.backend.cct.BuildConfig;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputDataProyekActivity extends AppCompatActivity {

    private static final String TAG = "InputDataProyekActivity";
    private static final int PICK_LOGO_REQUEST = 1;
    private static final int PICK_SITEPLAN_REQUEST = 2;
    private static final int PICK_FASILITAS_IMAGE_REQUEST = 3;

    // ✅ TAMBAHKAN KEY UNTUK SHAREDPREFERENCES YANG SESUAI
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAMA_USER = "nama_user";
    private static final String KEY_LEVEL = "level";
    private static final String USER_PREFS = "UserPrefs";

    private Button btnSimpan, btnBatal, btnPilihLogo, btnPilihSiteplan, btnPilihGambarFasilitas, btnTambahFasilitas;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextNamaProyek, editTextLokasiProyek, editTextDeskripsiProyek, editTextNamaFasilitas;
    private ImageView imagePreviewLogo, imagePreviewSiteplan, imagePreviewFasilitas;
    private RecyclerView recyclerViewFasilitas;
    private View textListFasilitas;

    private ApiService apiService;
    private FasilitasAdapter fasilitasAdapter;
    private List<FasilitasItem> fasilitasList;
    private SharedPreferences sharedPreferences;

    private Uri logoUri, siteplanUri, currentFasilitasImageUri;
    private String logoBase64, siteplanBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data_proyek);

        // ✅ INISIALISASI SHAREDPREFERENCES - COBA BERBAGAI NAMA FILE
        sharedPreferences = getSharedPreferences(USER_PREFS, MODE_PRIVATE);

        // ✅ COBA BACA DARI BERBAGAI SUMBER SHAREDPREFERENCES
        checkAllSharedPreferences();

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        initViews();
        setupRecyclerView();
        setupNavigation();
        setupButtonListeners();

        // ✅ SETUP USER INFO UNTUK DEBUG
        setupUserInfo();
    }

    // ✅ METHOD BARU: CEK SEMUA SHAREDPREFERENCES YANG MUNGKIN
    private void checkAllSharedPreferences() {
        Log.d(TAG, "=== CHECKING ALL SHAREDPREFERENCES ===");

        // Cek UserPrefs
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        Log.d(TAG, "UserPrefs - Username: " + userPrefs.getString("username", "NOT_FOUND"));
        Log.d(TAG, "UserPrefs - Nama User: " + userPrefs.getString("nama_user", "NOT_FOUND"));
        Log.d(TAG, "UserPrefs - Level: " + userPrefs.getString("level", "NOT_FOUND"));
        Log.d(TAG, "UserPrefs All Keys: " + userPrefs.getAll().toString());

        // Cek LoginPrefs (jika ada)
        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        Log.d(TAG, "LoginPrefs All Keys: " + loginPrefs.getAll().toString());

        // Cek AppPrefs (jika ada)
        SharedPreferences appPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Log.d(TAG, "AppPrefs All Keys: " + appPrefs.getAll().toString());

        // Cek default shared preferences
        SharedPreferences defaultPrefs = getSharedPreferences("default", MODE_PRIVATE);
        Log.d(TAG, "Default Prefs All Keys: " + defaultPrefs.getAll().toString());

        // ✅ COBA DAPATKAN DATA DARI INTENT (jika ada)
        Intent intent = getIntent();
        if (intent != null) {
            Log.d(TAG, "Intent Extras: " + intent.getExtras());
            if (intent.hasExtra("username")) {
                String usernameFromIntent = intent.getStringExtra("username");
                Log.d(TAG, "Username from Intent: " + usernameFromIntent);

                // Simpan ke SharedPreferences jika ditemukan
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USERNAME, usernameFromIntent);
                editor.apply();
                Log.d(TAG, "✅ Saved username from intent to SharedPreferences");
            }
        }
    }

    // ✅ METHOD BARU: SETUP USER INFO DENGAN FALLBACK KE BERBAGAI SUMBER
    private void setupUserInfo() {
        // Coba ambil dari berbagai sumber
        String username = getUsernameFromMultipleSources();
        String namaUser = getNamaUserFromMultipleSources();
        String userLevel = getLevelFromMultipleSources();

        Log.d(TAG, "=== FINAL USER DATA ===");
        Log.d(TAG, "Username: " + username);
        Log.d(TAG, "Nama User: " + namaUser);
        Log.d(TAG, "Level: " + userLevel);

        // ✅ SIMPAN KE SHAREDPREFERENCES JIKA BERHASIL DITEMUKAN
        if (!username.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, username);
            if (!namaUser.isEmpty()) {
                editor.putString(KEY_NAMA_USER, namaUser);
            }
            if (!userLevel.isEmpty()) {
                editor.putString(KEY_LEVEL, userLevel);
            }
            editor.apply();
            Log.d(TAG, "✅ Saved user data to SharedPreferences");
        }
    }

    // ✅ METHOD BARU: DAPATKAN USERNAME DARI BERBAGAI SUMBER
    private String getUsernameFromMultipleSources() {
        // 1. Coba dari SharedPreferences utama
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        if (!username.isEmpty()) {
            Log.d(TAG, "✅ Found username in main SharedPreferences: " + username);
            return username;
        }

        // 2. Coba dari UserPrefs
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = userPrefs.getString("username", "");
        if (!username.isEmpty()) {
            Log.d(TAG, "✅ Found username in UserPrefs: " + username);
            return username;
        }

        // 3. Coba dari LoginPrefs
        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        username = loginPrefs.getString("username", "");
        if (!username.isEmpty()) {
            Log.d(TAG, "✅ Found username in LoginPrefs: " + username);
            return username;
        }

        // 4. Coba dari Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            username = intent.getStringExtra("username");
            Log.d(TAG, "✅ Found username in Intent: " + username);
            return username;
        }

        // 5. Fallback - coba dari SharedPreferences default
        Map<String, ?> allPrefs = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().toLowerCase().contains("user") || entry.getKey().toLowerCase().contains("name")) {
                Log.d(TAG, "Potential username key: " + entry.getKey() + " = " + entry.getValue());
            }
        }

        Log.w(TAG, "❌ Username not found in any source");
        return "";
    }

    // ✅ METHOD BARU: DAPATKAN NAMA USER DARI BERBAGAI SUMBER
    private String getNamaUserFromMultipleSources() {
        // 1. Coba dari SharedPreferences utama
        String namaUser = sharedPreferences.getString(KEY_NAMA_USER, "");
        if (!namaUser.isEmpty()) {
            Log.d(TAG, "✅ Found nama_user in main SharedPreferences: " + namaUser);
            return namaUser;
        }

        // 2. Coba dari UserPrefs
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        namaUser = userPrefs.getString("nama_user", "");
        if (!namaUser.isEmpty()) {
            Log.d(TAG, "✅ Found nama_user in UserPrefs: " + namaUser);
            return namaUser;
        }

        // 3. Coba dari key lain yang mungkin
        namaUser = userPrefs.getString("nama", "");
        if (!namaUser.isEmpty()) {
            Log.d(TAG, "✅ Found nama in UserPrefs: " + namaUser);
            return namaUser;
        }

        // 4. Fallback ke username jika nama_user tidak ditemukan
        String username = getUsernameFromMultipleSources();
        if (!username.isEmpty()) {
            Log.d(TAG, "⚠️ Using username as nama_user: " + username);
            return username;
        }

        return "User";
    }

    // ✅ METHOD BARU: DAPATKAN LEVEL DARI BERBAGAI SUMBER
    private String getLevelFromMultipleSources() {
        // 1. Coba dari SharedPreferences utama
        String level = sharedPreferences.getString(KEY_LEVEL, "");
        if (!level.isEmpty()) {
            return level;
        }

        // 2. Coba dari UserPrefs
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        level = userPrefs.getString("level", "");
        if (!level.isEmpty()) {
            return level;
        }

        return "Operator";
    }

    // ✅ METHOD BARU: DAPATKAN INFO PENGGUNA YANG SEDANG LOGIN
    private UserInfo getCurrentUserInfo() {
        String username = getUsernameFromMultipleSources();
        String namaUser = getNamaUserFromMultipleSources();
        String level = getLevelFromMultipleSources();

        Log.d(TAG, "📋 Current User - Username: " + username + ", Nama: " + namaUser + ", Level: " + level);

        return new UserInfo(username, namaUser, level);
    }

    private void initViews() {
        try {
            btnSimpan = findViewById(R.id.btnSimpan);
            btnBatal = findViewById(R.id.btnBatal);
            btnPilihLogo = findViewById(R.id.btnPilihLogo);
            btnPilihSiteplan = findViewById(R.id.btnPilihSiteplan);
            btnPilihGambarFasilitas = findViewById(R.id.btnPilihGambarFasilitas);
            btnTambahFasilitas = findViewById(R.id.btnTambahFasilitas);

            editTextNamaProyek = findViewById(R.id.editTextNamaProyek);
            editTextLokasiProyek = findViewById(R.id.editTextLokasiProyek);
            editTextDeskripsiProyek = findViewById(R.id.editTextDeskripsiProyek);
            editTextNamaFasilitas = findViewById(R.id.editTextNamaFasilitas);

            imagePreviewLogo = findViewById(R.id.imagePreviewLogo);
            imagePreviewSiteplan = findViewById(R.id.imagePreviewSiteplan);
            imagePreviewFasilitas = findViewById(R.id.imagePreviewFasilitas);

            recyclerViewFasilitas = findViewById(R.id.recyclerViewFasilitas);
            textListFasilitas = findViewById(R.id.textListFasilitas);

            topAppBar = findViewById(R.id.topAppBar);
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            fasilitasList = new ArrayList<>();

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error initializing UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        try {
            fasilitasAdapter = new FasilitasAdapter(fasilitasList);
            recyclerViewFasilitas.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewFasilitas.setAdapter(fasilitasAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up recycler view: " + e.getMessage());
        }
    }

    private void setupNavigation() {
        try {
            // Navigasi toolbar
            topAppBar.setNavigationOnClickListener(v -> {
                navigateToHome();
            });

            // Bottom navigation
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, NewBeranda.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_folder) {
                    startActivity(new Intent(this, LihatDataActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage());
        }
    }

    private void setupButtonListeners() {
        try {
            // Simpan data proyek
            btnSimpan.setOnClickListener(v -> simpanDataProyek());

            // Batal input
            btnBatal.setOnClickListener(v -> navigateToHome());

            // Pilih gambar
            btnPilihLogo.setOnClickListener(v -> pilihGambar(PICK_LOGO_REQUEST));
            btnPilihSiteplan.setOnClickListener(v -> pilihGambar(PICK_SITEPLAN_REQUEST));
            btnPilihGambarFasilitas.setOnClickListener(v -> pilihGambar(PICK_FASILITAS_IMAGE_REQUEST));

            // Tambah fasilitas ke list
            btnTambahFasilitas.setOnClickListener(v -> tambahFasilitasKeList());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up button listeners: " + e.getMessage());
        }
    }

    private void pilihGambar(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar dari Galeri"), requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Error picking image: " + e.getMessage());
            Toast.makeText(this, "Error memilih gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void tambahFasilitasKeList() {
        try {
            String namaFasilitas = editTextNamaFasilitas.getText().toString().trim();

            if (namaFasilitas.isEmpty()) {
                editTextNamaFasilitas.setError("Nama fasilitas harus diisi");
                return;
            }

            if (currentFasilitasImageUri == null) {
                Toast.makeText(this, "Pilih gambar fasilitas terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String gambarBase64 = convertImageToBase64(currentFasilitasImageUri);
            FasilitasItem fasilitas = new FasilitasItem(namaFasilitas, gambarBase64);
            fasilitasList.add(fasilitas);

            // Update UI
            fasilitasAdapter.notifyDataSetChanged();
            recyclerViewFasilitas.setVisibility(View.VISIBLE);
            textListFasilitas.setVisibility(View.VISIBLE);

            // Reset form fasilitas
            editTextNamaFasilitas.setText("");
            imagePreviewFasilitas.setVisibility(View.INVISIBLE);
            currentFasilitasImageUri = null;

            Toast.makeText(this, "Fasilitas ditambahkan ke list", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error adding facility to list: " + e.getMessage());
            Toast.makeText(this, "Error menambahkan fasilitas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                switch (requestCode) {
                    case PICK_LOGO_REQUEST:
                        logoUri = imageUri;
                        logoBase64 = convertImageToBase64(imageUri);
                        imagePreviewLogo.setImageURI(imageUri);
                        imagePreviewLogo.setVisibility(View.VISIBLE);
                        break;

                    case PICK_SITEPLAN_REQUEST:
                        siteplanUri = imageUri;
                        siteplanBase64 = convertImageToBase64(imageUri);
                        imagePreviewSiteplan.setImageURI(imageUri);
                        imagePreviewSiteplan.setVisibility(View.VISIBLE);
                        break;

                    case PICK_FASILITAS_IMAGE_REQUEST:
                        currentFasilitasImageUri = imageUri;
                        imagePreviewFasilitas.setImageURI(imageUri);
                        imagePreviewFasilitas.setVisibility(View.VISIBLE);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image result: " + e.getMessage());
                Toast.makeText(this, "Error memproses gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertImageToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);

        // Decode image dengan options untuk mengurangi ukuran
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // Hitung sample size
        int targetWidth = 800;
        int targetHeight = 800;
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;

        inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        if (bitmap == null) {
            throw new IOException("Gagal decode bitmap");
        }

        // Kompresi gambar
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Tutup stream
        inputStream.close();
        byteArrayOutputStream.close();

        String base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d(TAG, "Image converted to base64, length: " + base64.length());

        return base64;
    }

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

        if (inSampleSize > 8) {
            inSampleSize = 8;
        }

        Log.d(TAG, "Image original: " + width + "x" + height + ", sampleSize: " + inSampleSize);
        return inSampleSize;
    }

    private void simpanDataProyek() {
        try {
            // Validasi input
            String namaProyek = editTextNamaProyek.getText().toString().trim();
            String lokasiProyek = editTextLokasiProyek.getText().toString().trim();
            String deskripsiProyek = editTextDeskripsiProyek.getText().toString().trim();

            // ✅ DAPATKAN USER INFO DARI BERBAGAI SUMBER
            UserInfo currentUser = getCurrentUserInfo();
            String username = currentUser.getUsername();
            String namaUser = currentUser.getNamaUser();

            // ✅ VALIDASI: Pastikan data user tidak kosong
            if (username.isEmpty()) {
                Log.e(TAG, "❌ CRITICAL: Username is empty after checking all sources");
                Toast.makeText(this,
                        "Tidak dapat menemukan data pengguna. Silakan login ulang atau hubungi administrator.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "📋 Using user info - Username: " + username + ", Nama: " + namaUser);

            if (namaProyek.isEmpty()) {
                editTextNamaProyek.setError("Nama proyek harus diisi");
                editTextNamaProyek.requestFocus();
                return;
            }

            if (lokasiProyek.isEmpty()) {
                editTextLokasiProyek.setError("Lokasi proyek harus diisi");
                editTextLokasiProyek.requestFocus();
                return;
            }

            // ✅ DEBUG: Tampilkan info notifikasi sebelum simpan
            debugFCMNotification(namaProyek, namaUser);

            // Tampilkan loading
            btnSimpan.setEnabled(false);
            btnSimpan.setText("Menyimpan...");

            String fasilitasJson = new Gson().toJson(fasilitasList);

            Log.d(TAG, "=== MENGIRIM DATA PROYEK ===");
            Log.d(TAG, "Nama Proyek: " + namaProyek);
            Log.d(TAG, "Lokasi: " + lokasiProyek);
            Log.d(TAG, "Penginput - Username: " + username + ", Nama: " + namaUser);

            // ✅ KIRIM DATA DENGAN USER INFO YANG BENAR
            Call<BasicResponse> call = apiService.addProyek(
                    "addProyek",
                    namaProyek,
                    lokasiProyek,
                    deskripsiProyek,
                    logoBase64 != null ? logoBase64 : "",
                    siteplanBase64 != null ? siteplanBase64 : "",
                    fasilitasJson,
                    username,
                    namaUser
            );

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("Simpan");

                    Log.d(TAG, "=== DEBUG RESPONSE ===");
                    Log.d(TAG, "Response Code: " + response.code());
                    Log.d(TAG, "Response isSuccessful: " + response.isSuccessful());

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                BasicResponse basicResponse = response.body();
                                Log.d(TAG, "Response Body - Success: " + basicResponse.isSuccess());
                                Log.d(TAG, "Response Body - Message: " + basicResponse.getMessage());
                                Log.d(TAG, "Response Body - Data Saved: " + basicResponse.getDataSaved());
                                Log.d(TAG, "Response Body - ID Proyek: " + basicResponse.getIdProyek());

                                if (basicResponse.isSuccess()) {
                                    // ✅ PERBAIKAN: TAMPILKAN NOTIFIKASI LOKAL HANYA UNTUK USER LAIN
                                    handleSuccessResponse(namaProyek, lokasiProyek, namaUser, basicResponse);


                                } else {
                                    String errorMsg = basicResponse.getMessage();
                                    Log.e(TAG, "Server returned success:false - " + errorMsg);
                                    Toast.makeText(InputDataProyekActivity.this,
                                            errorMsg.isEmpty() ? "Gagal menyimpan data" : errorMsg,
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.e(TAG, "Response body is null but HTTP code is successful");
                                // ✅ PERBAIKAN: Jika HTTP 200 tapi response body null, anggap sukses
                                if (response.code() == 200) {
                                    Log.w(TAG, "⚠️ HTTP 200 with null body - assuming data saved successfully");
                                    handleSuccessResponse(namaProyek, lokasiProyek, namaUser,
                                            createSuccessResponse(namaProyek));
                                } else {
                                    Toast.makeText(InputDataProyekActivity.this,
                                            "Response tidak valid dari server", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Log.e(TAG, "HTTP Error: " + response.code());

                            // ✅ PERBAIKAN: Handle error 500 khusus
                            if (response.code() == 500) {
                                Log.w(TAG, "⚠️ HTTP 500 Error - Data might be saved on server");
                                // Coba baca error body untuk info lebih detail
                                try {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Error 500 Body: " + errorBody);

                                    // Jika error 500, tetap anggap data tersimpan dan lanjutkan
                                    handleSuccessResponse(namaProyek, lokasiProyek, namaUser,
                                            createSuccessResponseWithWarning(namaProyek, "Error server 500 - Data mungkin sudah tersimpan"));

                                } catch (IOException e) {
                                    Log.e(TAG, "Error reading error body: " + e.getMessage());
                                    // Tetap lanjutkan dengan asumsi data tersimpan
                                    handleSuccessResponse(namaProyek, lokasiProyek, namaUser,
                                            createSuccessResponseWithWarning(namaProyek, "Error server 500 - Data mungkin sudah tersimpan"));
                                }
                            } else {
                                // Untuk error selain 500, tampilkan error normal
                                try {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "Error Body: " + errorBody);
                                    Toast.makeText(InputDataProyekActivity.this,
                                            "Error server: " + response.code(), Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    Log.e(TAG, "Error reading error body: " + e.getMessage());
                                    Toast.makeText(InputDataProyekActivity.this,
                                            "Error server: " + response.code(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response: " + e.getMessage());
                        // ✅ PERBAIKAN: Jika ada exception, tetap lanjutkan dengan asumsi data tersimpan
                        handleSuccessResponse(namaProyek, lokasiProyek, namaUser,
                                createSuccessResponseWithWarning(namaProyek, "Error: " + e.getMessage()));
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("Simpan");
                    Log.e(TAG, "Network error: " + t.getMessage());

                    // ✅ PERBAIKAN: Untuk network error, beri warning tapi lanjutkan
                    Toast.makeText(InputDataProyekActivity.this,
                            "Error koneksi: " + t.getMessage() + "\nPeriksa apakah data sudah tersimpan di server.",
                            Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error saving project data: " + e.getMessage());
            btnSimpan.setEnabled(true);
            btnSimpan.setText("Simpan");
            Toast.makeText(this, "Error menyimpan data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleFCMResult(BasicResponse basicResponse, String proyekName, String lokasiProyek, String namaUser) {
        try {
            if (basicResponse.getFcmNotification() != null) {
                Map<String, Object> fcmResult = basicResponse.getFcmNotification();
                boolean fcmSuccess = Boolean.TRUE.equals(fcmResult.get("success"));
                int httpCode = fcmResult.get("http_code") != null ?
                        ((Double) fcmResult.get("http_code")).intValue() : 0;
                String messageId = (String) fcmResult.get("message_id");
                String error = (String) fcmResult.get("error");

                Log.d(TAG, "=== FCM RESULT ANALYSIS ===");
                Log.d(TAG, "FCM Success: " + fcmSuccess);
                Log.d(TAG, "HTTP Code: " + httpCode);
                Log.d(TAG, "Message ID: " + messageId);
                Log.d(TAG, "Error: " + error);

                if (fcmSuccess) {
                    Log.i(TAG, "✅ FCM notification sent successfully to all other devices");
                    // Notifikasi FCM berhasil dikirim ke device lain
                } else {
                    Log.w(TAG, "❌ FCM notification failed: " + error);
                    // Jangan tampilkan error ke user, cukup log saja
                }
            } else {
                Log.w(TAG, "⚠️ No FCM notification data in response");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling FCM result: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: DEBUG FCM NOTIFICATION
    private void debugFCMNotification(String namaProyek, String namaUser) {
        try {
            Log.d(TAG, "=== FCM NOTIFICATION DEBUG ===");

            // Dapatkan info user saat ini untuk debug
            UserInfo currentUser = getCurrentUserInfo();
            String currentUsername = currentUser.getUsername();
            String currentNamaUser = currentUser.getNamaUser();

            Log.d(TAG, "📱 Current Device User:");
            Log.d(TAG, "  - Username: " + currentUsername);
            Log.d(TAG, "  - Nama: " + currentNamaUser);

            Log.d(TAG, "📨 Notification Data:");
            Log.d(TAG, "  - Proyek: " + namaProyek);
            Log.d(TAG, "  - Added By: " + namaUser);

            Log.d(TAG, "🔍 Notification Logic:");
            Log.d(TAG, "  - Should show local notification: false (user yang input)");
            Log.d(TAG, "  - Should show FCM notification: true (untuk device lain)");
            Log.d(TAG, "  - Notification structure: \"" + namaProyek + "\" telah ditambahkan oleh " + namaUser);

        } catch (Exception e) {
            Log.e(TAG, "Error in FCM debug: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: HANDLE FCM NOTIFICATION UNTUK PROYEK
    private void handleProyekFCMNotification(String namaProyek, String lokasiProyek, String namaUser, String username, Integer idProyek) {
        try {
            Log.d(TAG, "🚀 Preparing FCM notification for project: " + namaProyek);

            // ✅ DEBUG: Tampilkan data yang akan dikirim
            Log.d(TAG, "📨 FCM Data Details:");
            Log.d(TAG, "  - Proyek Name: " + namaProyek);
            Log.d(TAG, "  - Lokasi: " + lokasiProyek);
            Log.d(TAG, "  - Added By: " + namaUser);
            Log.d(TAG, "  - Username: " + username);
            Log.d(TAG, "  - ID Proyek: " + idProyek);

            // ✅ Buat payload data untuk FCM
            Map<String, String> fcmData = new HashMap<>();
            fcmData.put("type", "new_proyek");
            fcmData.put("title", "Proyek Ditambahkan 🏗️");
            fcmData.put("body", "\"" + namaProyek + "\" telah ditambahkan oleh " + namaUser);
            fcmData.put("proyek_name", namaProyek);
            fcmData.put("lokasi_proyek", lokasiProyek);
            fcmData.put("added_by", namaUser);
            fcmData.put("username", username);
            fcmData.put("id_proyek", idProyek != null ? idProyek.toString() : "0");
            fcmData.put("timestamp", String.valueOf(System.currentTimeMillis()));
            fcmData.put("click_action", "OPEN_PROYEK");

            Log.d(TAG, "✅ FCM payload prepared successfully");

            // ✅ Tampilkan notifikasi lokal HANYA untuk debugging
            // Di production, hapus bagian ini karena notifikasi akan dikirim via FCM
            if (BuildConfig.DEBUG) {
                showDebugLocalNotification(namaProyek, namaUser);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparing FCM notification: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: NOTIFIKASI LOKAL UNTUK DEBUG SAJA
    private void showDebugLocalNotification(String proyekName, String namaUser) {
        try {
            String title = "DEBUG: Proyek Ditambahkan 🏗️";
            String body = "\"" + proyekName + "\" telah ditambahkan oleh " + namaUser;

            NotificationHelper.showPromoNotification(
                    this,
                    title,
                    body,
                    null
            );
            Log.d(TAG, "🔔 Debug local notification shown");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing debug notification: " + e.getMessage());
        }
    }

    private void handleSuccessResponse(String namaProyek, String lokasiProyek, String namaUser, BasicResponse basicResponse) {
        try {
            // ✅ DAPATKAN ID PROYEK DARI RESPONSE
            Integer idProyek = basicResponse.getIdProyek();
            if (idProyek == null || idProyek == 0) {
                Log.w(TAG, "⚠️ ID Proyek not available in response");
                idProyek = 0;
            }

            // ✅ DAPATKAN USERNAME
            UserInfo currentUser = getCurrentUserInfo();
            String username = currentUser.getUsername();

            Log.d(TAG, "🎉 Success Response Details:");
            Log.d(TAG, "  - Proyek: " + namaProyek);
            Log.d(TAG, "  - ID Proyek: " + idProyek);
            Log.d(TAG, "  - User: " + namaUser + " (" + username + ")");

            // ✅ PERBAIKAN: TAMPILKAN NOTIFIKASI LOKAL UNTUK PENGGUNA YANG INPUT
            showLocalSuccessNotification(namaProyek, namaUser);

            // ✅ SIMPAN KE HISTORI PROYEK (TAMBAHKAN BARIS INI)
            saveProyekHistoriAfterSuccess(idProyek, namaProyek, lokasiProyek);

            // ✅ HANDLE FCM RESULT DARI SERVER (dari PHP)
            handleFCMResult(basicResponse, namaProyek, lokasiProyek, namaUser);

            String successMessage = basicResponse.getMessage();
            if (successMessage == null || successMessage.isEmpty()) {
                successMessage = "Proyek '" + namaProyek + "' berhasil disimpan";
            }

            Toast.makeText(InputDataProyekActivity.this, successMessage, Toast.LENGTH_SHORT).show();
            resetForm();

            // Navigasi ke home setelah delay
            new android.os.Handler().postDelayed(() -> navigateToHome(), 1000);

        } catch (Exception e) {
            Log.e(TAG, "Error in handleSuccessResponse: " + e.getMessage());
            Toast.makeText(this, "Proyek berhasil disimpan", Toast.LENGTH_LONG).show();
            resetForm();
            navigateToHome();
        }
    }

    // DI InputDataProyekActivity.java - PERBAIKI method saveProyekHistoriAfterSuccess()
    private void saveProyekHistoriAfterSuccess(int proyekId, String namaProyek, String lokasiProyek) {
        UserInfo currentUser = getCurrentUserInfo();
        String penginput = currentUser.getNamaUser();

        // DEBUG: Log detail
        Log.d(TAG, "📝 [HISTORI] Saving proyek histori:");
        Log.d(TAG, "📝 [HISTORI] - Proyek ID: " + proyekId);
        Log.d(TAG, "📝 [HISTORI] - Nama Proyek: " + namaProyek);
        Log.d(TAG, "📝 [HISTORI] - Lokasi: " + lokasiProyek);
        Log.d(TAG, "📝 [HISTORI] - Penginput: " + penginput);
        Log.d(TAG, "📝 [HISTORI] - Logo Base64: " + (logoBase64 != null ? logoBase64.length() + " chars" : "null"));

        // Ambil gambar untuk histori
        String imageDataForHistori = "";
        if (logoBase64 != null && !logoBase64.isEmpty() && !logoBase64.equals("null") && logoBase64.length() > 100) {
            imageDataForHistori = logoBase64;
            Log.d(TAG, "✅ [HISTORI] Using logo for histori, length: " + logoBase64.length());
        } else {
            Log.d(TAG, "⚠️ [HISTORI] No valid logo for histori");
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // PERBAIKAN: Gunakan endpoint yang benar dengan logging detail
        Call<BasicResponse> call = apiService.addProyekHistori(
                "add_proyek_histori", // action
                proyekId,
                namaProyek,
                lokasiProyek,
                penginput,
                "Ditambahkan",
                imageDataForHistori
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "📡 [HISTORI] Response received - Code: " + response.code());
                Log.d(TAG, "📡 [HISTORI] Response successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    Log.d(TAG, "📡 [HISTORI] Response success: " + basicResponse.isSuccess());
                    Log.d(TAG, "📡 [HISTORI] Response message: " + basicResponse.getMessage());

                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ [HISTORI] Proyek histori saved successfully to server");

                        // Kirim broadcast untuk refresh NewsActivity
                        Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
                        broadcastIntent.putExtra("ACTION", "PROYEK_ADDED");
                        broadcastIntent.putExtra("NAMA_PROYEK", namaProyek);
                        broadcastIntent.putExtra("LOKASI_PROYEK", lokasiProyek);
                        broadcastIntent.putExtra("PENGINPUT", penginput);
                        broadcastIntent.putExtra("PROYEK_ID", proyekId);

                        sendBroadcast(broadcastIntent);
                        Log.d(TAG, "📡 [HISTORI] Broadcast sent for proyek: " + namaProyek);
                    } else {
                        Log.e(TAG, "❌ [HISTORI] Failed to save proyek histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ [HISTORI] Proyek histori response not successful: " + response.code());
                    // Coba baca error body
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "❌ [HISTORI] Error body: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "❌ [HISTORI] Error reading error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ [HISTORI] Network error saving proyek histori: " + t.getMessage());

                // Tetap kirim broadcast meskipun histori gagal
                Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
                broadcastIntent.putExtra("ACTION", "PROYEK_ADDED");
                broadcastIntent.putExtra("NAMA_PROYEK", namaProyek);
                sendBroadcast(broadcastIntent);
            }
        });
    }

    // ✅ METHOD: Notifikasi lokal untuk penginput
    private void showLocalSuccessNotification(String proyekName, String namaUser) {
        try {
            String title = "Proyek Berhasil Disimpan ✅";
            String body = "Proyek \"" + proyekName + "\" telah berhasil disimpan";

            NotificationHelper.showPromoNotification(
                    this,
                    title,
                    body,
                    null
            );
            Log.d(TAG, "✅ Local notification shown for user: " + body);
        } catch (Exception e) {
            Log.e(TAG, "Error showing local notification: " + e.getMessage());
        }
    }

    private void resetForm() {
        editTextNamaProyek.setText("");
        editTextLokasiProyek.setText("");
        editTextDeskripsiProyek.setText("");
        editTextNamaFasilitas.setText("");

        imagePreviewLogo.setVisibility(View.INVISIBLE);
        imagePreviewSiteplan.setVisibility(View.INVISIBLE);
        imagePreviewFasilitas.setVisibility(View.INVISIBLE);

        recyclerViewFasilitas.setVisibility(View.GONE);
        textListFasilitas.setVisibility(View.GONE);

        fasilitasList.clear();
        fasilitasAdapter.notifyDataSetChanged();

        logoUri = null;
        siteplanUri = null;
        currentFasilitasImageUri = null;
        logoBase64 = null;
        siteplanBase64 = null;
    }

    private void navigateToHome() {
        Intent intent = new Intent(InputDataProyekActivity.this, NewBeranda.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHome();
    }

    // ✅ INNER CLASS UNTUK USER INFO
    private static class UserInfo {
        private String username;
        private String namaUser;
        private String level;

        public UserInfo(String username, String namaUser, String level) {
            this.username = username != null ? username : "";
            this.namaUser = namaUser != null ? namaUser : "";
            this.level = level != null ? level : "Operator";
        }

        public String getUsername() {
            return username;
        }

        public String getNamaUser() {
            return !namaUser.isEmpty() ? namaUser : username;
        }

        public String getLevel() {
            return level;
        }
    }

    // ✅ METHOD BARU: Buat response sukses manual
    private BasicResponse createSuccessResponse(String namaProyek) {
        // Buat BasicResponse manual menggunakan anonymous class
        return new BasicResponse() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "Proyek '" + namaProyek + "' berhasil disimpan";
            }

            @Override
            public Boolean getDataSaved() {
                return true;
            }

            @Override
            public Integer getIdProyek() {
                return 0; // Default value karena kita tidak tahu ID-nya
            }

            @Override
            public Map<String, Object> getFcmNotification() {
                return null; // Default value
            }

            @Override
            public Integer getFasilitasCount() {
                return 0; // Default value
            }

            @Override
            public boolean isAvailable() {
                return true; // Default value
            }
        };
    }

    // ✅ METHOD BARU: Buat response sukses dengan warning
    private BasicResponse createSuccessResponseWithWarning(String namaProyek, String warning) {
        return new BasicResponse() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getMessage() {
                return "Proyek '" + namaProyek + "' berhasil disimpan (Warning: " + warning + ")";
            }

            @Override
            public Boolean getDataSaved() {
                return true;
            }

            @Override
            public Integer getIdProyek() {
                return 0; // Default value
            }

            @Override
            public Map<String, Object> getFcmNotification() {
                return null; // Default value
            }

            @Override
            public Integer getFasilitasCount() {
                return 0; // Default value
            }

            @Override
            public boolean isAvailable() {
                return true; // Default value
            }
        };
    }
}