package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LihatDataActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    MaterialCardView cardProspek, cardBooking, cardKavling, cardRealisasi;
    BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;

    // Variabel untuk menyimpan level user
    private String userLevel = "";
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_LEVEL = "level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Ambil level user dari SharedPreferences
        userLevel = sharedPreferences.getString(KEY_LEVEL, "");

        Log.d("LihatDataActivity", "=== DEBUG USER DATA ===");
        Log.d("LihatDataActivity", "User Level: " + userLevel);
        Log.d("LihatDataActivity", "All keys in SharedPreferences: " + sharedPreferences.getAll().toString());

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        cardProspek = findViewById(R.id.cardProspek);
        cardBooking = findViewById(R.id.cardBooking);

        cardKavling = findViewById(R.id.cardKavling);
        cardRealisasi = findViewById(R.id.cardRealisasi);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, NewBeranda.class);
            startActivity(intent);
            finish();
        });

        cardProspek.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataProspekActivity.class);
            startActivity(intent);
        });

        cardBooking.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataUserpActivity.class);
            startActivity(intent);
        });

        cardRealisasi.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataRealisasiActivity.class);
            startActivity(intent);
        });


        cardKavling.setOnClickListener(v -> {
            // Cek level user untuk akses lihat data kavling
            if (!"Admin".equals(userLevel)) {
                Toast.makeText(this, "Hanya Admin yang dapat mengakses Data Kavling", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(LihatDataActivity.this, LihatDataKavlingActivity.class);
            startActivity(intent);
        });



        // Setup visibility berdasarkan level user
        setupAccessBasedOnLevel();

        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupAccessBasedOnLevel() {
        Log.d("LihatDataActivity", "=== SETUP ACCESS FOR LEVEL: " + userLevel + " ===");

        // Jika user BUKAN Admin, sembunyikan menu proyek, hunian, dan kavling
        if (!"Admin".equals(userLevel)) {
            Log.d("LihatDataActivity", "Hiding admin features for level: " + userLevel);

            // Sembunyikan card kavling
            if (cardKavling != null) {
                cardKavling.setVisibility(View.GONE);
                Log.d("LihatDataActivity", "Hidden cardKavling");
            }

        } else {
            // Untuk Admin, tampilkan semua menu
            Log.d("LihatDataActivity", "Showing all features for Admin level: " + userLevel);


            // Tampilkan card kavling
            if (cardKavling != null) {
                cardKavling.setVisibility(View.VISIBLE);
                Log.d("LihatDataActivity", "Shown cardKavling");
            }
        }

        // PASTIKAN SEMUA CARD LAIN TETAP TAMPIL UNTUK SEMUA LEVEL
        if (cardProspek != null) {
            cardProspek.setVisibility(View.VISIBLE);
            Log.d("LihatDataActivity", "cardProspek remains visible for all levels");
        }

        if (cardBooking != null) {
            cardBooking.setVisibility(View.VISIBLE);
            Log.d("LihatDataActivity", "cardBooking remains visible for all levels");
        }
    }

    private void checkAccountExpiry() {
        String dateOutStr = sharedPreferences.getString("date_out", null);

        if (dateOutStr != null && !dateOutStr.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dateOut = dateFormat.parse(dateOutStr);
                Date today = new Date();

                if (today.after(dateOut)) {
                    Toast.makeText(this, "Akun telah expired. Silakan hubungi administrator.", Toast.LENGTH_LONG).show();
                    MainActivity.logout(this);
                }
            } catch (ParseException e) {
                Log.e("LihatDataActivity", "Error parsing date_out: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh user level setiap resume
        userLevel = sharedPreferences.getString(KEY_LEVEL, "");
        setupAccessBasedOnLevel();

        Log.d("LihatDataActivity", "onResume completed - Level: " + userLevel);
    }
}