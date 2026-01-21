package com.example.mobile;

import android.content.Context;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputHunianActivity extends AppCompatActivity implements FasilitasHunianAdapter.OnFasilitasHapusListener {

    private static final String TAG = "InputHunianActivity";
    private static final int PICK_IMAGE_UNIT_REQUEST = 1;
    private static final int PICK_IMAGE_DENAH_REQUEST = 2;
    private Button btnSimpan, btnBatal, btnPilihGambarUnit, btnPilihGambarDenah, btnTambahFasilitas;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextNamaHunian, editTextLuasTanah, editTextLuasBangunan, editTextDeskripsiHunian;
    private EditText editTextNamaFasilitas, editTextJumlahFasilitas;
    private Spinner spinnerProyek;
    private ImageView imagePreviewUnit, imagePreviewDenah;
    private RecyclerView recyclerViewFasilitas;
    private TextView textListFasilitas;

    private int currentHunianId = 0;
    private ApiService apiService;
    private List<String> proyekList = new ArrayList<>();
    private android.widget.ArrayAdapter<String> proyekAdapter;
    private FasilitasHunianAdapter fasilitasAdapter;
    private List<FasilitasHunianItem> fasilitasHunianList = new ArrayList<>();

    private Uri gambarUnitUri, gambarDenahUri;
    private String gambarUnitBase64 = "", gambarDenahBase64 = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_hunian);

        Log.d(TAG, "Activity onCreate started");

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);
        Log.d(TAG, "API Service initialized");

        // Inisialisasi view
        initViews();
        setupNavigation();
        setupButtonListeners();
        setupRecyclerView();
        loadProyekData();
    }

    private void initViews() {
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        btnPilihGambarUnit = findViewById(R.id.btnPilihGambarUnit);
        btnPilihGambarDenah = findViewById(R.id.btnPilihGambarDenah);
        btnTambahFasilitas = findViewById(R.id.btnTambahFasilitas);

        editTextNamaHunian = findViewById(R.id.editTextNamaHunian);
        editTextLuasTanah = findViewById(R.id.editTextLuasTanah);
        editTextLuasBangunan = findViewById(R.id.editTextLuasBangunan);
        editTextDeskripsiHunian = findViewById(R.id.editTextDeskripsiHunian);
        editTextNamaFasilitas = findViewById(R.id.editTextNamaFasilitas);
        editTextJumlahFasilitas = findViewById(R.id.editTextJumlahFasilitas);

        spinnerProyek = findViewById(R.id.spinnerRoleProyek);
        imagePreviewUnit = findViewById(R.id.imagePreviewUnit);
        imagePreviewDenah = findViewById(R.id.imagePreviewDenah);
        recyclerViewFasilitas = findViewById(R.id.recyclerViewFasilitas);
        textListFasilitas = findViewById(R.id.textListFasilitas);

        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // PERBAIKAN: Setup spinner adapter dengan cara yang benar
        proyekAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, proyekList);
        proyekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyek.setAdapter(proyekAdapter);

        // Tambahkan item default sementara
        proyekList.add("Memuat data proyek...");
        proyekAdapter.notifyDataSetChanged();

        Log.d(TAG, "Views initialized successfully");
    }

    private void setupRecyclerView() {
        fasilitasAdapter = new FasilitasHunianAdapter(fasilitasHunianList, this);
        recyclerViewFasilitas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFasilitas.setAdapter(fasilitasAdapter);
    }

    private void setupNavigation() {
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
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupButtonListeners() {
        // Simpan data hunian
        btnSimpan.setOnClickListener(v -> simpanDataHunian());

        // Batal input
        btnBatal.setOnClickListener(v -> navigateToHome());

        // Pilih gambar unit
        btnPilihGambarUnit.setOnClickListener(v -> pilihGambar(PICK_IMAGE_UNIT_REQUEST));

        // Pilih gambar denah
        btnPilihGambarDenah.setOnClickListener(v -> pilihGambar(PICK_IMAGE_DENAH_REQUEST));

        // Tambah fasilitas ke list
        btnTambahFasilitas.setOnClickListener(v -> tambahFasilitasKeList());
    }

    private void tambahFasilitasKeList() {
        String namaFasilitas = editTextNamaFasilitas.getText().toString().trim();
        String jumlahStr = editTextJumlahFasilitas.getText().toString().trim();

        if (namaFasilitas.isEmpty()) {
            editTextNamaFasilitas.setError("Nama fasilitas harus diisi");
            return;
        }

        if (jumlahStr.isEmpty()) {
            editTextJumlahFasilitas.setError("Jumlah harus diisi");
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
        } catch (NumberFormatException e) {
            editTextJumlahFasilitas.setError("Jumlah harus berupa angka");
            return;
        }

        // PERBAIKAN: Gunakan constructor yang benar untuk FasilitasHunianItem
        FasilitasHunianItem fasilitas = new FasilitasHunianItem();
        fasilitas.setNamaFasilitas(namaFasilitas);
        fasilitas.setJumlah(jumlah);
        // idFasilitas dan namaHunian akan di-set oleh server nanti

        fasilitasHunianList.add(fasilitas);

        // Update UI
        fasilitasAdapter.notifyDataSetChanged();
        recyclerViewFasilitas.setVisibility(View.VISIBLE);
        textListFasilitas.setVisibility(View.VISIBLE);

        // Reset form fasilitas
        editTextNamaFasilitas.setText("");
        editTextJumlahFasilitas.setText("");

        Toast.makeText(this, "Fasilitas ditambahkan ke list", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHapusFasilitas(int position) {
        fasilitasHunianList.remove(position);
        fasilitasAdapter.notifyDataSetChanged();

        // Sembunyikan recyclerview jika list kosong
        if (fasilitasHunianList.isEmpty()) {
            recyclerViewFasilitas.setVisibility(View.GONE);
            textListFasilitas.setVisibility(View.GONE);
        }
    }

    private void pilihGambar(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Error picking image: " + e.getMessage());
            Toast.makeText(this, "Error memilih gambar", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                switch (requestCode) {
                    case PICK_IMAGE_UNIT_REQUEST:
                        gambarUnitUri = imageUri;
                        gambarUnitBase64 = convertImageToBase64(imageUri);
                        imagePreviewUnit.setImageURI(imageUri);
                        imagePreviewUnit.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Gambar unit dipilih", Toast.LENGTH_SHORT).show();
                        break;

                    case PICK_IMAGE_DENAH_REQUEST:
                        gambarDenahUri = imageUri;
                        gambarDenahBase64 = convertImageToBase64(imageUri);
                        imagePreviewDenah.setImageURI(imageUri);
                        imagePreviewDenah.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Gambar denah dipilih", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image result: " + e.getMessage());
                Toast.makeText(this, "Error memproses gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertImageToBase64(Uri imageUri) throws IOException {
        android.content.ContentResolver contentResolver = getContentResolver();
        java.io.InputStream inputStream = contentResolver.openInputStream(imageUri);

        // Decode image dengan options untuk mengurangi ukuran
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 800, 800);
        options.inJustDecodeBounds = false;

        inputStream = contentResolver.openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        if (bitmap == null) {
            throw new IOException("Gagal decode bitmap");
        }

        // Kompresi gambar untuk menghindari ukuran terlalu besar
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Tutup stream
        inputStream.close();
        byteArrayOutputStream.close();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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

    private void loadProyekData() {
        Log.d(TAG, "Loading data proyek from getProyek()...");

        Call<ProyekResponse> call = apiService.getProyek();
        call.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                Log.d(TAG, "Response received - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ProyekResponse proyekResponse = response.body();
                    Log.d(TAG, "Response success: " + proyekResponse.isSuccess());
                    Log.d(TAG, "Response message: " + proyekResponse.getMessage());

                    if (proyekResponse.isSuccess() && proyekResponse.getData() != null) {
                        proyekList.clear();
                        // PERBAIKAN: Tambahkan item default di awal
                        proyekList.add("Pilih Proyek");

                        for (Proyek proyek : proyekResponse.getData()) {
                            proyekList.add(proyek.getNamaProyek());
                        }
                        proyekAdapter.notifyDataSetChanged();

                        Log.d(TAG, "Proyek data loaded successfully: " + proyekList.size() + " items");

                        // Set selection ke item default
                        spinnerProyek.setSelection(0);

                    } else {
                        String errorMsg = "Gagal memuat data proyek: " + proyekResponse.getMessage();
                        Toast.makeText(InputHunianActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, errorMsg);

                        // Tetap tampilkan item default meski gagal
                        proyekList.clear();
                        proyekList.add("Pilih Proyek - Data gagal dimuat");
                        proyekAdapter.notifyDataSetChanged();
                    }
                } else {
                    String errorMsg = "Response tidak sukses - Code: " + response.code();
                    Toast.makeText(InputHunianActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMsg);

                    tryFallbackProyekLoad();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                String errorMsg = "Gagal memuat data proyek: " + t.getMessage();
                Toast.makeText(InputHunianActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Load proyek error: " + t.getMessage(), t);

                // Tetap tampilkan item default meski gagal
                proyekList.clear();
                proyekList.add("Pilih Proyek - Koneksi gagal");
                proyekAdapter.notifyDataSetChanged();

                tryFallbackProyekLoad();
            }
        });
    }


    // Fallback method jika getProyek() gagal
    private void tryFallbackProyekLoad() {
        Log.d(TAG, "Mencoba fallback method untuk load proyek...");

        Call<ProyekResponse> fallbackCall = apiService.getProyekData("getProyek");
        fallbackCall.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProyekResponse proyekResponse = response.body();
                    if (proyekResponse.isSuccess()) {
                        proyekList.clear();
                        for (Proyek proyek : proyekResponse.getData()) {
                            proyekList.add(proyek.getNamaProyek());
                        }
                        proyekAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Fallback proyek data loaded: " + proyekList.size() + " items");
                    }
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Log.e(TAG, "Fallback proyek load juga gagal: " + t.getMessage());
            }
        });
    }

    private void simpanDataHunian() {
        // Validasi input - TAMBAHKAN VALIDASI GAMBAR
        String namaHunian = editTextNamaHunian.getText().toString().trim();
        String namaProyek = spinnerProyek.getSelectedItem() != null ?
                spinnerProyek.getSelectedItem().toString() : "";
        String luasTanahStr = editTextLuasTanah.getText().toString().trim();
        String luasBangunanStr = editTextLuasBangunan.getText().toString().trim();
        String deskripsiHunian = editTextDeskripsiHunian.getText().toString().trim();

        // Validasi dasar dengan pesan yang lebih jelas
        if (namaHunian.isEmpty()) {
            editTextNamaHunian.setError("Nama hunian harus diisi");
            editTextNamaHunian.requestFocus();
            return;
        }

        if (namaProyek.isEmpty() || namaProyek.equals("Pilih Proyek") ||
                namaProyek.contains("Memuat") || namaProyek.contains("gagal")) {
            Toast.makeText(this, "Pilih proyek yang valid terlebih dahulu", Toast.LENGTH_SHORT).show();
            spinnerProyek.requestFocus();
            return;
        }

        // Validasi deskripsi
        if (deskripsiHunian.isEmpty()) {
            editTextDeskripsiHunian.setError("Deskripsi hunian harus diisi");
            editTextDeskripsiHunian.requestFocus();
            return;
        }

        // Konversi luas tanah dan bangunan ke integer
        int luasTanah, luasBangunan;
        try {
            luasTanah = Integer.parseInt(luasTanahStr);
            luasBangunan = Integer.parseInt(luasBangunanStr);

            // Validasi angka positif
            if (luasTanah <= 0 || luasBangunan <= 0) {
                Toast.makeText(this, "Luas tanah dan bangunan harus lebih dari 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format luas tanah/bangunan tidak valid. Harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Konversi list fasilitas ke JSON
        List<Map<String, Object>> fasilitasForJson = new ArrayList<>();
        for (FasilitasHunianItem fasilitas : fasilitasHunianList) {
            Map<String, Object> fasilitasMap = new HashMap<>();
            fasilitasMap.put("Nama_Fasilitas", fasilitas.getNamaFasilitas());
            fasilitasMap.put("Jumlah", fasilitas.getJumlah());
            fasilitasForJson.add(fasilitasMap);
        }

        String fasilitasJson = new Gson().toJson(fasilitasForJson);

        // Handle gambar kosong
        String finalGambarUnit = (gambarUnitBase64 == null || gambarUnitBase64.isEmpty() || gambarUnitBase64.equals("null")) ? "" : gambarUnitBase64;
        String finalGambarDenah = (gambarDenahBase64 == null || gambarDenahBase64.isEmpty() || gambarDenahBase64.equals("null")) ? "" : gambarDenahBase64;

        // Validasi ukuran gambar untuk menghindari error
        if (finalGambarUnit.length() > 1000000) { // 1MB limit
            Toast.makeText(this, "Ukuran gambar unit terlalu besar (max 1MB)", Toast.LENGTH_LONG).show();
            btnSimpan.setEnabled(true);
            btnSimpan.setText("Simpan Data Hunian");
            return;
        }

        if (finalGambarDenah.length() > 1000000) { // 1MB limit
            Toast.makeText(this, "Ukuran gambar denah terlalu besar (max 1MB)", Toast.LENGTH_LONG).show();
            btnSimpan.setEnabled(true);
            btnSimpan.setText("Simpan Data Hunian");
            return;
        }

        // Debug log lebih detail
        Log.d(TAG, "=== PARAMETER YANG AKAN DIKIRIM ===");
        Log.d(TAG, "action: addHunian");
        Log.d(TAG, "nama_hunian: " + namaHunian);
        Log.d(TAG, "nama_proyek: " + namaProyek);
        Log.d(TAG, "luas_tanah: " + luasTanah);
        Log.d(TAG, "luas_bangunan: " + luasBangunan);
        Log.d(TAG, "deskripsi_hunian: " + deskripsiHunian);
        Log.d(TAG, "fasilitas count: " + fasilitasForJson.size());
        Log.d(TAG, "fasilitas JSON: " + fasilitasJson);
        Log.d(TAG, "gambar_unit length: " + finalGambarUnit.length());
        Log.d(TAG, "gambar_denah length: " + finalGambarDenah.length());

        // PERBAIKAN: Pastikan action tidak null
        String action = "addHunian";

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String namaPenginput = sharedPreferences.getString("nama_lengkap", username);

        // ✅ PERBAIKAN: Validasi username tidak kosong
        if (username.isEmpty()) {
            Toast.makeText(this, "Username tidak ditemukan. Silakan login ulang", Toast.LENGTH_LONG).show();
            btnSimpan.setEnabled(true);
            btnSimpan.setText("Simpan Data Hunian");
            return;
        }

        if (namaPenginput.isEmpty()) {
            namaPenginput = username; // Fallback ke username jika nama kosong
        }

        Log.d(TAG, "User info - Username: " + username + ", Nama: " + namaPenginput);

        Log.d(TAG, "=== MENGIRIM DATA KE SERVER (FormUrlEncoded) ===");

        // Pastikan gambar tidak null
        String gambarUnitFinal = (finalGambarUnit == null || finalGambarUnit.isEmpty() || finalGambarUnit.equals("null")) ? "" : finalGambarUnit;
        String gambarDenahFinal = (finalGambarDenah == null || finalGambarDenah.isEmpty() || finalGambarDenah.equals("null")) ? "" : finalGambarDenah;

        Log.d(TAG, "Gambar Unit length: " + gambarUnitFinal.length());
        Log.d(TAG, "Gambar Denah length: " + gambarDenahFinal.length());

        // Trim nama proyek (hilangkan "Pilih Proyek" jika ada)
        String namaProyekFinal = namaProyek;
        if (namaProyekFinal.contains("Pilih Proyek")) {
            Toast.makeText(this, "Pilih proyek yang valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<BasicResponse> call = apiService.addHunianComprehensive(
                "addHunianComprehensive", // action
                namaHunian,
                namaProyekFinal,
                gambarUnitFinal,
                gambarDenahFinal,
                luasTanah,
                luasBangunan,
                deskripsiHunian,
                fasilitasJson,
                username,
                namaPenginput
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan Data Hunian");

                Log.d(TAG, "=== RESPONSE DARI SERVER ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    Log.d(TAG, "Success: " + basicResponse.isSuccess());
                    Log.d(TAG, "Message: " + basicResponse.getMessage());

                    if (basicResponse.isSuccess()) {
                        // ✅ PERBAIKAN: Simpan ID hunian ke variabel class
                        currentHunianId = basicResponse.getIdHunian() != null ? basicResponse.getIdHunian() : 0;

                        Toast.makeText(InputHunianActivity.this,
                                "✅ " + basicResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        // ✅ PERBAIKAN: Gunakan ID yang sudah disimpan
                        saveHunianHistoriAfterSuccess(
                                currentHunianId, // Gunakan variabel class
                                namaHunian,
                                namaProyekFinal,
                                luasTanah,
                                luasBangunan,
                                gambarUnitFinal
                        );

                        resetForm();
                        new android.os.Handler().postDelayed(
                                () -> navigateToHome(),
                                1500
                        );
                    } else {
                        Toast.makeText(InputHunianActivity.this,
                                "❌ " + basicResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // DEBUG: Coba parse error sebagai JSON
                    try {
                        String errorJson = response.errorBody() != null ?
                                response.errorBody().string() : "{}";
                        Log.e(TAG, "Error JSON: " + errorJson);

                        // Coba parse sebagai JSON
                        if (errorJson.startsWith("{")) {
                            JSONObject errorObj = new JSONObject(errorJson);
                            String errorMsg = errorObj.optString("message", "Unknown error");
                            Toast.makeText(InputHunianActivity.this,
                                    "Server Error: " + errorMsg,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(InputHunianActivity.this,
                                    "HTTP Error " + response.code() + ": " + errorJson,
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(InputHunianActivity.this,
                                "HTTP Error " + response.code() + " (Cannot parse error)",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan Data Hunian");

                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(InputHunianActivity.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // PERBAIKAN: Di InputHunianActivity.java - method saveHunianHistoriAfterSuccess()
    private void saveHunianHistoriAfterSuccess(int hunianId, String namaHunian, String namaProyek,
                                               int luasTanah, int luasBangunan, String finalGambarUnit) {

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String penginput = prefs.getString("nama_lengkap", prefs.getString("username", "User"));

        Log.d(TAG, "📝 Saving hunian histori to server...");
        Log.d(TAG, "  - Hunian ID: " + hunianId);
        Log.d(TAG, "  - Nama Hunian: " + namaHunian);
        Log.d(TAG, "  - Nama Proyek: " + namaProyek);
        Log.d(TAG, "  - Luas Tanah: " + luasTanah);
        Log.d(TAG, "  - Luas Bangunan: " + luasBangunan);
        Log.d(TAG, "  - Penginput: " + penginput);
        Log.d(TAG, "  - Image length: " + (finalGambarUnit != null ? finalGambarUnit.length() : 0));

        // ✅ PERBAIKAN: Validasi ID hunian
        if (hunianId <= 0) {
            Log.e(TAG, "❌ Invalid hunian ID for histori: " + hunianId);
            return;
        }

        // ✅ PERBAIKAN: Validasi gambar untuk histori
        String imageDataForHistori = "";
        if (finalGambarUnit != null && !finalGambarUnit.isEmpty() &&
                !finalGambarUnit.equals("null") && !finalGambarUnit.equals("NULL")) {

            String cleanImage = finalGambarUnit.trim();

            // Untuk hunian, kriteria lebih longgar
            if (cleanImage.length() >= 30 &&
                    !cleanImage.startsWith("data:image") && // Hindari data URL
                    !cleanImage.endsWith("...") &&
                    !cleanImage.endsWith("..")) {

                imageDataForHistori = cleanImage;
                Log.d(TAG, "✅ Using image for hunian histori, length: " + imageDataForHistori.length());
            }
        }

        // ✅ PERBAIKAN: Gunakan thread baru agar tidak block UI
        String finalImageDataForHistori = imageDataForHistori;
        new Thread(() -> {
            try {
                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

                // ✅ PERBAIKAN: Tambahkan timeout
                Call<BasicResponse> call = apiService.addHunianHistori(
                        "add_hunian_histori", // action
                        hunianId,
                        namaHunian,
                        namaProyek,
                        luasTanah,       // ✅ Tambahkan luas tanah
                        luasBangunan,    // ✅ Tambahkan luas bangunan
                        penginput,
                        "Ditambahkan",   // ✅ Status
                        finalImageDataForHistori
                );

                Response<BasicResponse> response = call.execute(); // Gunakan execute() untuk synchronous call

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d(TAG, "✅ Hunian histori saved successfully to server");

                        // Kirim broadcast untuk refresh NewsActivity
                        sendHunianHistoriBroadcast("Ditambahkan", namaHunian, namaProyek, penginput);
                    } else {
                        Log.e(TAG, "❌ Server response failed: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ HTTP error saving hunian histori: " + response.code());
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Network error saving hunian histori: " + e.getMessage());

                // Tetap kirim broadcast meskipun histori gagal
                sendHunianHistoriBroadcast("Ditambahkan", namaHunian, namaProyek, penginput);
            }
        }).start();
    }

    // ✅ Method untuk kirim broadcast
    private void sendHunianHistoriBroadcast(String status, String namaHunian, String namaProyek, String penginput) {
        Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
        broadcastIntent.putExtra("ACTION", "HUNIAN_ADDED");
        broadcastIntent.putExtra("TYPE", "hunian");
        broadcastIntent.putExtra("NAMA_HUNIAN", namaHunian);
        broadcastIntent.putExtra("NAMA_PROYEK", namaProyek);
        broadcastIntent.putExtra("PENGINPUT", penginput);
        broadcastIntent.putExtra("HUNIAN_ID", currentHunianId);
        broadcastIntent.putExtra("STATUS", status);

        sendBroadcast(broadcastIntent);
        Log.d(TAG, "📡 Broadcast sent for hunian: " + namaHunian);
    }


    private void resetForm() {
        editTextNamaHunian.setText("");
        editTextLuasTanah.setText("");
        editTextLuasBangunan.setText("");
        editTextDeskripsiHunian.setText("");
        editTextNamaFasilitas.setText("");
        editTextJumlahFasilitas.setText("");
        spinnerProyek.setSelection(0);

        imagePreviewUnit.setVisibility(View.GONE);
        imagePreviewDenah.setVisibility(View.GONE);
        recyclerViewFasilitas.setVisibility(View.GONE);
        textListFasilitas.setVisibility(View.GONE);

        gambarUnitUri = null;
        gambarDenahUri = null;
        gambarUnitBase64 = "";
        gambarDenahBase64 = "";

        fasilitasHunianList.clear();
        fasilitasAdapter.notifyDataSetChanged();
    }

    private void navigateToHome() {
        Intent intent = new Intent(InputHunianActivity.this, NewBeranda.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHome();
    }
}