package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DetailProyekHarmonyActivity extends AppCompatActivity {
    private static final String TAG = "DetailProyekHarmonyActivity"; // ✅ PERBAIKI TAG
    Button btnLihatUnit;
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    ImageView imgSitePlan;
    Button btnViewFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_proyek_harmony);

        initViews();
        setupClickListeners();
        setupNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        try {
            TopAppBar = findViewById(R.id.topAppBar);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            btnLihatUnit = findViewById(R.id.btnLihatUnit);
            imgSitePlan = findViewById(R.id.imgSitePlan);
            btnViewFull = findViewById(R.id.btnViewFull);

            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }

            Log.d(TAG, "Views initialized: " +
                    (TopAppBar != null) + " " +
                    (btnLihatUnit != null) + " " +
                    (imgSitePlan != null) + " " +
                    (btnViewFull != null));

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    private void setupClickListeners() {
        try {
            // Button Lihat Unit
            if (btnLihatUnit != null) {
                btnLihatUnit.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(DetailProyekHarmonyActivity.this, UnitHarmonyActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening UnitHarmonyActivity: " + e.getMessage());
                        Toast.makeText(this, "Cannot open unit page", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Button View Full Site Plan
            if (btnViewFull != null) {
                btnViewFull.setOnClickListener(v -> openFullScreenImage());
            }

            // Image Site Plan juga bisa diklik untuk fullscreen
            if (imgSitePlan != null) {
                imgSitePlan.setOnClickListener(v -> openFullScreenImage());
            }

            Log.d(TAG, "Click listeners setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void setupNavigation() {
        try {
            // TopAppBar navigation
            if (TopAppBar != null) {
                TopAppBar.setNavigationOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(DetailProyekHarmonyActivity.this, ProyekActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating back: " + e.getMessage());
                        finish();
                    }
                });
            }

            // Bottom Navigation
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    try {
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
                    } catch (Exception e) {
                        Log.e(TAG, "Error in bottom navigation: " + e.getMessage());
                    }
                    return false;
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage(), e);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Method untuk membuka gambar fullscreen
    private void openFullScreenImage() {
        try {
            Intent intent = new Intent(DetailProyekHarmonyActivity.this, SiteplanHarmonyActivity.class);
            // ✅ PERBAIKAN: Gunakan gambar Harmony, bukan Riverside
            intent.putExtra("IMAGE_RESOURCE", R.drawable.site_plan_harmony);
            startActivity(intent);

            // Optional: Tambahkan animasi
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            Log.d(TAG, "Full screen Harmony image opened");

        } catch (Exception e) {
            Log.e(TAG, "Error opening full screen image: " + e.getMessage(), e);
            Toast.makeText(this, "Cannot open image viewer", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}