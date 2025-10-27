package com.example.mobile;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.RequiresApi;
import java.io.OutputStream;
public class LihatDataUserpActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private UserProspekAdapter adapter;
    private List<UserProspekSimple> userProspekList;
    private List<UserProspekSimple> filteredList;
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private String userLevel; // Simpan level (Admin, Operator, dll)
    private String userName;  // Simpan username untuk parameter penginput
    private ApiService apiService;
    private static final String TAG = "LihatDataUserp";

    // FIX: Gunakan KEY yang sama dengan MainActivity
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LEVEL = "level";
    private static final int STORAGE_PERMISSION_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_userp);

        // Inisialisasi Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // FIX: Ambil data dari SharedPreferences yang benar
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userLevel = prefs.getString(KEY_LEVEL, "");  // Level: Admin, Operator, dll
        userName = prefs.getString(KEY_USERNAME, ""); // Username: untuk parameter penginput

        // DEBUG: Cek SharedPreferences
        Log.d(TAG, "=== SHARED PREFERENCES DEBUG ===");
        Log.d(TAG, "Prefs Name: " + PREFS_NAME);
        Log.d(TAG, "All stored data: " + prefs.getAll().toString());
        Log.d(TAG, "User Level: '" + userLevel + "'");
        Log.d(TAG, "User Name: '" + userName + "'");

        // Inisialisasi view
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userProspekList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserProspekAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadUserProspekData();
        checkStoragePermission();

        // Navigation
        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataUserpActivity.this, LihatDataActivity.class);
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

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserProspekData() {
        // FIX: Logic parameter berdasarkan LEVEL bukan ROLE
        String penginputParam;

        if (userLevel.equals("Admin")) {
            // Admin bisa lihat semua data
            penginputParam = "all";
        } else {
            // Operator, Freelance, Inhouse hanya lihat data mereka sendiri
            penginputParam = userName; // Username sebagai filter
        }

        Log.d(TAG, "🎯 === LOAD DATA PARAMETERS ===");
        Log.d(TAG, "🎯 User Level: " + userLevel);
        Log.d(TAG, "🎯 User Name: " + userName);
        Log.d(TAG, "🎯 Parameter dikirim: " + penginputParam);

        Call<UserProspekSimpleResponse> call = apiService.getUserProspekSimpleData("getUserProspekSimple", penginputParam);
        call.enqueue(new Callback<UserProspekSimpleResponse>() {
            @Override
            public void onResponse(Call<UserProspekSimpleResponse> call, Response<UserProspekSimpleResponse> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG, "🎯 === API RESPONSE ===");
                            Log.d(TAG, "🎯 Response code: " + response.code());

                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    UserProspekSimpleResponse apiResponse = response.body();
                                    Log.d(TAG, "🎯 API Success: " + apiResponse.isSuccess());
                                    Log.d(TAG, "🎯 API Message: " + apiResponse.getMessage());

                                    if (apiResponse.isSuccess()) {
                                        List<UserProspekSimple> data = apiResponse.getData();

                                        Log.d(TAG, "🎯 Data reference: " + data);
                                        Log.d(TAG, "🎯 Data == null: " + (data == null));

                                        if (data != null) {
                                            Log.d(TAG, "🎯 Data.size(): " + data.size());
                                            Log.d(TAG, "🎯 Data.isEmpty(): " + data.isEmpty());

                                            // DEBUG: Log first few items
                                            if (data.size() > 0) {
                                                for (int i = 0; i < Math.min(data.size(), 3); i++) {
                                                    UserProspekSimple item = data.get(i);
                                                    Log.d(TAG, "🎯 Item " + i + ": " + item.getNama() + " by " + item.getPenginput());
                                                }
                                            }

                                            if (!data.isEmpty()) {
                                                userProspekList.clear();
                                                userProspekList.addAll(data);

                                                filteredList.clear();
                                                filteredList.addAll(userProspekList);

                                                if (adapter != null) {
                                                    adapter.notifyDataSetChanged();
                                                }

                                                String message = userLevel.equals("Admin") ?
                                                        "✅ Data semua user: " + userProspekList.size() + " items" :
                                                        "✅ Data Anda: " + userProspekList.size() + " items";

                                                Toast.makeText(LihatDataUserpActivity.this, message, Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "🎯 SUCCESS: Data loaded to UI - " + userProspekList.size() + " items");
                                            } else {
                                                Log.d(TAG, "📭 Data list is EMPTY");
                                                userProspekList.clear();
                                                filteredList.clear();
                                                if (adapter != null) {
                                                    adapter.notifyDataSetChanged();
                                                }

                                                String message = userLevel.equals("Admin") ?
                                                        "📭 Tidak ada data prospek untuk semua user" :
                                                        "📭 Tidak ada data prospek untuk user: " + userName;

                                                Toast.makeText(LihatDataUserpActivity.this, message, Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Log.d(TAG, "❌ Data is NULL from API");
                                            Toast.makeText(LihatDataUserpActivity.this, "❌ Data null dari API", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error";
                                        Log.e(TAG, "❌ API Error: " + errorMsg);
                                        Toast.makeText(LihatDataUserpActivity.this, "❌ Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(TAG, "❌ Response body is NULL");
                                    Toast.makeText(LihatDataUserpActivity.this, "❌ Response body null", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "❌ HTTP Error: " + response.code());
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                    Log.e(TAG, "❌ Error body: " + errorBody);
                                } catch (Exception e) {
                                    Log.e(TAG, "❌ Error reading error body: " + e.getMessage());
                                }
                                Toast.makeText(LihatDataUserpActivity.this, "❌ HTTP Error: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Exception in onResponse", e);
                            Toast.makeText(LihatDataUserpActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "🎯 === END DEBUG ===");
                    }
                });
            }

            @Override
            public void onFailure(Call<UserProspekSimpleResponse> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "❌ API CALL FAILED: " + t.getMessage(), t);
                        Toast.makeText(LihatDataUserpActivity.this, "❌ Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted, silakan cetak ulang", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied, tidak bisa menyimpan PDF", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void generatePDF(UserProspekSimple userProspek) {
        try {
            // Cek permission terlebih dahulu
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(() -> {
                        Toast.makeText(LihatDataUserpActivity.this, "Mohon berikan permission storage terlebih dahulu", Toast.LENGTH_LONG).show();
                        checkStoragePermission();
                    });
                    return;
                }
            }

            // Buat nama file dengan timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Penerimaan_Kas_" + timeStamp + ".pdf";

            File file;

            // Untuk Android 10+ gunakan MediaStore, untuk yang lama gunakan traditional method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Gunakan MediaStore
                file = createFileUsingMediaStore(fileName);
            } else {
                // Android 9 dan bawah - Gunakan traditional method
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }
                file = new File(downloadsDir, fileName);
            }

            if (file == null) {
                runOnUiThread(() -> {
                    Toast.makeText(LihatDataUserpActivity.this, "Gagal membuat file", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Buat PDF writer
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Set margin
            document.setMargins(50, 50, 50, 50);

            // Header
            Paragraph header1 = new Paragraph("PT. BUMI JATIKALANG SEJAHTERA")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(14);

            Paragraph header2 = new Paragraph("PENERIMAAN KAS (BOOKING KAVLING)")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(12)
                    .setMarginTop(5);

            document.add(header1);
            document.add(header2);

            // Nomor dan Tanggal Input Prospek
            String nomorDokumen = "KM" + new SimpleDateFormat("ddMMyy", Locale.getDefault()).format(new Date()) + "/B001";

            Paragraph nomor = new Paragraph("Nomor : " + nomorDokumen)
                    .setFontSize(10)
                    .setMarginTop(15);

            document.add(nomor);

            // Tanggal Input Prospek
            if (userProspek.getTanggal() != null && !userProspek.getTanggal().isEmpty()) {
                String tanggalInput = formatDateForPDF(userProspek.getTanggal());
                Paragraph tanggalInputParagraph = new Paragraph("Tanggal Input : " + tanggalInput)
                        .setFontSize(9)
                        .setMarginTop(2);
                document.add(tanggalInputParagraph);
            }

            // Terima Dari
            Paragraph terimaDari = new Paragraph("Terima Dari :")
                    .setFontSize(10)
                    .setMarginTop(10);

            String infoHunian = userProspek.getHunian() != null ? " (" + userProspek.getHunian() + ")" : "";
            Paragraph namaUser = new Paragraph(userProspek.getNama() + infoHunian)
                    .setFontSize(10)
                    .setBold()
                    .setMarginTop(5);

            document.add(terimaDari);
            document.add(namaUser);

            // TAMBAHAN: Tanggal Cetak di atas tabel, pojok kanan
            String tanggalCetak = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Paragraph tanggalCetakParagraph = new Paragraph("Cetak: " + tanggalCetak)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                    .setMarginTop(10); // Margin di atas tabel

            document.add(tanggalCetakParagraph);

            // Tabel utama
            float[] columnWidths = {1f, 5f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(5); // Margin kecil antara tanggal cetak dan tabel

            // Header tabel
            table.addHeaderCell(new Paragraph("No").setFontSize(8));
            table.addHeaderCell(new Paragraph("Keterangan").setFontSize(8));
            table.addHeaderCell(new Paragraph("Jumlah").setFontSize(8));

            // Isi tabel
            table.addCell(new Paragraph("1").setFontSize(8));

            // Format keterangan sesuai permintaan
            String keterangan = "Uang Tanda Jadi " +
                    (userProspek.getTipeHunian() != null ? "Type : " + userProspek.getTipeHunian() + ", " : "") +
                    (userProspek.getHunian() != null ? "Blok/No : " + userProspek.getHunian() + ", " : "") +
                    "di Perumahan " +
                    (userProspek.getProyek() != null ? userProspek.getProyek() : "The Quality Riverside") +
                    ", a/n " + userProspek.getNama();

            table.addCell(new Paragraph(keterangan).setFontSize(8));

            // Format jumlah uang
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String jumlahUang = formatRupiah.format(userProspek.getDp())
                    .replace("Rp", "").trim();

            table.addCell(new Paragraph(jumlahUang).setFontSize(8).setTextAlignment(TextAlignment.RIGHT));

            // Total
            table.addCell(new Paragraph("").setFontSize(8));
            table.addCell(new Paragraph("Total Rp.").setFontSize(8).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Paragraph(jumlahUang).setFontSize(8).setTextAlignment(TextAlignment.RIGHT));

            document.add(table);

            // Terbilang
            String terbilang = terbilang(userProspek.getDp()) + " rupiah";

            Paragraph textTerbilang = new Paragraph("terbilang :")
                    .setFontSize(8)
                    .setMarginTop(10);

            Paragraph isiTerbilang = new Paragraph(terbilang)
                    .setFontSize(8)
                    .setItalic();

            document.add(textTerbilang);
            document.add(isiTerbilang);

            document.close();

            runOnUiThread(() -> {
                Toast.makeText(LihatDataUserpActivity.this, "PDF berhasil disimpan: " + fileName, Toast.LENGTH_LONG).show();
                Log.d(TAG, "PDF saved: " + file.getAbsolutePath());
            });

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(LihatDataUserpActivity.this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "PDF creation error: " + e.getMessage(), e);
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(LihatDataUserpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
    private String formatDateForPDF(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            // Jika format pertama gagal, coba format tanpa jam
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception ex) {
                return dateString; // Kembalikan string asli jika parsing gagal
            }
        }
    }
    // Method untuk Android 10+ menggunakan MediaStore
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private File createFileUsingMediaStore(String fileName) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

            if (uri != null) {
                // Untuk MediaStore, kita tidak bisa mendapatkan File object langsung
                // Tapi kita bisa menggunakan OutputStream dari ContentResolver
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    // Karena kita butuh File untuk iText, kita buat file temporary dulu
                    File tempFile = new File(getCacheDir(), fileName);
                    return tempFile;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating file with MediaStore: " + e.getMessage());
        }
        return null;
    }

    // Alternatif: Gunakan internal storage jika external tidak available
    private File getDownloadDirectory() {
        File dir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MobileApp");
        } else {
            dir = new File(getFilesDir(), "Downloads");
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    // Method untuk konversi angka ke terbilang
    private String terbilang(long angka) {
        String[] bilangan = {"", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan", "sepuluh", "sebelas"};

        if (angka < 12) {
            return bilangan[(int) angka];
        } else if (angka < 20) {
            return terbilang(angka - 10) + " belas";
        } else if (angka < 100) {
            return terbilang(angka / 10) + " puluh " + terbilang(angka % 10);
        } else if (angka < 200) {
            return "seratus " + terbilang(angka - 100);
        } else if (angka < 1000) {
            return terbilang(angka / 100) + " ratus " + terbilang(angka % 100);
        } else if (angka < 2000) {
            return "seribu " + terbilang(angka - 1000);
        } else if (angka < 1000000) {
            return terbilang(angka / 1000) + " ribu " + terbilang(angka % 1000);
        } else if (angka < 1000000000) {
            return terbilang(angka / 1000000) + " juta " + terbilang(angka % 1000000);
        } else {
            return "angka terlalu besar";
        }
    }
    private void filterData(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userProspekList);
        } else {
            for (UserProspekSimple userProspek : userProspekList) {
                if (userProspek.getNama() != null && userProspek.getNama().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(userProspek);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private String formatCurrency(int amount) {
        try {
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            formatter.applyPattern("#,###");
            return formatter.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    private void showEditDialog(UserProspekSimple userProspek) {
        // FIX: Cek level untuk hak akses edit
        if (!userLevel.equals("Admin")) {
            Toast.makeText(this, "Hanya Admin yang dapat mengedit data", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(LihatDataUserpActivity.this, EditDataUserpActivity.class);
        intent.putExtra("USER_PROSPEK_ID", userProspek.getId());
        intent.putExtra("PENGINPUT", userProspek.getPenginput());
        intent.putExtra("NAMA", userProspek.getNama());
        intent.putExtra("EMAIL", userProspek.getEmail());
        intent.putExtra("NO_HP", userProspek.getNohp());
        intent.putExtra("ALAMAT", userProspek.getAlamat());
        intent.putExtra("PROYEK", userProspek.getProyek());
        intent.putExtra("HUNIAN", userProspek.getHunian());
        intent.putExtra("TIPE_HUNIAN", userProspek.getTipeHunian());
        intent.putExtra("DP", userProspek.getDp());
        intent.putExtra("STATUS_BPJS", userProspek.getBpjs());
        intent.putExtra("STATUS_NPWP", userProspek.getNpwp());
        startActivityForResult(intent, 1);
    }

    private void showDeleteConfirmation(UserProspekSimple userProspek) {
        // FIX: Cek level untuk hak akses delete
        if (!userLevel.equals("Admin")) {
            Toast.makeText(this, "Hanya Admin yang dapat menghapus data", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus data " + userProspek.getNama() + "?")
                .setPositiveButton("Ya", (dialog, which) -> {
                   // deleteUserProspek(userProspek);
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

   /* private void deleteUserProspek(UserProspekSimple userProspek) {
        Call<BasicResponse> call = apiService.deleteProspekByData(
                userProspek.getPenginput(),
                userProspek.getNama()
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isSuccess()) {
                                Toast.makeText(LihatDataUserpActivity.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                                loadUserProspekData();
                            } else {
                                Toast.makeText(LihatDataUserpActivity.this,
                                        "Gagal menghapus data: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LihatDataUserpActivity.this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LihatDataUserpActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadUserProspekData();
        }
    }

    // Adapter class dengan null safety
    private class UserProspekAdapter extends RecyclerView.Adapter<UserProspekAdapter.ViewHolder> {

        private List<UserProspekSimple> userProspekList;

        public UserProspekAdapter(List<UserProspekSimple> userProspekList) {
            this.userProspekList = userProspekList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_userp, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            UserProspekSimple userProspek = userProspekList.get(position);

            // Null safety untuk semua field
            holder.tvPenginput.setText("Penginput: " + (userProspek.getPenginput() != null ? userProspek.getPenginput() : "-"));
            holder.tvTanggal.setText("Tanggal: " + (userProspek.getTanggal() != null ? formatDate(userProspek.getTanggal()) : "-"));
            holder.tvNama.setText("Nama: " + (userProspek.getNama() != null ? userProspek.getNama() : "-"));
            holder.tvEmail.setText("Email: " + (userProspek.getEmail() != null ? userProspek.getEmail() : "-"));
            holder.tvNoHp.setText("No. HP: " + (userProspek.getNohp() != null ? userProspek.getNohp() : "-"));
            holder.tvAlamat.setText("Alamat: " + (userProspek.getAlamat() != null ? userProspek.getAlamat() : "-"));

            String formattedDP = "DP: Rp " + formatCurrency(userProspek.getDp());
            holder.tvJumlahUangTandaJadi.setText(formattedDP);
            // TAMPILKAN HUNIAN
            holder.tvHunian.setText("Hunian: " + (userProspek.getHunian() != null ? userProspek.getHunian() : "-"));

            // TAMPILKAN TIPE HUNIAN
            holder.tvTipeHunian.setText("Tipe Hunian: " + (userProspek.getTipeHunian() != null ? userProspek.getTipeHunian() : "-"));

            // FIX: Tampilkan edit button hanya untuk Admin
            if (holder.btnEdit != null) {
                if (userLevel.equals("Admin")) {
                    holder.btnEdit.setVisibility(View.VISIBLE);
                    holder.btnEdit.setOnClickListener(v -> showEditDialog(userProspek));
                } else {
                    holder.btnEdit.setVisibility(View.GONE);
                }
            }
            if (holder.btnHistori != null) {
                holder.btnHistori.setVisibility(View.VISIBLE);
                holder.btnHistori.setOnClickListener(v -> {
                    try {
                        // Intent ke halaman histori - PERBAIKI NAMA ACTIVITY
                        Intent intent = new Intent(LihatDataUserpActivity.this, LihatDataHistoriUserProspek.class);

                        // Kirim data yang diperlukan
                        intent.putExtra("USER_PROSPEK_ID", userProspek.getId());
                        intent.putExtra("NAMA", userProspek.getNama());
                        intent.putExtra("PENGINPUT", userProspek.getPenginput());

                        // Debug log
                        Log.d(TAG, "🎯 Button Histori diklik:");
                        Log.d(TAG, "🎯 USER_PROSPEK_ID: " + userProspek.getId());
                        Log.d(TAG, "🎯 NAMA: " + userProspek.getNama());
                        Log.d(TAG, "🎯 PENGINPUT: " + userProspek.getPenginput());

                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error saat membuka histori: " + e.getMessage());
                        Toast.makeText(LihatDataUserpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(userProspek));
            }
            // TAMBAHAN: Button Cetak - selalu visible
            if (holder.btnCetak != null) {
                holder.btnCetak.setVisibility(View.VISIBLE);
                holder.btnCetak.setOnClickListener(v -> {
                    try {
                        Log.d(TAG, "🎯 Button Cetak diklik untuk: " + userProspek.getNama());

                        // Cek permission sebelum generate PDF
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                Toast.makeText(LihatDataUserpActivity.this, "Silakan berikan permission storage, lalu tekan cetak lagi", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        // Jalankan generate PDF di thread terpisah
                        new Thread(() -> {
                            generatePDF(userProspek);
                        }).start();

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error saat mencetak PDF: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(LihatDataUserpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return userProspekList != null ? userProspekList.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPenginput, tvTanggal, tvNama, tvEmail, tvNoHp, tvAlamat, tvJumlahUangTandaJadi, tvHunian, tvTipeHunian;
            MaterialButton btnEdit, btnDelete,btnHistori,btnCetak;;

            public ViewHolder(View itemView) {
                super(itemView);
                tvPenginput = itemView.findViewById(R.id.tvPenginput);
                tvTanggal = itemView.findViewById(R.id.tvTanggal);
                tvNama = itemView.findViewById(R.id.tvNama);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvNoHp = itemView.findViewById(R.id.tvNoHp);
                tvAlamat = itemView.findViewById(R.id.tvAlamat);
                tvHunian = itemView.findViewById(R.id.tvHunian); // TAMBAHAN
                tvTipeHunian = itemView.findViewById(R.id.tvTipeHunian); // TAMBAHAN
                tvJumlahUangTandaJadi = itemView.findViewById(R.id.tvJumlahUangTandaJadi);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnHistori = itemView.findViewById(R.id.btnHistori);
                btnCetak = itemView.findViewById(R.id.btnCetak);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProspekData();
    }
}