package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnitProyekActivity extends AppCompatActivity {

    private static final String TAG = "UnitProyekActivity";

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout containerHunian;
    private TextView textEmpty;

    private ApiService apiService;
    private String namaProyek;
    private String userLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_proyek);

        Log.d(TAG, "Activity onCreate started");

        // Ambil nama proyek dari intent
        namaProyek = getIntent().getStringExtra("NAMA_PROYEK");
        if (namaProyek == null) {
            Toast.makeText(this, "Data proyek tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Nama Proyek: " + namaProyek);

        // Ambil user level dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        userLevel = sharedPreferences.getString("level", "Operator");
        Log.d(TAG, "User Level: " + userLevel);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        initViews();
        setupNavigation();

        // Load data hunian
        loadHunianData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        containerHunian = findViewById(R.id.containerHunian);
        textEmpty = findViewById(R.id.textEmpty);

        // Set judul toolbar
        topAppBar.setTitle("Unit " + namaProyek);
    }

    private void setupNavigation() {
        // Navigasi toolbar
        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
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
    }

    private void loadHunianData() {
        Log.d(TAG, "Loading data hunian untuk proyek: " + namaProyek);

        Call<HunianDetailResponse> call = apiService.getHunianDataByProyek("getHunianByProyek", namaProyek);

        call.enqueue(new Callback<HunianDetailResponse>() {
            @Override
            public void onResponse(Call<HunianDetailResponse> call, Response<HunianDetailResponse> response) {
                Log.d(TAG, "Response received - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    HunianDetailResponse hunianResponse = response.body();
                    Log.d(TAG, "Response success: " + hunianResponse.isSuccess());
                    Log.d(TAG, "Response message: " + hunianResponse.getMessage());

                    if (hunianResponse.isSuccess() && hunianResponse.getData() != null) {
                        // Debug: Tampilkan data yang diterima
                        for (Hunian hunian : hunianResponse.getData()) {
                            Log.d(TAG, "Hunian: " + hunian.getNamaHunian() +
                                    ", Luas Tanah: " + hunian.getLuasTanah() +
                                    ", Luas Bangunan: " + hunian.getLuasBangunan() +
                                    ", Gambar Unit: " + (hunian.getGambarUnit() != null ? "ada" : "tidak ada"));
                        }

                        displayHunianData(hunianResponse.getData());
                        Log.d(TAG, "Menampilkan data hunian: " + hunianResponse.getData().size() + " items");
                    } else {
                        showEmptyState();
                        Toast.makeText(UnitProyekActivity.this,
                                "Data hunian kosong: " + hunianResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showEmptyState();
                    String errorMsg = "Response tidak sukses - Code: " + response.code();
                    Toast.makeText(UnitProyekActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMsg);

                    // DEBUG: Tampilkan error body untuk debugging
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<HunianDetailResponse> call, Throwable t) {
                showEmptyState();
                String errorMsg = "Gagal memuat data hunian: " + t.getMessage();
                Toast.makeText(UnitProyekActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Load hunian error: " + t.getMessage(), t);
            }
        });
    }

    private void displayHunianData(List<Hunian> hunianList) {
        containerHunian.removeAllViews();

        if (hunianList == null || hunianList.isEmpty()) {
            showEmptyState();
            Log.d(TAG, "Data hunian kosong atau null");
            return;
        }

        hideEmptyState();

        for (Hunian hunian : hunianList) {
            View cardView = createHunianCard(hunian);
            containerHunian.addView(cardView);
        }

        Log.d(TAG, "Menampilkan " + hunianList.size() + " hunian");
    }

    private View createHunianCard(Hunian hunian) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_hunian_card, containerHunian, false);

        // Set data ke view
        TextView textNamaHunian = cardView.findViewById(R.id.textNamaHunian);
        TextView textNamaProyek = cardView.findViewById(R.id.textNamaProyek);
        TextView textLuasTanah = cardView.findViewById(R.id.textLuasTanah);
        TextView textLuasBangunan = cardView.findViewById(R.id.textLuasBangunan);
        ImageView imageUnit = cardView.findViewById(R.id.imageUnit);
        Button btnDetail = cardView.findViewById(R.id.btnDetail);
        Button btnDelete = cardView.findViewById(R.id.btnDelete);

        // Set basic data
        textNamaHunian.setText(hunian.getNamaHunian());
        textNamaProyek.setText(hunian.getNamaProyek());

        // Gunakan data baru jika ada, jika tidak gunakan default
        if (hunian.getLuasTanah() > 0) {
            textLuasTanah.setText(hunian.getLuasTanah() + " m²");
        } else {
            textLuasTanah.setText("0 m²");
        }

        if (hunian.getLuasBangunan() > 0) {
            textLuasBangunan.setText(hunian.getLuasBangunan() + " m²");
        } else {
            textLuasBangunan.setText("0 m²");
        }

        // Set gambar unit jika ada
        if (hunian.getGambarUnit() != null && !hunian.getGambarUnit().isEmpty()) {
            setImageFromBase64(hunian.getGambarUnit(), imageUnit);
        } else {
            imageUnit.setImageResource(R.drawable.ic_placeholder);
        }

        // Hitung dan tampilkan fasilitas jika ada
        if (hunian.getFasilitas() != null) {
            processFasilitas(hunian, cardView);
        }

        // Button detail
        btnDetail.setOnClickListener(v -> {
            // Buka DetailUnitProyekActivity
            Intent intent = new Intent(UnitProyekActivity.this, DetailUnitProyekActivity.class);
            intent.putExtra("HUNIAN", hunian);
            startActivity(intent);
        });;

        // Button delete - hanya untuk admin (case insensitive)
        if (userLevel != null && "Admin".equalsIgnoreCase(userLevel)) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                showDeleteConfirmation(hunian);
            });
            Log.d(TAG, "Tombol delete ditampilkan untuk admin");
        } else {
            btnDelete.setVisibility(View.GONE);
            Log.d(TAG, "Tombol delete disembunyikan untuk level: " + userLevel);
        }

        return cardView;
    }

    private void processFasilitas(Hunian hunian, View cardView) {
        if (hunian.getFasilitas() == null || hunian.getFasilitas().isEmpty()) {
            return;
        }

        int kamarTidur = 0;
        int kamarMandi = 0;
        int fasilitasLain = 0;

        for (FasilitasHunianItem fasilitas : hunian.getFasilitas()) {
            String namaFasilitas = fasilitas.getNamaFasilitas().toLowerCase();

            if (namaFasilitas.contains("kamar tidur") || namaFasilitas.contains("ruang tidur")) {
                kamarTidur += fasilitas.getJumlah();
            } else if (namaFasilitas.contains("kamar mandi") || namaFasilitas.contains("wc") || namaFasilitas.contains("toilet")) {
                kamarMandi += fasilitas.getJumlah();
            } else {
                fasilitasLain += fasilitas.getJumlah();
            }
        }

        // Tampilkan fasilitas yang ada
        if (kamarTidur > 0) {
            cardView.findViewById(R.id.layoutKamarTidur).setVisibility(View.VISIBLE);
            TextView textKamarTidur = cardView.findViewById(R.id.textKamarTidur);
            textKamarTidur.setText(String.valueOf(kamarTidur));
        }

        if (kamarMandi > 0) {
            cardView.findViewById(R.id.layoutKamarMandi).setVisibility(View.VISIBLE);
            TextView textKamarMandi = cardView.findViewById(R.id.textKamarMandi);
            textKamarMandi.setText(String.valueOf(kamarMandi));
        }

        if (fasilitasLain > 0) {
            cardView.findViewById(R.id.layoutFasilitasLain).setVisibility(View.VISIBLE);
            TextView textFasilitasLain = cardView.findViewById(R.id.textFasilitasLain);
            textFasilitasLain.setText(String.valueOf(fasilitasLain));
        }
    }

    private void setImageFromBase64(String base64String, ImageView imageView) {
        try {
            String cleanBase64 = base64String.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            cleanBase64 = cleanBase64.replaceAll("\\s", "");

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decoding image: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private void showDetailHunian(Hunian hunian) {
        // Buat string deskripsi
        StringBuilder description = new StringBuilder();
        description.append("Nama Hunian: ").append(hunian.getNamaHunian()).append("\n");
        description.append("Proyek: ").append(hunian.getNamaProyek()).append("\n");
        description.append("Luas Tanah: ").append(hunian.getLuasTanah()).append(" m²\n");
        description.append("Luas Bangunan: ").append(hunian.getLuasBangunan()).append(" m²\n");

        if (hunian.getDeskripsiHunian() != null && !hunian.getDeskripsiHunian().isEmpty()) {
            description.append("Deskripsi: ").append(hunian.getDeskripsiHunian()).append("\n");
        } else {
            description.append("Deskripsi: -\n");
        }

        // Tambahkan fasilitas jika ada
        if (hunian.getFasilitas() != null && !hunian.getFasilitas().isEmpty()) {
            description.append("\nFasilitas:\n");
            for (FasilitasHunianItem fasilitas : hunian.getFasilitas()) {
                description.append("• ").append(fasilitas.getNamaFasilitas())
                        .append(": ").append(fasilitas.getJumlah()).append("\n");
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detail " + hunian.getNamaHunian())
                .setMessage(description.toString())
                .setPositiveButton("Tutup", null)
                .show();
    }

    private void showDeleteConfirmation(Hunian hunian) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Hunian")
                .setMessage("Apakah Anda yakin ingin menghapus hunian \"" + hunian.getNamaHunian() + "\"?\n\n" +
                        "Penghapusan akan:\n" +
                        "• Mengecek apakah ada data prospek terkait\n" +
                        "• Menghapus data kavling terkait\n" +
                        "• Menghapus fasilitas hunian\n" +
                        "• Menghapus hunian")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deleteHunian(hunian);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteHunian(Hunian hunian) {

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String deletedBy = sharedPreferences.getString("nama_lengkap", username);

        Log.d(TAG, "Memulai proses penghapusan hunian: " + hunian.getNamaHunian() + " (ID: " + hunian.getIdHunian() + ")");

        Call<BasicResponse> call = apiService.deleteHunian(
                "deleteHunian",
                hunian.getIdHunian(),
                username,
                deletedBy  // Tambahkan parameter ini
        );
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse deleteResponse = response.body();
                    if (deleteResponse.isSuccess()) {
                        Toast.makeText(UnitProyekActivity.this,
                                deleteResponse.getMessage() != null ? deleteResponse.getMessage() : "Hunian berhasil dihapus",
                                Toast.LENGTH_LONG).show();
                        // Reload data
                        loadHunianData();
                    } else {
                        String errorMessage = deleteResponse.getMessage() != null ?
                                deleteResponse.getMessage() : "Gagal menghapus hunian";
                        Toast.makeText(UnitProyekActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Gagal menghapus hunian: " + errorMessage);
                    }
                } else {
                    String errorMsg = "Error response: " + response.code();
                    Toast.makeText(UnitProyekActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMsg);

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                String errorMsg = "Error: " + t.getMessage();
                Toast.makeText(UnitProyekActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage(), t);
            }
        });
    }

    private void showEmptyState() {
        textEmpty.setVisibility(View.VISIBLE);
        containerHunian.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        textEmpty.setVisibility(View.GONE);
        containerHunian.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}