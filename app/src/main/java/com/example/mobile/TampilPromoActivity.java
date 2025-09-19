package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TampilPromoActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private ImageView imgPreview;
    private Spinner spinnerPromo;
    private Button btnSimpan, btnBatal;
    private List<Promo> promoList;
    private String selectedImageBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tampil_promo);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(TampilPromoActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
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

        initViews();
        setupToolbar();
        loadPromoData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        imgPreview = findViewById(R.id.imgPreview);
        spinnerPromo = findViewById(R.id.spinnerRole);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        btnSimpan.setOnClickListener(v -> simpanPromoYangDitampilkan());
        btnBatal.setOnClickListener(v -> finish());
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadPromoData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        promoList = promoResponse.getData();
                        setupSpinner();
                    } else {
                        Toast.makeText(TampilPromoActivity.this, "Gagal memuat promo: " + promoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TampilPromoActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Toast.makeText(TampilPromoActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner() {
        List<String> promoNames = new ArrayList<>();
        for (Promo promo : promoList) {
            promoNames.add(promo.getNamaPromo());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                promoNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPromo.setAdapter(adapter);

        spinnerPromo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Promo selectedPromo = promoList.get(position);
                displayImage(selectedPromo.getGambarBase64());
                selectedImageBase64 = selectedPromo.getGambarBase64();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void displayImage(String imageBase64) {
        try {
            byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imgPreview.setImageBitmap(bitmap);

            // Tambahkan onClickListener untuk editing gambar
            imgPreview.setOnClickListener(v -> {
                // Untuk versi sederhana, kita bisa menggunakan library external untuk crop
                // Atau bisa diarahkan ke activity editing gambar
                Toast.makeText(TampilPromoActivity.this, "Fitur edit gambar akan datang", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Gagal menampilkan gambar", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void simpanPromoYangDitampilkan() {
        if (selectedImageBase64 != null && !selectedImageBase64.isEmpty()) {
            // Simpan ke SharedPreferences atau kirim ke BerandaActivity
            SharedPreferences preferences = getSharedPreferences("PromoPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("selected_promo_image", selectedImageBase64);
            editor.apply();

            Toast.makeText(this, "Promo berhasil ditampilkan di beranda", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Pilih promo terlebih dahulu", Toast.LENGTH_SHORT).show();
        }
    }
}