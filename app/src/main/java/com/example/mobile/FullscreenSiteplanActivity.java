package com.example.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FullscreenSiteplanActivity extends AppCompatActivity {

    private static final String TAG = "FullscreenSiteplanActivity";
    private ZoomableImageView imageView;
    private MaterialToolbar toolbar;
    private FloatingActionButton fabReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_siteplan);

        Log.d(TAG, "=== FULLSCREEN ACTIVITY CREATED ===");

        initViews();
        loadSiteplanImageFromCache();
        setupClickListeners();
    }

    private void initViews() {
        try {
            imageView = findViewById(R.id.zoomableImageView);
            toolbar = findViewById(R.id.toolbar);
            fabReset = findViewById(R.id.fabReset);

            if (imageView == null) {
                throw new RuntimeException("ZoomableImageView not found");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing viewer", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadSiteplanImageFromCache() {
        try {
            String cacheKey = getIntent().getStringExtra("CACHE_KEY");
            String proyekName = getIntent().getStringExtra("PROYEK_NAME");

            Log.d(TAG, "Loading from cache with key: " + cacheKey);

            if (cacheKey == null) {
                Toast.makeText(this, "Data siteplan tidak valid", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set toolbar title
            if (toolbar != null) {
                String title = "Site Plan";
                if (proyekName != null && !proyekName.isEmpty()) {
                    title = "Site Plan " + proyekName;
                }
                toolbar.setTitle(title);
            }

            // Load dari cache
            String siteplanBase64 = loadFromCache(cacheKey);

            if (siteplanBase64 == null || siteplanBase64.isEmpty()) {
                Toast.makeText(this, "Gagal memuat siteplan dari cache", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Decode base64 ke bitmap
            Log.d(TAG, "Decoding base64, length: " + siteplanBase64.length());
            byte[] decodedString = Base64.decode(siteplanBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if (decodedByte != null) {
                Log.d(TAG, "Bitmap created - Dimensions: " +
                        decodedByte.getWidth() + "x" + decodedByte.getHeight());

                imageView.setImageBitmap(decodedByte);

                // Reset zoom setelah image loaded
                imageView.postDelayed(() -> {
                    if (imageView != null) {
                        imageView.resetZoom();
                        Log.d(TAG, "Zoom reset completed");
                    }
                }, 500);

                // Bersihkan cache setelah digunakan
                clearCache(cacheKey);

            } else {
                throw new Exception("Failed to decode base64 image");
            }

        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory error: " + e.getMessage());
            Toast.makeText(this, "Gambar terlalu besar, tidak dapat dimuat", Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error loading siteplan: " + e.getMessage(), e);
            Toast.makeText(this, "Gagal memuat siteplan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String loadFromCache(String key) {
        try {
            SharedPreferences prefs = getSharedPreferences("siteplan_cache", MODE_PRIVATE);

            // Cek jika data di-split menjadi chunks
            int chunks = prefs.getInt(key + "_chunks", 0);
            if (chunks > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < chunks; i++) {
                    String chunk = prefs.getString(key + "_chunk_" + i, null);
                    if (chunk != null) {
                        sb.append(chunk);
                    }
                }
                return sb.toString();
            } else {
                return prefs.getString(key, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading from cache: " + e.getMessage());
            return null;
        }
    }

    private void clearCache(String key) {
        try {
            SharedPreferences prefs = getSharedPreferences("siteplan_cache", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            int chunks = prefs.getInt(key + "_chunks", 0);
            if (chunks > 0) {
                for (int i = 0; i < chunks; i++) {
                    editor.remove(key + "_chunk_" + i);
                }
                editor.remove(key + "_chunks");
            } else {
                editor.remove(key);
            }

            editor.apply();
            Log.d(TAG, "Cache cleared for key: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cache: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        // Toolbar back button
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Reset zoom button
        if (fabReset != null) {
            fabReset.setOnClickListener(v -> {
                if (imageView != null) {
                    imageView.resetZoom();
                    Toast.makeText(this, "Zoom direset", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}