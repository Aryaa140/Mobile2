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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import java.util.Arrays;
import java.util.Calendar;
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
    private String userLevel;
    private String userName;
    private ApiService apiService;
    private static final String TAG = "LihatDataUserp";
    private static final int REQUEST_REALISASI = 100;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LEVEL = "level";
    private static final int STORAGE_PERMISSION_CODE = 100;

    private UserProspekSimple pendingPrintProspek = null;
    private boolean isPermissionRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_userp);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userLevel = prefs.getString(KEY_LEVEL, "");
        userName = prefs.getString(KEY_USERNAME, "");

        Log.d(TAG, "=== SHARED PREFERENCES DEBUG ===");
        Log.d(TAG, "User Level: '" + userLevel + "'");
        Log.d(TAG, "User Name: '" + userName + "'");

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userProspekList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserProspekAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        loadUserProspekData();

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
        String penginputParam;

        if (userLevel.equals("Admin")) {
            penginputParam = "all";
        } else {
            penginputParam = userName;
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

                                        if (data != null) {
                                            Log.d(TAG, "🎯 Data.size(): " + data.size());

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
                                Toast.makeText(LihatDataUserpActivity.this, "❌ HTTP Error: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Exception in onResponse", e);
                            Toast.makeText(LihatDataUserpActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
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
    private void showRealisasiDialog(UserProspekSimple userProspek) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Realisasi Data User Prospek");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_realisasi, null);
        builder.setView(dialogView);

        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        MaterialButton btnSimpanRealisasi = dialogView.findViewById(R.id.btnSimpanRealisasi);
        MaterialButton btnBatalRealisasi = dialogView.findViewById(R.id.btnBatalRealisasi);

        AlertDialog dialog = builder.create();

        btnSimpanRealisasi.setOnClickListener(v -> {
            // Get selected date
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String tanggalRealisasi = dateFormat.format(calendar.getTime());

            // Panggil API untuk realisasi
            realisasiUserProspek(userProspek, tanggalRealisasi);
            dialog.dismiss();
        });

        btnBatalRealisasi.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void realisasiUserProspek(UserProspekSimple userProspek, String tanggalRealisasi) {
        Log.d(TAG, "🎯 Memproses realisasi untuk: " + userProspek.getNama());

        Call<BasicResponse> call = apiService.realisasiUserProspek(
                userProspek.getId(),
                userProspek.getNama(),
                userProspek.getPenginput(),
                userProspek.getEmail(),
                userProspek.getNohp(),
                userProspek.getAlamat(),
                userProspek.getProyek(),
                userProspek.getHunian(),
                userProspek.getTipeHunian(),
                userProspek.getDp(),
                userProspek.getBpjs(),
                userProspek.getNpwp(),
                userProspek.getTanggal(),
                tanggalRealisasi
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Toast.makeText(LihatDataUserpActivity.this,
                                    "✅ Data berhasil direalisasikan!", Toast.LENGTH_LONG).show();
                            // Refresh data setelah berhasil realisasi
                            loadUserProspekData();
                        } else {
                            Toast.makeText(LihatDataUserpActivity.this,
                                    "❌ Gagal: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LihatDataUserpActivity.this,
                                "❌ Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ API Call Failed: " + t.getMessage(), t);
                    Toast.makeText(LihatDataUserpActivity.this,
                            "❌ Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // PERBAIKAN: Method untuk request permission dengan explanation jika diperlukan
    private void requestStoragePermissionWithExplanation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Tampilkan penjelasan mengapa permission diperlukan
            new AlertDialog.Builder(this)
                    .setTitle("Izin Penyimpanan Diperlukan")
                    .setMessage("Aplikasi membutuhkan izin penyimpanan untuk menyimpan file PDF hasil cetak. Izin ini diperlukan untuk menyimpan dokumen ke folder Downloads.")
                    .setPositiveButton("Berikan Izin", (dialog, which) -> {
                        requestStoragePermission();
                    })
                    .setNegativeButton("Tolak", (dialog, which) -> {
                        Toast.makeText(this, "Fitur cetak tidak dapat digunakan tanpa izin penyimpanan", Toast.LENGTH_LONG).show();
                    })
                    .show();
        } else {
            // Langsung minta permission
            requestStoragePermission();
        }
    }

    private void requestStoragePermission() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        Log.d(TAG, "Requesting permissions: " + Arrays.toString(permissions));
        ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE);
    }


    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Izin Diperlukan")
                .setMessage("Izin penyimpanan ditolak secara permanen. Untuk menggunakan fitur cetak, Anda perlu memberikan izin di Settings. Buka Settings sekarang?")
                .setPositiveButton("BUKA SETTINGS", (dialog, which) -> {
                    openAppSettings();
                })
                .setNegativeButton("TUTUP", (dialog, which) -> {
                    Toast.makeText(this, "Fitur cetak tidak dapat digunakan", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
    // DI LihatDataUserpActivity - HAPUS SEMUA METHOD PERMISSION YANG ADA
// DAN GANTI DENGAN INI:

    private boolean hasStoragePermission() {
        return PermissionUtils.isStoragePermissionGranted(this);
    }

    private void handlePrintButton(UserProspekSimple userProspek) {
        Log.d(TAG, "🎯 Handle print untuk: " + userProspek.getNama());

        // SIMPAN DATA YANG AKAN DICETAK
        pendingPrintProspek = userProspek;

        // DEBUG: Cek status permission
        Log.d(TAG, "=== BEFORE PERMISSION CHECK ===");
        boolean hasPerm = hasStoragePermission();
        Log.d(TAG, "Has Storage Permission: " + hasPerm);

        if (hasPerm) {
            Log.d(TAG, "Permission SUDAH diberikan, langsung generate PDF");
            new Thread(() -> generatePDF(userProspek)).start();
        } else {
            Log.d(TAG, "Permission BELUM diberikan, meminta permission...");

            // Gunakan PermissionUtils yang sama dengan NewBeranda
            PermissionUtils.requestStoragePermissions(this);
        }
    }

    // PERBAIKI onRequestPermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult - Request Code: " + requestCode);

        // Handle untuk semua tipe permission request
        if (requestCode == PermissionUtils.STORAGE_PERMISSION_CODE ||
                requestCode == PermissionUtils.ALL_PERMISSIONS_CODE) {

            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Izin penyimpanan diberikan!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Storage permission granted");

                // Lanjutkan dengan generate PDF
                if (pendingPrintProspek != null) {
                    new Thread(() -> generatePDF(pendingPrintProspek)).start();
                    pendingPrintProspek = null;
                }
            } else {
                // Jangan tampilkan pesan "ditolak secara permanen"
                // Cukup arahkan ke internal storage
                Log.d(TAG, "Some permissions denied, using internal storage");

                if (pendingPrintProspek != null) {
                    new Thread(() -> generatePDF(pendingPrintProspek)).start();
                }
            }
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Izin Penyimpanan Diperlukan")
                .setMessage("Aplikasi membutuhkan izin penyimpanan untuk:\n• Menyimpan file PDF hasil cetak\n• Menyimpan ke folder Downloads\n\nIzin ini aman dan hanya digunakan untuk fungsi cetak.")
                .setPositiveButton("BERIKAN IZIN", (dialog, which) -> {
                    requestStoragePermission();
                })
                .setNegativeButton("TOLAK", (dialog, which) -> {
                    Toast.makeText(this, "Fitur cetak tidak dapat digunakan", Toast.LENGTH_LONG).show();
                    pendingPrintProspek = null;
                })
                .setCancelable(false)
                .show();
    }

    private void generatePDF(UserProspekSimple userProspek) {
        Log.d(TAG, "=== START GENERATE PDF ===");

        try {
            // GUNAKAN INTERNAL STORAGE SAJA - TIDAK PERLU PERMISSION
            File internalDir = new File(getFilesDir(), "PDF_Documents");
            if (!internalDir.exists()) {
                boolean created = internalDir.mkdirs();
                Log.d(TAG, "Internal directory created: " + created);
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Penerimaan_Kas_" + userProspek.getNama() + "_" + timeStamp + ".pdf";
            File file = new File(internalDir, fileName);

            Log.d(TAG, "PDF will be saved to: " + file.getAbsolutePath());

            // Buat PDF writer
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Set margin
            document.setMargins(50, 50, 50, 50);

            // Header
            Paragraph header1 = new Paragraph("THE QUALITY GROUP")
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

            // Tanggal Cetak
            String tanggalCetak = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            Paragraph tanggalCetakParagraph = new Paragraph("Cetak: " + tanggalCetak)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                    .setMarginTop(10);
            document.add(tanggalCetakParagraph);

            // Tabel utama
            float[] columnWidths = {1f, 5f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(5);

            // Header tabel
            table.addHeaderCell(new Paragraph("No").setFontSize(8));
            table.addHeaderCell(new Paragraph("Keterangan").setFontSize(8));
            table.addHeaderCell(new Paragraph("Jumlah").setFontSize(8));

            // Isi tabel
            table.addCell(new Paragraph("1").setFontSize(8));

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
                Toast.makeText(LihatDataUserpActivity.this,
                        "PDF berhasil disimpan!\nFile: " + fileName,
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "PDF successfully saved: " + file.getAbsolutePath());

                // Share PDF file
                sharePDFFile(file);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error generating PDF", e);
            runOnUiThread(() -> {
                Toast.makeText(LihatDataUserpActivity.this,
                        "Gagal membuat PDF: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void sharePDFFile(File file) {
        try {
            // Gunakan FileProvider untuk mendapatkan URI yang aman
            Uri fileUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    file);

            // Buat intent untuk share
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Penerimaan Kas - " + (pendingPrintProspek != null ? pendingPrintProspek.getNama() : ""));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Dokumen Penerimaan Kas");

            // Berikan izin baca ke aplikasi yang menerima intent
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Tampilkan dialog share
            startActivity(Intent.createChooser(shareIntent, "Bagikan PDF"));

            Log.d(TAG, "PDF shared successfully: " + file.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Error sharing PDF", e);

            // Fallback: show file location
            runOnUiThread(() -> {
                Toast.makeText(this,
                        "PDF disimpan di: " + file.getAbsolutePath() +
                                "\nError sharing: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
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
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception ex) {
                return dateString;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private File createFileUsingMediaStore(String fileName) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

            if (uri != null) {
                // Langsung return file dari cache, nanti akan disimpan via OutputStream
                File tempFile = new File(getCacheDir(), fileName);
                return tempFile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating file with MediaStore: " + e.getMessage());

            // Fallback: gunakan cache directory
            File tempFile = new File(getCacheDir(), fileName);
            return tempFile;
        }
        return null;
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadUserProspekData();
        }
    }

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

            holder.tvPenginput.setText("Penginput: " + (userProspek.getPenginput() != null ? userProspek.getPenginput() : "-"));
            holder.tvTanggal.setText("Tanggal: " + (userProspek.getTanggal() != null ? formatDate(userProspek.getTanggal()) : "-"));
            holder.tvNama.setText("Nama: " + (userProspek.getNama() != null ? userProspek.getNama() : "-"));
            holder.tvEmail.setText("Email: " + (userProspek.getEmail() != null ? userProspek.getEmail() : "-"));
            holder.tvNoHp.setText("No. HP: " + (userProspek.getNohp() != null ? userProspek.getNohp() : "-"));
            holder.tvAlamat.setText("Alamat: " + (userProspek.getAlamat() != null ? userProspek.getAlamat() : "-"));

            String formattedDP = "DP: Rp " + formatCurrency(userProspek.getDp());
            holder.tvJumlahUangTandaJadi.setText(formattedDP);
            holder.tvHunian.setText("Hunian: " + (userProspek.getHunian() != null ? userProspek.getHunian() : "-"));
            holder.tvTipeHunian.setText("Tipe Hunian: " + (userProspek.getTipeHunian() != null ? userProspek.getTipeHunian() : "-"));

            if (holder.btnEdit != null) {
                if (userLevel.equals("Admin")) {
                    holder.btnEdit.setVisibility(View.VISIBLE);
                    holder.btnEdit.setOnClickListener(v -> showEditDialog(userProspek));
                } else {
                    holder.btnEdit.setVisibility(View.GONE);
                }
            }
// PERBAIKAN: Tombol realisasi - bisa diakses semua user
            if (holder.btnRealisasi != null) {
                holder.btnRealisasi.setVisibility(View.VISIBLE);
                holder.btnRealisasi.setOnClickListener(v -> {
                    try {
                        Log.d(TAG, "🎯 Button Realisasi diklik untuk: " + userProspek.getNama());
                        showRealisasiDialog(userProspek);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error saat realisasi: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(LihatDataUserpActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
            if (holder.btnHistori != null) {
                holder.btnHistori.setVisibility(View.VISIBLE);
                holder.btnHistori.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(LihatDataUserpActivity.this, LihatDataHistoriUserProspek.class);
                        intent.putExtra("USER_PROSPEK_ID", userProspek.getId());
                        intent.putExtra("NAMA", userProspek.getNama());
                        intent.putExtra("PENGINPUT", userProspek.getPenginput());
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

            // PERBAIKAN UTAMA: Tombol cetak dengan handler yang diperbaiki
            if (holder.btnCetak != null) {
                holder.btnCetak.setVisibility(View.VISIBLE);
                holder.btnCetak.setOnClickListener(v -> {
                    try {
                        Log.d(TAG, "🎯 Button Cetak diklik untuk: " + userProspek.getNama());
                        handlePrintButton(userProspek);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error saat mencetak PDF: " + e.getMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(LihatDataUserpActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
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
            MaterialButton btnEdit, btnDelete, btnHistori, btnCetak,btnRealisasi;

            public ViewHolder(View itemView) {
                super(itemView);
                tvPenginput = itemView.findViewById(R.id.tvPenginput);
                tvTanggal = itemView.findViewById(R.id.tvTanggal);
                tvNama = itemView.findViewById(R.id.tvNama);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvNoHp = itemView.findViewById(R.id.tvNoHp);
                tvAlamat = itemView.findViewById(R.id.tvAlamat);
                tvHunian = itemView.findViewById(R.id.tvHunian);
                tvTipeHunian = itemView.findViewById(R.id.tvTipeHunian);
                tvJumlahUangTandaJadi = itemView.findViewById(R.id.tvJumlahUangTandaJadi);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnHistori = itemView.findViewById(R.id.btnHistori);
                btnCetak = itemView.findViewById(R.id.btnCetak);
                btnRealisasi = itemView.findViewById(R.id.btnRealisasi);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProspekData();
    }
}