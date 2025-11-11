package com.example.mobile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewBeranda extends AppCompatActivity implements PromoAdapter.OnPromoActionListener {
    MaterialCardView cardWelcome, cardProspekM, cardFasilitasM, cardProyekM, cardUserpM, cardInputPromoM;
    MaterialCardView cardInputNIP, cardStatusAkun, cardInputProyek, cardInputHunian, cardInputKavling;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;
    TextView tvUserName, tvMenuData, tvMenu2, tvMenu, tvPromo, tvMenuData2;
    ImageView icbookakun, icbookproyek;
    private RecyclerView recyclerPromo;
    private PromoAdapter promoAdapter;
    private List<Promo> promoList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_DIVISION = "division";

    // SHARED PREFERENCES UNTUK NEWS
    private SharedPreferences newsPrefs;
    private static final String NEWS_PREFS_NAME = "NewsUpdates";
    private static final String TAG = "NewBeranda";
    // Variabel untuk menyimpan level user
    private String userLevel = "";
    private Handler autoDeleteHandler;
    private Runnable autoDeleteRunnable;
    private static final long AUTO_DELETE_INTERVAL = 5 * 60 * 1000; // 5 menit
    private static final long INITIAL_DELAY = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_beranda);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        newsPrefs = getSharedPreferences(NEWS_PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupUserInfo();
        setupRecyclerView();
        loadPromoData();
        setupClickListeners();
        setupNavigation();
        setupAccessBasedOnLevel();
        checkAndRequestPermissions();
        startAutoDeleteBackgroundService();

        checkAccountExpiry();
        checkAccountExpiryRealTime();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void checkAccountExpiryRealTime() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");

        if (username.isEmpty()) {
            Log.d("RealTimeCheck", "Username tidak ditemukan, skip real-time check");
            checkAccountExpiry(); // Fallback ke local check
            return;
        }

        Log.d("RealTimeCheck", "Checking real-time expiry for: " + username);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<DateOutResponse> call = apiService.checkDateOut(username);

        call.enqueue(new Callback<DateOutResponse>() {
            @Override
            public void onResponse(Call<DateOutResponse> call, Response<DateOutResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DateOutResponse dateOutResponse = response.body();

                    if (dateOutResponse.isSuccess()) {
                        String currentDateOut = dateOutResponse.getDate_out();
                        boolean isExpired = dateOutResponse.isIs_expired();

                        Log.d("RealTimeCheck", "Server Date Out: " + currentDateOut);
                        Log.d("RealTimeCheck", "Is Expired: " + isExpired);

                        // Update SharedPreferences dengan data terbaru
                        if (currentDateOut != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("date_out", currentDateOut);
                            editor.apply();
                            Log.d("RealTimeCheck", "Updated SharedPreferences date_out: " + currentDateOut);
                        }

                        // Jika expired, logout
                        if (isExpired) {
                            Log.d("RealTimeCheck", "*** ACCOUNT EXPIRED - LOGGING OUT ***");
                            Toast.makeText(NewBeranda.this,
                                    "Akun telah expired. Silakan hubungi administrator.",
                                    Toast.LENGTH_LONG).show();
                            MainActivity.logout(NewBeranda.this);
                        } else {
                            Log.d("RealTimeCheck", "Account still valid");
                            // Fallback ke local check untuk memastikan
                            checkAccountExpiry();
                        }

                    } else {
                        Log.e("RealTimeCheck", "Server response not successful: " + dateOutResponse.getMessage());
                        checkAccountExpiry(); // Fallback ke local check
                    }
                } else {
                    Log.e("RealTimeCheck", "Network error in real-time check");
                    checkAccountExpiry(); // Fallback ke local check
                }
            }

            @Override
            public void onFailure(Call<DateOutResponse> call, Throwable t) {
                Log.e("RealTimeCheck", "Real-time check failed: " + t.getMessage());
                checkAccountExpiry(); // Fallback ke local check
            }
        });
    }

    private void checkAccountExpiry() {
        String dateOutStr = sharedPreferences.getString("date_out", null);

        Log.d("AccountExpiry", "=== DEBUG ACCOUNT EXPIRY ===");
        Log.d("AccountExpiry", "Date_out from SharedPreferences: " + dateOutStr);

        if (dateOutStr != null && !dateOutStr.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dateOut = dateFormat.parse(dateOutStr);
                Date today = new Date();

                // Format today untuk debug
                String todayStr = dateFormat.format(today);
                Log.d("AccountExpiry", "Today (formatted): " + todayStr);
                Log.d("AccountExpiry", "Date Out: " + dateOutStr);

                // Reset waktu untuk perbandingan yang akurat
                Calendar calToday = Calendar.getInstance();
                calToday.setTime(today);
                calToday.set(Calendar.HOUR_OF_DAY, 0);
                calToday.set(Calendar.MINUTE, 0);
                calToday.set(Calendar.SECOND, 0);
                calToday.set(Calendar.MILLISECOND, 0);
                today = calToday.getTime();

                Calendar calDateOut = Calendar.getInstance();
                calDateOut.setTime(dateOut);
                calDateOut.set(Calendar.HOUR_OF_DAY, 0);
                calDateOut.set(Calendar.MINUTE, 0);
                calDateOut.set(Calendar.SECOND, 0);
                calDateOut.set(Calendar.MILLISECOND, 0);
                dateOut = calDateOut.getTime();

                Log.d("AccountExpiry", "Today (millis): " + today.getTime());
                Log.d("AccountExpiry", "DateOut (millis): " + dateOut.getTime());
                Log.d("AccountExpiry", "Is expired: " + (today.getTime() >= dateOut.getTime()));

                if (today.getTime() >= dateOut.getTime()) {
                    Log.d("AccountExpiry", "*** ACCOUNT EXPIRED - LOGGING OUT ***");
                    Toast.makeText(this, "Akun telah expired sejak " + dateOutStr + ". Silakan hubungi administrator.", Toast.LENGTH_LONG).show();
                    MainActivity.logout(this);
                } else {
                    Log.d("AccountExpiry", "Account still valid");
                }

            } catch (ParseException e) {
                Log.e("AccountExpiry", "Error parsing date_out: " + e.getMessage());
                Log.e("AccountExpiry", "Date string that failed: " + dateOutStr);
            }
        } else {
            Log.d("AccountExpiry", "No date_out found or empty");
        }
    }

    private void initViews() {
        cardWelcome = findViewById(R.id.cardWelcome);
        cardProspekM = findViewById(R.id.cardProspekM);
        cardFasilitasM = findViewById(R.id.cardFasilitasM);
        cardProyekM = findViewById(R.id.cardProyekM);
        cardUserpM = findViewById(R.id.cardUserpM);
        cardInputPromoM = findViewById(R.id.cardInputPromoM);

        // INISIALISASI ELEMEN BARU
        cardInputNIP = findViewById(R.id.cardInputNIP);
        cardStatusAkun = findViewById(R.id.cardStatusAkun);
        cardInputProyek = findViewById(R.id.cardInputProyek);
        cardInputHunian = findViewById(R.id.cardInputHunian);
        cardInputKavling = findViewById(R.id.cardInputKavling);

        tvMenu2 = findViewById(R.id.tvMenu2);
        tvMenu = findViewById(R.id.tvMenu);
        tvPromo = findViewById(R.id.tvPromo);
        tvMenuData = findViewById(R.id.tvMenuData);
        tvMenuData2 = findViewById(R.id.tvMenuData2);

        icbookakun = findViewById(R.id.icbookakun);
        icbookproyek = findViewById(R.id.icbookproyek);

        tvUserName = findViewById(R.id.tvUserName);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);
        recyclerPromo = findViewById(R.id.recyclerPromo);

        Log.d("BerandaActivity", "Init views completed");
        Log.d("BerandaActivity", "cardInputHunian: " + (cardInputHunian != null));
        Log.d("BerandaActivity", "userLevel: " + userLevel);
    }

    private void setupUserInfo() {
        // Ambil data user dari SharedPreferences
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        userLevel = sharedPreferences.getString(KEY_LEVEL, "Admin"); // Default ke Admin untuk testing

        // DEBUG: Tampilkan semua data yang tersimpan
        Log.d("BerandaActivity", "=== DEBUG USER DATA ===");
        Log.d("BerandaActivity", "Username: " + username);
        Log.d("BerandaActivity", "Level: " + userLevel);
        Log.d("BerandaActivity", "All keys in SharedPreferences: " + sharedPreferences.getAll().toString());

        if (!username.isEmpty()) {
            tvUserName.setText(username);
        } else {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                tvUserName.setText(username);
            }
        }

        Log.d("BerandaActivity", "Final User Level: " + userLevel);
    }

    private void setupAccessBasedOnLevel() {
        Log.d("BerandaActivity", "=== SETUP ACCESS FOR LEVEL: " + userLevel + " ===");

        // Jika user adalah Operator, Operator Inhouse, atau Operator Freelance, sembunyikan menu tertentu
        if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
            Log.d("BerandaActivity", "Hiding admin features for Operator level: " + userLevel);

            // Sembunyikan button tambah promo
            if (cardInputPromoM != null) {
                cardInputPromoM.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardInputPromoM");
            }

            // Sembunyikan menu pengelolaan akun
            if (tvMenu2 != null) {
                tvMenu2.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden tvMenu2");
            }

            // Sembunyikan card input NIP
            if (cardInputNIP != null) {
                cardInputNIP.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardInputNIP");
            }

            // Sembunyikan card status akun
            if (cardStatusAkun != null) {
                cardStatusAkun.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardStatusAkun");
            }

            // Sembunyikan icon book akun
            if (icbookakun != null) {
                icbookakun.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden icbookakun");
            }

            // Sembunyikan menu pengelolaan proyek (admin only)
            if (tvMenuData != null) {
                tvMenuData.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden tvMenuData (Menu Pengelolaan Proyek)");
            }

            // Sembunyikan icon book proyek
            if (icbookproyek != null) {
                icbookproyek.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden icbookproyek");
            }

            // Sembunyikan card input proyek, hunian, kavling
            if (cardInputProyek != null) {
                cardInputProyek.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardInputProyek");
            }

            if (cardInputHunian != null) {
                cardInputHunian.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardInputHunian");
            }

            if (cardInputKavling != null) {
                cardInputKavling.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardInputKavling");
            }

            // Sembunyikan menu admin di navigation drawer
            hideAdminNavigationMenus();

        } else {
            // Untuk Admin atau level lainnya, tampilkan semua menu
            Log.d("BerandaActivity", "Showing all features for Admin/Other level: " + userLevel);

            // Tampilkan button tambah promo
            if (cardInputPromoM != null) {
                cardInputPromoM.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardInputPromoM");
            }

            // Tampilkan menu pengelolaan akun
            if (tvMenu2 != null) {
                tvMenu2.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown tvMenu2");
            }

            // Tampilkan card input NIP
            if (cardInputNIP != null) {
                cardInputNIP.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardInputNIP");
            }

            // Tampilkan card status akun
            if (cardStatusAkun != null) {
                cardStatusAkun.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardStatusAkun");
            }

            // Tampilkan icon book akun
            if (icbookakun != null) {
                icbookakun.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown icbookakun");
            }

            // Tampilkan menu pengelolaan proyek (admin only)
            if (tvMenuData != null) {
                tvMenuData.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown tvMenuData (Menu Pengelolaan Proyek)");
            }

            // Tampilkan icon book proyek
            if (icbookproyek != null) {
                icbookproyek.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown icbookproyek");
            }

            // Tampilkan card input proyek, hunian, kavling
            if (cardInputProyek != null) {
                cardInputProyek.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardInputProyek");
            }

            if (cardInputHunian != null) {
                cardInputHunian.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardInputHunian");
            }

            if (cardInputKavling != null) {
                cardInputKavling.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardInputKavling");
            }

            // Tampilkan menu admin di navigation drawer
            showAdminNavigationMenus();
        }

        // PASTIKAN SEMUA TEXTVIEW DAN RECYCLERVIEW TAMPIL UNTUK SEMUA LEVEL
        if (tvMenu != null) {
            tvMenu.setVisibility(View.VISIBLE); // Menu Informasi tetap tampil
            Log.d("BerandaActivity", "tvMenu remains visible for all levels");
        }

        if (tvPromo != null) {
            tvPromo.setVisibility(View.VISIBLE); // Promo Terbaru tetap tampil
            Log.d("BerandaActivity", "tvPromo remains visible for all levels");
        }

        if (tvMenuData2 != null) {
            tvMenuData2.setVisibility(View.VISIBLE); // Menu Input Data tetap tampil untuk semua level
            Log.d("BerandaActivity", "tvMenuData2 remains visible for all levels");
        }

        if (recyclerPromo != null) {
            recyclerPromo.setVisibility(View.VISIBLE);
            Log.d("BerandaActivity", "RecyclerView is visible for all levels");
        }

        // Card Prospek dan Booking tetap tampil untuk semua level
        if (cardProspekM != null) {
            cardProspekM.setVisibility(View.VISIBLE);
            Log.d("BerandaActivity", "cardProspekM remains visible for all levels");
        }

        if (cardUserpM != null) {
            cardUserpM.setVisibility(View.VISIBLE);
            Log.d("BerandaActivity", "cardUserpM remains visible for all levels");
        }
    }

    private void hideAdminNavigationMenus() {
        if (navigationView != null) {
            // Sembunyikan section "Menu Pengelolaan Akun" dan item-itemnya
            hideMenuByTitle("Menu Pengelolaan Akun");
            hideMenuByTitle("Input NIP");
            hideMenuByTitle("Aktivasi Akun");
            hideMenuByTitle("Kelola Akun");
            hideMenuByTitle("Manage Accounts");

            Log.d("NavigationMenu", "Admin menus hidden for " + userLevel);
        }
    }

    private void showAdminNavigationMenus() {
        if (navigationView != null) {
            // Tampilkan section "Menu Pengelolaan Akun" dan item-itemnya
            showMenuByTitle("Menu Pengelolaan Akun");
            showMenuByTitle("Input NIP");
            showMenuByTitle("Aktivasi Akun");
            showMenuByTitle("Kelola Akun");
            showMenuByTitle("Manage Accounts");

            Log.d("NavigationMenu", "Admin menus shown for " + userLevel);
        }
    }

    private void hideMenuByTitle(String title) {
        if (navigationView != null) {
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                if (navigationView.getMenu().getItem(i).getTitle().toString().equalsIgnoreCase(title)) {
                    navigationView.getMenu().getItem(i).setVisible(false);
                    Log.d("NavigationMenu", "Hidden menu: " + title);
                    break;
                }
            }
        }
    }

    private void showMenuByTitle(String title) {
        if (navigationView != null) {
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                if (navigationView.getMenu().getItem(i).getTitle().toString().equalsIgnoreCase(title)) {
                    navigationView.getMenu().getItem(i).setVisible(true);
                    Log.d("NavigationMenu", "Shown menu: " + title);
                    break;
                }
            }
        }
    }

    private void setupRecyclerView() {
        recyclerPromo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        promoAdapter = new PromoAdapter(this, promoList);
        promoAdapter.setUserLevel(userLevel);
        promoAdapter.setOnPromoActionListener(this);
        recyclerPromo.setAdapter(promoAdapter);

        Log.d("BerandaActivity", "RecyclerView setup completed with level: " + userLevel);
    }

    private void loadPromoData() {
        Log.d("NewBeranda", "🔄 Loading Promo Data with Cache Busting");

        // Gunakan timestamp untuk hindari cache
        long timestamp = System.currentTimeMillis();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromoWithTimestamp(timestamp);

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        promoList.clear();
                        promoList.addAll(promoResponse.getData());
                        promoAdapter.notifyDataSetChanged();

                        Log.d("NewBeranda", "✅ Promo Data Loaded: " + promoList.size() + " items");

                        // ✅ TAMBAHKAN INI - Setelah load data, cek manual expired promos sebagai backup
                        new Handler().postDelayed(() -> {
                            checkManualExpiredPromos();
                        }, 1000);

                    } else {
                        Log.e("NewBeranda", "❌ Failed to load promo: " + promoResponse.getMessage());
                        Toast.makeText(NewBeranda.this, "Gagal memuat promo: " + promoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("NewBeranda", "❌ Server Error: " + response.code());
                    Toast.makeText(NewBeranda.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e("NewBeranda", "❌ Network Error: " + t.getMessage());
                Toast.makeText(NewBeranda.this, "Gagal memuat promo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndRequestPermissions() {
        if (!PermissionUtils.areAllPermissionsGranted(this)) {
            if (shouldShowRequestPermissionRationale()) {
                showPermissionExplanationDialog();
            } else {
                PermissionUtils.requestAllPermissions(this);
            }
        } else {
            setupAfterPermissions();
        }
    }

    private boolean shouldShowRequestPermissionRationale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            return ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.POST_NOTIFICATIONS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            // Android 12 dan bawah
            return ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void showPermissionExplanationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Izin Diperlukan")
                .setMessage("Aplikasi membutuhkan izin untuk:\n" +
                        "• Notifikasi - untuk menerima update dan promosi terbaru\n" +
                        "• Penyimpanan - untuk menyimpan dan mengunduh file\n\n" +
                        "Izin ini diperlukan untuk pengalaman penggunaan yang optimal.")
                .setPositiveButton("Berikan Izin", (dialog, which) -> {
                    PermissionUtils.requestAllPermissions(NewBeranda.this);
                })
                .setNegativeButton("Nanti", (dialog, which) -> {
                    setupAfterPermissions();
                })
                .show();
    }

    private void setupAfterPermissions() {
        setupNotificationChannel();
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Promo Notifications";
            String description = "Notifications for latest promotions and updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("promo_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtils.ALL_PERMISSIONS_CODE ||
                requestCode == PermissionUtils.NOTIFICATION_PERMISSION_CODE ||
                requestCode == PermissionUtils.STORAGE_PERMISSION_CODE) {

            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Izin berhasil diberikan", Toast.LENGTH_SHORT).show();
                setupAfterPermissions();
            } else {
                Toast.makeText(this, "Beberapa fitur mungkin tidak berfungsi optimal", Toast.LENGTH_LONG).show();
                setupAfterPermissions();
            }
        }
    }

    // IMPLEMENTASI METHOD DARI INTERFACE - YANG INI SUDAH ADA DAN TIDAK DIUBAH
    @Override
    public void onPromoUpdated(int promoId, String updatedImage) {
        Log.d("BerandaActivity", "Promo updated - ID: " + promoId);

        // Update item di adapter
        if (promoAdapter != null) {
            promoAdapter.updatePromoItem(promoId, updatedImage);
            Toast.makeText(this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();
        }

        // SIMPAN INFO UPDATE UNTUK NEWS ACTIVITY
        savePromoUpdateForNews(promoId, "Diubah", updatedImage);
    }

    // IMPLEMENTASI METHOD BARU YANG DIBUTUHKAN
    @Override
    public void onPromoDeleted(String promoTitle, String penginput) {
        Log.d("BerandaActivity", "Promo deleted: " + promoTitle);
        Toast.makeText(this, "Promo '" + promoTitle + "' dihapus", Toast.LENGTH_SHORT).show();

        // SIMPAN INFO DELETE UNTUK NEWS ACTIVITY
        savePromoDeleteForNews(promoTitle, penginput);
    }

    // METHOD UNTUK SIMPAN INFO UPDATE PROMO - TIDAK DIUBAH
    private void savePromoUpdateForNews(int promoId, String status, String updatedImage) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putInt("last_updated_promo_id", promoId);
        editor.putString("last_updated_status", status);
        editor.putString("last_updated_image", updatedImage != null ? updatedImage : "");
        editor.putLong("last_update_time", System.currentTimeMillis());
        editor.apply();

        Log.d("BerandaActivity", "Saved update info for NewsActivity - Promo ID: " + promoId);
    }

    // METHOD UNTUK SIMPAN INFO DELETE PROMO - TIDAK DIUBAH
    private void savePromoDeleteForNews(String promoTitle, String penginput) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putString("last_deleted_title", promoTitle);
        editor.putString("last_deleted_inputter", penginput);
        editor.putString("last_deleted_status", "Dihapus");
        editor.putLong("last_delete_time", System.currentTimeMillis());
        editor.apply();

        Log.d("BerandaActivity", "Saved delete info for NewsActivity - Title: " + promoTitle);
    }

    // HANDLE ACTIVITY RESULT - TIDAK DIUBAH
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("BerandaActivity", "onActivityResult - Request: " + requestCode + ", Result: " + resultCode);

        if (requestCode == PromoAdapter.EDIT_PROMO_REQUEST && resultCode == RESULT_OK && data != null) {
            handleEditPromoResult(data);
        }
    }

    private void handleEditPromoResult(Intent data) {
        int updatedPromoId = data.getIntExtra("UPDATED_PROMO_ID", -1);
        String updatedImage = data.getStringExtra("UPDATED_IMAGE");

        Log.d("BerandaActivity", "Handle edit result - ID: " + updatedPromoId);

        if (updatedPromoId != -1) {
            // Panggil method update melalui interface
            onPromoUpdated(updatedPromoId, updatedImage);
        } else {
            Log.w("BerandaActivity", "Invalid update data, refreshing from server");
            loadPromoData();
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

        // INTENT UNTUK TAMBAH USERP ACTIVITY - SUDAH ADA
        cardUserpM.setOnClickListener(v -> {
            Intent intentUserp = new Intent(NewBeranda.this, TambahUserpActivity.class);
            startActivity(intentUserp);
        });

        cardInputPromoM.setOnClickListener(v -> {
            // Cek level user untuk akses input promo - Operator, Operator Inhouse, dan Operator Freelance tidak bisa akses
            if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
                Toast.makeText(this, "Hanya Admin yang dapat menambah promo", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(NewBeranda.this, InputPromoActivity.class);
            startActivity(intent);
        });

        // TAMBAHKAN CLICK LISTENER UNTUK MENU ADMIN - DENGAN INTENT YANG BENAR
        if (cardInputNIP != null) {
            cardInputNIP.setOnClickListener(v -> {
                // Operator, Operator Inhouse, dan Operator Freelance tidak bisa akses
                if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses Input NIP", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent ke InputNipActivity
                try {
                    Intent intent = new Intent(NewBeranda.this, InputNipActivity.class);
                    startActivity(intent);
                    Log.d("BerandaActivity", "Opening InputNipActivity");
                } catch (Exception e) {
                    Log.e("BerandaActivity", "Error opening InputNipActivity: " + e.getMessage());
                    Toast.makeText(NewBeranda.this, "Gagal membuka Input NIP", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardStatusAkun != null) {
            cardStatusAkun.setOnClickListener(v -> {
                // Operator, Operator Inhouse, dan Operator Freelance tidak bisa akses
                if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses Aktivasi Akun", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent ke StatusAkunActivity
                try {
                    Intent intent = new Intent(NewBeranda.this, StatusAkunActivity.class);
                    startActivity(intent);
                    Log.d("BerandaActivity", "Opening StatusAkunActivity");
                } catch (Exception e) {
                    Log.e("BerandaActivity", "Error opening StatusAkunActivity: " + e.getMessage());
                    Toast.makeText(NewBeranda.this, "Gagal membuka Aktivasi Akun", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // TAMBAHKAN CLICK LISTENER UNTUK CARD INPUT PROYEK, HUNIAN, KAVLING
        if (cardInputProyek != null) {
            cardInputProyek.setOnClickListener(v -> {
                // Operator, Operator Inhouse, dan Operator Freelance tidak bisa akses
                if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses Input Proyek", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent ke InputProyekActivity
                try {
                    Intent intent = new Intent(NewBeranda.this, InputDataProyekActivity.class);
                    startActivity(intent);
                    Log.d("BerandaActivity", "Opening InputProyekActivity");
                } catch (Exception e) {
                    Log.e("BerandaActivity", "Error opening InputProyekActivity: " + e.getMessage());
                    Toast.makeText(NewBeranda.this, "Gagal membuka Input Proyek", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardInputHunian != null) {
            cardInputHunian.setOnClickListener(v -> {
                // PERBAIKAN: Tambahkan pengecekan level user
                if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses Input Hunian", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent ke InputHunianActivity
                try {
                    Intent intent = new Intent(NewBeranda.this, InputHunianActivity.class);
                    startActivity(intent);
                    Log.d("BerandaActivity", "Opening InputHunianActivity");
                } catch (Exception e) {
                    Log.e("BerandaActivity", "Error opening InputHunianActivity: " + e.getMessage());
                    Toast.makeText(NewBeranda.this, "Gagal membuka Input Hunian: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        if (cardInputKavling != null) {
            cardInputKavling.setOnClickListener(v -> {
                // Operator, Operator Inhouse, dan Operator Freelance tidak bisa akses
                if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses Input Kavling", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent ke InputKavlingActivity
                try {
                    Intent intent = new Intent(NewBeranda.this, InputKavlingActivity.class);
                    startActivity(intent);
                    Log.d("BerandaActivity", "Opening InputKavlingActivity");
                } catch (Exception e) {
                    Log.e("BerandaActivity", "Error opening InputKavlingActivity: " + e.getMessage());
                    Toast.makeText(NewBeranda.this, "Gagal membuka Input Kavling", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
            String title = item.getTitle().toString();
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
            } else if (title.equalsIgnoreCase("Input NIP") ||
                    title.equalsIgnoreCase("Aktivasi Akun") ||
                    title.contains("Pengelolaan Akun")) {
                // Menu admin - cek level user (Operator, Operator Inhouse, dan Operator Freelance tidak bisa akses)
                if ("Operator".equals(userLevel) || "Operator Inhouse".equals(userLevel) || "Operator Freelance".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses menu ini", Toast.LENGTH_SHORT).show();
                    return false;
                }
                // Handle menu admin di sini
                handleAdminMenu(title);
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

    private void handleAdminMenu(String menuTitle) {
        try {
            if (menuTitle.equalsIgnoreCase("Input NIP")) {
                Intent intent = new Intent(this, InputNipActivity.class);
                startActivity(intent);
            } else if (menuTitle.equalsIgnoreCase("Aktivasi Akun")) {
                Intent intent = new Intent(this, StatusAkunActivity.class);
                startActivity(intent);
            } else if (menuTitle.equalsIgnoreCase("Tambah User Prospek") ||
                    menuTitle.contains("User Prospek")) {
                // TAMBAHAN: Intent untuk TambahUserpActivity dari navigation drawer
                Intent intent = new Intent(this, TambahUserpActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Membuka: " + menuTitle, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("BerandaActivity", "Error handling admin menu: " + e.getMessage());
            Toast.makeText(this, "Gagal membuka menu: " + menuTitle, Toast.LENGTH_SHORT).show();
        }
    }
    // ✅ BACKGROUND SERVICE UNTUK AUTO DELETE REAL-TIME
    private void startAutoDeleteBackgroundService() {
        Log.d("NewBeranda", "🔄 Starting Auto Delete Background Service");

        autoDeleteHandler = new Handler(Looper.getMainLooper());
        autoDeleteRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("NewBeranda", "⏰ Auto Delete Service Running - " + new Date());
                executeAutoDeleteWithRetry();
                autoDeleteHandler.postDelayed(this, AUTO_DELETE_INTERVAL);
            }
        };

        // Start dengan delay initial
        autoDeleteHandler.postDelayed(autoDeleteRunnable, INITIAL_DELAY);

        Toast.makeText(this, "Auto delete service started", Toast.LENGTH_SHORT).show();
    }

    // ✅ EXECUTE AUTO DELETE DENGAN RETRY MECHANISM
    private void executeAutoDeleteWithRetry() {
        Log.d("NewBeranda", "🔄 Executing Auto Delete with Retry Mechanism");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.autoDeleteExpiredPromos();

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("NewBeranda", "✅ Auto Delete Success: " + basicResponse.getMessage());

                        // Refresh data setelah auto delete berhasil
                        new Handler().postDelayed(() -> {
                            loadPromoData();
                            showAutoDeleteNotification(basicResponse);
                        }, 1500);

                    } else {
                        Log.e("NewBeranda", "❌ Auto Delete Failed: " + basicResponse.getMessage());
                        retryAutoDelete(1); // Retry sekali
                    }
                } else {
                    Log.e("NewBeranda", "❌ Server Error: " + response.code());
                    retryAutoDelete(1);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("NewBeranda", "❌ Network Error: " + t.getMessage());
                retryAutoDelete(1);
            }
        });
    }

    // ✅ RETRY MECHANISM UNTUK AUTO DELETE
    private void retryAutoDelete(int retryCount) {
        if (retryCount <= 3) {
            Log.d("NewBeranda", "🔄 Retry Auto Delete - Attempt " + retryCount);

            new Handler().postDelayed(() -> {
                executeAutoDeleteWithRetry();
            }, 3000 * retryCount); // Exponential backoff
        } else {
            Log.e("NewBeranda", "❌ Auto Delete failed after 3 retries");
        }
    }

    // ✅ NOTIFIKASI LOKAL SETELAH AUTO DELETE
    private void showAutoDeleteNotification(BasicResponse response) {
        try {
            String title = "🔄 Auto Delete Executed";
            String message = response.getMessage();

            NotificationHelper.showSimpleNotification(this, title, message);
            Log.d("NewBeranda", "📢 Auto Delete Notification: " + message);

        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error showing auto delete notification: " + e.getMessage());
        }
    }

    // ✅ CEK PROMO KADALUARSA SECARA MANUAL (BACKUP)
    private void checkManualExpiredPromos() {
        Log.d("NewBeranda", "🔍 Manual Check for Expired Promos");

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        boolean foundExpired = false;

        for (int i = promoList.size() - 1; i >= 0; i--) {
            Promo promo = promoList.get(i);
            if (isPromoExpired(promo.getKadaluwarsa(), currentDate)) {
                Log.d("NewBeranda", "⚠️ Found Locally Expired Promo: " + promo.getNamaPromo());
                showExpiredPromoAlert(promo.getNamaPromo());
                foundExpired = true;
            }
        }

        if (!foundExpired) {
            Log.d("NewBeranda", "✅ No Expired Promos Found in Manual Check");
        }
    }

    private boolean isPromoExpired(String expiryDate, String currentDate) {
        if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("null")) {
            return false;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date promoDate = dateFormat.parse(expiryDate);
            Date today = dateFormat.parse(currentDate);

            return promoDate.before(today);
        } catch (ParseException e) {
            Log.e("NewBeranda", "❌ Error parsing date: " + e.getMessage());
            return false;
        }
    }

    private void showExpiredPromoAlert(String promoName) {
        // Notifikasi lokal untuk promo kadaluarsa
        String title = "🕒 Promo Expired (Local)";
        String message = "Promo '" + promoName + "' telah kadaluarsa";

        NotificationHelper.showSimpleNotification(this, title, message);
        Log.d("NewBeranda", "📢 Local Expired Alert: " + message);
    }

    // ✅ HENTIKAN SERVICE SAAT ACTIVITY DESTROY
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoDeleteBackgroundService();
        Log.d("NewBeranda", "🛑 Auto Delete Background Service Stopped");
    }

    private void stopAutoDeleteBackgroundService() {
        if (autoDeleteHandler != null && autoDeleteRunnable != null) {
            autoDeleteHandler.removeCallbacks(autoDeleteRunnable);
            autoDeleteHandler = null;
            autoDeleteRunnable = null;
        }
    }
    private void logout() {
        stopAutoDeleteBackgroundService();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove("username");
        editor.remove("division");
        editor.remove("nip");
        editor.remove(KEY_LEVEL);
        editor.apply();

        Toast.makeText(NewBeranda.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(NewBeranda.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromoData();

        // Refresh access control setiap resume
        setupUserInfo();
        setupAccessBasedOnLevel();
        if (autoDeleteHandler == null) {
            startAutoDeleteBackgroundService();
        }

        Log.d("BerandaActivity", "onResume completed - Level: " + userLevel);
    }
}