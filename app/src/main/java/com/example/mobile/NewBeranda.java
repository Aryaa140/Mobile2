package com.example.mobile;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewBeranda extends AppCompatActivity implements PromoAdapter.OnPromoActionListener {
    MaterialCardView cardWelcome, cardProspekM, cardFasilitasM, cardProyekM, cardUserpM, cardInputPromoM;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;
    TextView tvUserName;
    private RecyclerView recyclerPromo;
    private PromoAdapter promoAdapter;
    private List<Promo> promoList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";


    private SharedPreferences newsPrefs;
    private static final String NEWS_PREFS_NAME = "NewsUpdates";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_beranda);

        // PERBAIKAN: Request notification permission untuk Android 13+
        requestNotificationPermission();

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        newsPrefs = getSharedPreferences(NEWS_PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupRecyclerView();
        loadPromoData();
        setupUserInfo();
        setupClickListeners();
        setupNavigation();


        // TEST: Coba tampilkan test notification setelah delay
        //testNotification();//

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Method untuk request notification permission
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101);
                Log.d("NewBeranda", "Requesting notification permission");
            } else {
                Log.d("NewBeranda", "Notification permission already granted");
            }
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NewBeranda", "Notification permission granted");
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("NewBeranda", "Notification permission denied");
                Toast.makeText(this, "Notification permission denied - some features may not work", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Method untuk test notification
    private void testNotification() {
        // Tunggu 3 detik lalu tampilkan test notification
        new Handler().postDelayed(() -> {
            NotificationUtils.testNotification(this);
            Log.d("NewBeranda", "Test notification triggered");
        }, 3000);
    }

    private void initViews() {
        cardWelcome = findViewById(R.id.cardWelcome);
        cardProspekM = findViewById(R.id.cardProspekM);
        cardFasilitasM = findViewById(R.id.cardFasilitasM);
        cardProyekM = findViewById(R.id.cardProyekM);
        cardUserpM = findViewById(R.id.cardUserpM);
        cardInputPromoM = findViewById(R.id.cardInputPromoM);
        tvUserName = findViewById(R.id.tvUserName);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);
        recyclerPromo = findViewById(R.id.recyclerPromo);
    }

    private void setupRecyclerView() {
        recyclerPromo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        promoAdapter = new PromoAdapter(this, promoList);
        promoAdapter.setOnPromoActionListener(this);
        recyclerPromo.setAdapter(promoAdapter);
    }

    private void loadPromoData() {
        Log.d("BerandaActivity", "Loading promo data...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();
        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        promoList.clear();
                        promoList.addAll(promoResponse.getData());
                        promoAdapter.notifyDataSetChanged();
                        Log.d("BerandaActivity", "Promo data loaded: " + promoList.size() + " items");

                        // if (promoList.isEmpty()) {
                        //     NotificationUtils.showInfoNotification(NewBeranda.this, "Info", "Tidak ada data promo");
                        // }
                    } else {
                        NotificationUtils.showErrorNotification(NewBeranda.this, "Gagal memuat promo: " + promoResponse.getMessage());
                    }
                } else {
                    NotificationUtils.showErrorNotification(NewBeranda.this, "Error response server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                NotificationUtils.showErrorNotification(NewBeranda.this, "Gagal memuat promo: " + t.getMessage());
                Log.e("BerandaActivity", "Load promo error: " + t.getMessage());
            }
        });
    }

    // IMPLEMENTASI METHOD DARI INTERFACE - YANG INI SUDAH ADA
    private String getCurrentUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "User");
    }

    // IMPLEMENTASI METHOD DARI INTERFACE - UPDATE PROMO (DIPERBAIKI)
    @Override
    public void onPromoUpdated(int promoId, String updatedImage) {
        Log.d("BerandaActivity", "Promo updated - ID: " + promoId);

        // Update item di adapter
        if (promoAdapter != null) {
            promoAdapter.updatePromoItem(promoId, updatedImage);
        }

        // CARI JUDUL PROMO DAN USER UNTUK NOTIFIKASI
        String promoTitle = findPromoTitleById(promoId);
        String currentUser = getCurrentUsername(); // DAPATKAN USER YANG SEDANG LOGIN

        // SIMPAN GAMBAR KE CACHE JIKA ADA PERUBAHAN
        if (updatedImage != null && !updatedImage.isEmpty() && promoTitle != null) {
            saveImageToCache(promoTitle, updatedImage);
        }

        // SIMPAN INFO UPDATE UNTUK NEWS ACTIVITY
        savePromoUpdateForNews(promoId, "Diubah", updatedImage, promoTitle);

        // TAMPILKAN NOTIFIKASI SISTEM DENGAN USER INFO - INI YANG DIPERBAIKI
        if (promoTitle != null) {
            NotificationUtils.showPromoUpdatedNotification(this, promoTitle, currentUser);
        }
    }

    // IMPLEMENTASI METHOD DARI INTERFACE - DELETE PROMO (DIPERBAIKI)
    @Override
    public void onPromoDeleted(String promoTitle, String penginput) {
        Log.d("BerandaActivity", "Promo deleted: " + promoTitle);

        // SIMPAN INFO DELETE UNTUK NEWS ACTIVITY
        savePromoDeleteForNews(promoTitle, penginput);

        // TAMPILKAN NOTIFIKASI SISTEM DENGAN USER INFO - INI YANG DIPERBAIKI
        // Gunakan penginput dari parameter (user yang menghapus)
        NotificationUtils.showPromoDeletedNotification(this, promoTitle, penginput);
    }

    // PERBAIKI handleEditPromoResult UNTUK MENERIMA DATA USER
    private void handleEditPromoResult(Intent data) {
        int updatedPromoId = data.getIntExtra("UPDATED_PROMO_ID", -1);
        String updatedImage = data.getStringExtra("UPDATED_IMAGE");
        String updatedTitle = data.getStringExtra("UPDATED_TITLE");
        String updatedUser = data.getStringExtra("UPDATED_USER"); // TAMBAHKAN INI
        boolean isSuccess = data.getBooleanExtra("IS_SUCCESS", false);
        String errorMessage = data.getStringExtra("ERROR_MESSAGE");

        Log.d("BerandaActivity", "Handle edit result - ID: " + updatedPromoId + ", Success: " + isSuccess);

        if (isSuccess && updatedPromoId != -1) {
            // Panggil method update melalui interface
            onPromoUpdated(updatedPromoId, updatedImage);

            // JIKA PERLU, TAMPILKAN NOTIFIKASI LAGI DARI SINI DENGAN USER INFO
            if (updatedTitle != null) {
                String currentUser = updatedUser != null ? updatedUser : getCurrentUsername();
                //NotificationUtils.showPromoUpdatedNotification(this, updatedTitle, currentUser);//
            }

            // Refresh data dari server untuk memastikan konsistensi
            new Handler().postDelayed(() -> loadPromoData(), 1000);
        } else {
            NotificationUtils.showErrorNotification(this, "Gagal update promo: " + errorMessage);
            Log.w("BerandaActivity", "Invalid update data, refreshing from server");
            loadPromoData();
        }
    }


    // METHOD BARU: CARI JUDUL PROMO BERDASARKAN ID
    private String findPromoTitleById(int promoId) {
        for (Promo promo : promoList) {
            if (promo.getIdPromo() == promoId) {
                return promo.getNamaPromo();
            }
        }
        return null;
    }

    // METHOD UNTUK SIMPAN INFO UPDATE PROMO (SUDAH DIPERBAIKI)
    private void savePromoUpdateForNews(int promoId, String status, String updatedImage, String promoTitle) {
        long updateTime = System.currentTimeMillis() + 2000; // 2 detik delay

        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putInt("last_updated_promo_id", promoId);
        editor.putString("last_updated_status", status);
        editor.putString("last_updated_title", promoTitle != null ? promoTitle : "");
        editor.putString("last_updated_image", updatedImage != null ? updatedImage : "");
        editor.putLong("last_update_time", updateTime);
        editor.apply();

        Log.d("NewBeranda", "Saved update info for: " + promoTitle + " at time: " + updateTime);
    }

    // METHOD UNTUK SIMPAN INFO DELETE PROMO (SUDAH DIPERBAIKI)
    private void savePromoDeleteForNews(String promoTitle, String penginput) {
        long deleteTime = System.currentTimeMillis() + 1000; // 1 detik delay

        String lastImage = getCachedImageForPromo(promoTitle);

        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putString("last_deleted_title", promoTitle);
        editor.putString("last_deleted_inputter", penginput);
        editor.putString("last_deleted_status", "Dihapus");
        editor.putString("last_deleted_image", lastImage != null ? lastImage : "");
        editor.putLong("last_delete_time", deleteTime);
        editor.apply();

        Log.d("NewBeranda", "Saved delete info for: " + promoTitle + " at time: " + deleteTime);
    }

    private String findLastImageForPromo(String promoTitle) {
        // CARI ALTERNATIF 1: Dari promoList yang sudah di-load
        for (Promo promo : promoList) {
            if (promo.getNamaPromo() != null && promo.getNamaPromo().equals(promoTitle)) {
                Log.d("NewBeranda", "Found image from promoList: " + promoTitle);
                return promo.getGambarBase64();
            }
        }

        // CARI ALTERNATIF 2: Dari SharedPreferences atau cache
        String cachedImage = getCachedImageForPromo(promoTitle);
        if (cachedImage != null) {
            return cachedImage;
        }

        Log.d("NewBeranda", "No image found for: " + promoTitle);
        return null;
    }

    // METHOD BARU: CARI GAMBAR DARI CACHE SEDERHANA
    // Di NewBeranda.java - METHOD BARU: SIMPAN GAMBAR KE CACHE DENGAN TIMESTAMP
    private void saveImageToCache(String promoTitle, String imageBase64) {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return;
        }

        SharedPreferences imageCache = getSharedPreferences("ImageCache", MODE_PRIVATE);
        String key = "last_image_" + promoTitle + "_" + System.currentTimeMillis();

        // Simpan dengan timestamp dan batasi jumlah cache
        imageCache.edit().putString(key, imageBase64).apply();

        // Bersihkan cache lama (lebih dari 10 item)
        cleanOldImageCache();
    }

    private void cleanOldImageCache() {
        SharedPreferences imageCache = getSharedPreferences("ImageCache", MODE_PRIVATE);
        Map<String, ?> allEntries = imageCache.getAll();

        if (allEntries.size() > 10) {
            SharedPreferences.Editor editor = imageCache.edit();
            int count = 0;
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (count >= 5) { // Hapus 5 yang paling lama
                    break;
                }
                editor.remove(entry.getKey());
                count++;
            }
            editor.apply();
        }
    }

    // METHOD BARU: CARI GAMBAR TERBARU DARI CACHE
    private String getCachedImageForPromo(String promoTitle) {
        SharedPreferences imageCache = getSharedPreferences("ImageCache", MODE_PRIVATE);
        Map<String, ?> allEntries = imageCache.getAll();

        String latestImage = null;
        long latestTime = 0;

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("last_image_" + promoTitle + "_")) {
                // Extract timestamp dari key
                try {
                    long time = Long.parseLong(key.split("_")[3]);
                    if (time > latestTime) {
                        latestTime = time;
                        latestImage = (String) entry.getValue();
                    }
                } catch (Exception e) {
                    Log.e("NewBeranda", "Error parsing cache key: " + key);
                }
            }
        }

        return latestImage;
    }

    // HANDLE ACTIVITY RESULT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("BerandaActivity", "onActivityResult - Request: " + requestCode + ", Result: " + resultCode);

        if (requestCode == PromoAdapter.EDIT_PROMO_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                handleEditPromoResult(data);
            } else if (resultCode == RESULT_CANCELED) {
                NotificationUtils.showInfoNotification(this, "Dibatalkan", "Edit promo dibatalkan");
            }
        }
    }


    private void setupUserInfo() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        if (!username.isEmpty()) {
            tvUserName.setText(username);
        } else {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                tvUserName.setText(username);
            }
        }
    }


    private void setupClickListeners() {
        cardWelcome.setOnClickListener(v -> {
            Intent profileIntent = new Intent(NewBeranda.this, ProfileActivity.class);
            startActivity(profileIntent);
        });
        cardProspekM.setOnClickListener(v -> {
            Intent intentProspek = new Intent(NewBeranda.this, TambahProspekActivity.class);
            startActivity(intentProspek);
        });
        cardFasilitasM.setOnClickListener(v -> {
            Intent intentFasilitas = new Intent(NewBeranda.this, FasilitasActivity.class);
            startActivity(intentFasilitas);
        });
        cardProyekM.setOnClickListener(v -> {
            Intent intentProyek = new Intent(NewBeranda.this, ProyekActivity.class);
            startActivity(intentProyek);
        });
        cardUserpM.setOnClickListener(v -> {
            Intent intentUserp = new Intent(NewBeranda.this, TambahUserpActivity.class);
            startActivity(intentUserp);
        });
        cardInputPromoM.setOnClickListener(v -> {
            Intent intent = new Intent(NewBeranda.this, InputPromoActivity.class);
            startActivity(intent);
        });
    }

    private void setupNavigation() {
        topAppBar.setNavigationOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_exit) {
                logout();
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove("username");
        editor.remove("division");
        editor.remove("nip");
        editor.apply();

        NotificationUtils.showInfoNotification(this, "Logout", "Logout berhasil");
        Intent intent = new Intent(NewBeranda.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromoData();
    }
}