package com.example.mobile;

import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SiteplanHarmonyActivity extends AppCompatActivity {
    private static final String TAG = "SiteplanHarmonyActivity";
    private ZoomableImageView imageView;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate started");

        try {
            // Set status bar color sebelum setContentView
            setStatusBarColor();

            // ✅ PERBAIKAN: Gunakan layout yang benar - activity_siteplan_harmony
            setContentView(R.layout.activity_siteplan_harmony);
            Log.d(TAG, "Layout inflated successfully");

            // Enable EdgeToEdge SETELAH setContentView
            EdgeToEdge.enable(this);

            initViews();
            setupImage();
            setupClickListeners();

            Log.d(TAG, "Activity setup completed");

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error membuka gambar", Toast.LENGTH_SHORT).show();
            finish();
        }

        // WindowInsets setelah semua view diinisialisasi
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setStatusBarColor() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();

                // Clear any existing flags
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                // Set status bar color
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

                // For light status bar icons
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    );
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting status bar color: " + e.getMessage());
        }
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");

        try {
            imageView = findViewById(R.id.imageView);
            toolbar = findViewById(R.id.toolbar);
            fabReset = findViewById(R.id.fabReset);

            if (imageView == null) {
                throw new RuntimeException("ZoomableImageView is null");
            }

            // ✅ PERBAIKAN: Set title yang benar untuk Harmony
            if (toolbar != null) {
                toolbar.setTitle("Site Plan Harmony");
            }

            Log.d(TAG, "All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupImage() {
        try {
            // Get image resource from intent
            int imageResource = getIntent().getIntExtra("IMAGE_RESOURCE", R.drawable.site_plan_harmony);
            Log.d(TAG, "Setting image resource: " + imageResource);

            // Set image
            imageView.setImageResource(imageResource);

            // Beri waktu untuk layout selesai
            imageView.postDelayed(() -> {
                if (imageView != null) {
                    imageView.resetZoom();
                    Log.d(TAG, "Image reset completed");
                }
            }, 300);

            Log.d(TAG, "Image setup completed");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up image: " + e.getMessage(), e);
            Toast.makeText(this, "Error memuat gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            // Toolbar navigation
            toolbar.setNavigationOnClickListener(v -> {
                Log.d(TAG, "Back pressed");
                onBackPressed();
            });

            // Reset button
            fabReset.setOnClickListener(v -> {
                Log.d(TAG, "Reset pressed");
                if (imageView != null) {
                    imageView.resetZoom();
                }
            });

            Log.d(TAG, "Click listeners setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}