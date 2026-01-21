package com.example.mobile;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private SharedPreferences newsPrefs;
    private static final String NEWS_PREFS_NAME = "NewsUpdates";
    private static final String TAG = "NewBeranda";
    // Variabel untuk menyimpan level user
    private String userLevel = "";
    private Handler autoDeleteHandler;
    private Runnable autoDeleteRunnable;
    private static final long AUTO_DELETE_INTERVAL = 2 * 60 * 1000; // 2 menit (dari 5 menit)
    private static final long INITIAL_DELAY = 5000; // 5 detik (dari 10 detik)
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
        startEnhancedAutoDeleteService();

        // ✅ DEBUG: Manual check saat startup
        new Handler().postDelayed(() -> {
            debugManualExpiryCheck();
        }, 3000);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("NewBeranda", "onActivityResult - Request: " + requestCode + ", Result: " + resultCode);

        if (requestCode == PromoAdapter.EDIT_PROMO_REQUEST && resultCode == RESULT_OK && data != null) {
            handleEditPromoResult(data);
        }
    }

    // ✅ METHOD BARU: Handle promo kadaluwarsa dengan status yang benar
    @Override
    public void onPromoExpired(String promoTitle, String penginput) {
        Log.d("NewBeranda", "Promo expired: " + promoTitle);

        // ✅ SIMPAN KE HISTORI DENGAN STATUS KADALUWARSA
        savePromoExpiredForNews(promoTitle, penginput);

        // ✅ KIRIM BROADCAST KE DELETED NEWS ACTIVITY
        sendExpiredPromoBroadcast(promoTitle, penginput);

        // ✅ TAMPILKAN NOTIFIKASI KADALUWARSA
        showExpiredNotification(promoTitle);
    }

    // ✅ METHOD BARU: Simpan info expired untuk NewsActivity
    private void savePromoExpiredForNews(String promoTitle, String penginput) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putString("last_expired_title", promoTitle);
        editor.putString("last_expired_inputter", penginput);
        editor.putString("last_expired_status", "Kadaluwarsa");
        editor.putLong("last_expired_time", System.currentTimeMillis());
        editor.apply();

        Log.d("NewBeranda", "Saved expired info for DeletedNewsActivity - Title: " + promoTitle);
    }

    // ✅ METHOD BARU: Kirim broadcast khusus untuk promo kadaluwarsa
    private void sendExpiredPromoBroadcast(String promoTitle, String penginput) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_DELETED_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "PROMO_EXPIRED");
            broadcastIntent.putExtra("PROMO_TITLE", promoTitle);
            broadcastIntent.putExtra("PENGINPUT", penginput);
            broadcastIntent.putExtra("STATUS", "Kadaluwarsa"); // ✅ STATUS YANG BENAR

            sendBroadcast(broadcastIntent);
            Log.d("NewBeranda", "📢 Expired broadcast sent: " + promoTitle);
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error sending expired broadcast: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: Notifikasi untuk promo kadaluwarsa
    private void showExpiredNotification(String promoName) {
        try {
            String title = "🕒 Promo Kadaluwarsa";
            String message = "Promo '" + promoName + "' telah kadaluwarsa dan dipindahkan ke arsip";

            NotificationHelper.showSimpleNotification(this, title, message);

            runOnUiThread(() -> {
                Toast.makeText(NewBeranda.this, message, Toast.LENGTH_LONG).show();
            });

            Log.d("NewBeranda", "📢 Expired notification: " + message);
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error showing expired notification: " + e.getMessage());
        }
    }

    // DI NEWBERANDA.JAVA - PERBAIKI METHOD handleEditPromoResult
    private void handleEditPromoResult(Intent data) {
        int updatedPromoId = data.getIntExtra("UPDATED_PROMO_ID", -1);
        String updatedImage = data.getStringExtra("UPDATED_IMAGE");
        String updatedTitle = data.getStringExtra("UPDATED_TITLE");
        String updatedUser = data.getStringExtra("UPDATED_USER");
        boolean isSuccess = data.getBooleanExtra("IS_SUCCESS", false);

        Log.d("NewBeranda", "Handle edit result - ID: " + updatedPromoId +
                ", Success: " + isSuccess +
                ", Image: " + (updatedImage != null ? updatedImage.length() + " chars" : "null") +
                ", Title: " + updatedTitle);

        if (isSuccess && updatedPromoId != -1) {
            // ✅ PERBAIKAN: Update adapter dengan gambar baru
            if (promoAdapter != null) {
                promoAdapter.updatePromoItem(updatedPromoId, updatedImage);
                Log.d("NewBeranda", "✅ Adapter updated with new image for promo ID: " + updatedPromoId);
            }

            // ✅ PERBAIKAN: Simpan info update untuk NewsActivity dengan data lengkap
            savePromoUpdateForNews(updatedPromoId, "Diubah", updatedImage, updatedTitle, updatedUser);

            // ✅ PERBAIKAN: Kirim broadcast langsung ke NewsActivity
            sendUpdateBroadcastToNews(updatedPromoId, updatedImage, updatedTitle, updatedUser);

            // Refresh data dari server untuk memastikan konsistensi
            new Handler().postDelayed(() -> {
                loadPromoData();
            }, 1000);

            Toast.makeText(this, "Promo '" + updatedTitle + "' berhasil diupdate", Toast.LENGTH_SHORT).show();
        } else {
            String errorMessage = data.getStringExtra("ERROR_MESSAGE");
            Log.w("NewBeranda", "Update failed: " + errorMessage);
            Toast.makeText(this, "Update gagal: " + errorMessage, Toast.LENGTH_SHORT).show();
            loadPromoData(); // Refresh anyway
        }
    }

    // ✅ METHOD BARU: Kirim broadcast langsung ke NewsActivity
    private void sendUpdateBroadcastToNews(int promoId, String imageData, String title, String user) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("UPDATED_PROMO_ID", promoId);
            broadcastIntent.putExtra("UPDATED_IMAGE", imageData);
            broadcastIntent.putExtra("UPDATED_TITLE", title);
            broadcastIntent.putExtra("UPDATED_USER", user);
            broadcastIntent.putExtra("STATUS", "Diubah");

            sendBroadcast(broadcastIntent);
            Log.d("NewBeranda", "📢 Direct broadcast sent to NewsActivity - ID: " + promoId);
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error sending broadcast: " + e.getMessage());
        }
    }

    // IMPLEMENTASI METHOD DARI INTERFACE - YANG INI SUDAH ADA DAN TIDAK DIUBAH
    @Override
    public void onPromoUpdated(int promoId, String updatedImage) {
        Log.d("NewBeranda", "Promo updated - ID: " + promoId + ", Image: " + (updatedImage != null ? updatedImage.length() + " chars" : "null"));

        // Update item di adapter
        if (promoAdapter != null) {
            promoAdapter.updatePromoItem(promoId, updatedImage);
            Toast.makeText(this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();
        }

        // SIMPAN INFO UPDATE UNTUK NEWS ACTIVITY (versi lama untuk kompatibilitas)
        savePromoUpdateForNews(promoId, "Diubah", updatedImage);
    }

    private void savePromoUpdateForNews(int promoId, String status, String updatedImage, String title, String user) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putInt("last_updated_promo_id", promoId);
        editor.putString("last_updated_status", status);
        editor.putString("last_updated_image", updatedImage != null ? updatedImage : "");
        editor.putString("last_updated_title", title != null ? title : "");
        editor.putString("last_updated_inputter", user != null ? user : "");
        editor.putLong("last_update_time", System.currentTimeMillis());
        editor.apply();

        Log.d("NewBeranda", "Saved update info for NewsActivity - Promo ID: " + promoId + ", Title: " + title);

        // PERBAIKAN: Kirim broadcast langsung ke NewsActivity
        sendUpdateBroadcastToNews(promoId, updatedImage, title, user);
    }


    // METHOD UNTUK SIMPAN INFO UPDATE PROMO - VERSI LAMA (untuk kompatibilitas)
    private void savePromoUpdateForNews(int promoId, String status, String updatedImage) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putInt("last_updated_promo_id", promoId);
        editor.putString("last_updated_status", status);
        editor.putString("last_updated_image", updatedImage != null ? updatedImage : "");
        editor.putLong("last_update_time", System.currentTimeMillis());
        editor.apply();

        Log.d("NewBeranda", "Saved update info for NewsActivity - Promo ID: " + promoId);
    }

    // METHOD UNTUK SIMPAN INFO DELETE PROMO - TIDAK DIUBAH
    private void savePromoDeleteForNews(String promoTitle, String penginput) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putString("last_deleted_title", promoTitle);
        editor.putString("last_deleted_inputter", penginput);
        editor.putString("last_deleted_status", "Dihapus");
        editor.putLong("last_delete_time", System.currentTimeMillis());
        editor.apply();

        Log.d("NewBeranda", "Saved delete info for NewsActivity - Title: " + promoTitle);
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

    // ✅ PERBAIKAN: Enhanced Auto Delete Service yang benar-benar bekerja
    private void startEnhancedAutoDeleteService() {
        Log.d("NewBeranda", "🚀 Starting Enhanced Auto Delete Service");

        // Hentikan service sebelumnya jika ada
        stopAutoDeleteBackgroundService();

        autoDeleteHandler = new Handler(Looper.getMainLooper());
        autoDeleteRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("NewBeranda", "🔄 Enhanced Auto Delete Running - " + new Date());

                // ✅ PERBAIKAN KRITIS: Panggil API auto delete yang benar
                triggerEnhancedServerAutoDelete();

                // ✅ PERBAIKAN: Pastikan handler tetap berjalan
                if (autoDeleteHandler != null) {
                    autoDeleteHandler.postDelayed(this, AUTO_DELETE_INTERVAL);
                    Log.d("NewBeranda", "✅ Auto Delete Scheduled for next run");
                }
            }
        };

        // Jalankan segera
        autoDeleteHandler.postDelayed(autoDeleteRunnable, INITIAL_DELAY);
        Log.d("NewBeranda", "✅ Enhanced auto delete service started");
    }


    // ✅ METHOD BARU: Kirim broadcast khusus untuk kadaluwarsa
    private void sendEnhancedExpiredBroadcast(int expiredCount) {
        Log.d("NewBeranda", "📢 Sending Enhanced EXPIRED Broadcast");

        try {
            Intent broadcast = new Intent("REFRESH_DELETED_NEWS_DATA");
            broadcast.putExtra("ACTION", "PROMO_EXPIRED");
            broadcast.putExtra("EXPIRED_COUNT", expiredCount);
            broadcast.putExtra("TIMESTAMP", System.currentTimeMillis());
            sendBroadcast(broadcast);

            Log.d("NewBeranda", "✅ Enhanced expired broadcast sent");

        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Enhanced expired broadcast failed: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: Notifikasi auto delete untuk kadaluwarsa
    private void showExpiredAutoDeleteNotification(int expiredCount) {
        try {
            String title = "🕒 Auto Delete Executed";
            String message = expiredCount + " promo kadaluwarsa telah dipindahkan ke arsip";

            NotificationHelper.showSimpleNotification(this, title, message);
            Log.d("NewBeranda", "📢 Expired Auto Delete Notification: " + message);

        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error showing expired notification: " + e.getMessage());
        }
    }
 


    // ✅ METHOD BARU: Manual check yang lebih detail
    private void debugManualExpiryCheck() {
        Log.d("NewBeranda", "=== MANUAL EXPIRY CHECK DEBUG ===");

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d("NewBeranda", "Current Date: " + currentDate);

        for (int i = 0; i < promoList.size(); i++) {
            Promo promo = promoList.get(i);
            String expiryDate = promo.getKadaluwarsa();

            Log.d("NewBeranda", "Promo " + i + ": " + promo.getNamaPromo());
            Log.d("NewBeranda", "  - ID: " + promo.getIdPromo());
            Log.d("NewBeranda", "  - Kadaluwarsa: '" + expiryDate + "'");

            if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("null")) {
                Log.d("NewBeranda", "  ❌ INVALID: Null or empty date");
                continue;
            }

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date promoExpiry = format.parse(expiryDate);
                Date today = format.parse(currentDate);

                boolean isExpired = promoExpiry.before(today);
                Log.d("NewBeranda", "  - Is Expired: " + isExpired);
                Log.d("NewBeranda", "  - Comparison: " + expiryDate + " < " + currentDate + " = " + isExpired);

            } catch (ParseException e) {
                Log.e("NewBeranda", "  ❌ PARSE ERROR: " + e.getMessage());
            }
        }
        Log.d("NewBeranda", "=== END MANUAL DEBUG ===");
    }

    // ✅ METHOD BARU: Cek ketersediaan jaringan
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error checking network: " + e.getMessage());
            return false;
        }
    }
    // ✅ PERBAIKAN: Manual check dengan debugging yang lebih detail
    private void checkAndProcessExpiredPromosManually() {
        Log.d("NewBeranda", "🔍 Manual PROCESSING expired promos - ENHANCED DEBUG");

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Promo> expiredPromos = new ArrayList<>();

        Log.d("NewBeranda", "📅 DEBUG - Current Date: " + currentDate);
        Log.d("NewBeranda", "📊 DEBUG - Total Promos to Check: " + promoList.size());

        // Debug semua promo dan tanggal kadaluwarsanya
        for (int i = 0; i < promoList.size(); i++) {
            Promo promo = promoList.get(i);
            String expiryDate = promo.getKadaluwarsa();

            Log.d("NewBeranda", "🔍 DEBUG Promo " + i + ": " + promo.getNamaPromo());
            Log.d("NewBeranda", "   - Kadaluwarsa: " + expiryDate);
            Log.d("NewBeranda", "   - ID: " + promo.getIdPromo());

            if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("null")) {
                Log.d("NewBeranda", "   ❌ SKIP - Invalid expiry date");
                continue;
            }

            // Pengecekan manual sederhana untuk debug
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date promoExpiry = format.parse(expiryDate);
                Date today = format.parse(currentDate);

                boolean isExpiredManual = promoExpiry.before(today);
                Log.d("NewBeranda", "   - Manual Check: " + expiryDate + " < " + currentDate + " = " + isExpiredManual);

            } catch (Exception e) {
                Log.e("NewBeranda", "   ❌ Error in manual date check: " + e.getMessage());
            }

            // Gunakan method isPromoExpired
            boolean isExpired = isPromoExpired(expiryDate, currentDate);

            Log.d("NewBeranda", "   - Final Result: " + isExpired);

            if (isExpired) {
                expiredPromos.add(promo);
                Log.d("NewBeranda", "   ⚠️ ✅ ADDED TO EXPIRED LIST");
            } else {
                Log.d("NewBeranda", "   ✅ NOT EXPIRED - Skip");
            }
            Log.d("NewBeranda", "   ---");
        }

        if (!expiredPromos.isEmpty()) {
            Log.d("NewBeranda", "🚨 MANUAL PROCESS - Found " + expiredPromos.size() + " expired promos:");

            for (Promo expired : expiredPromos) {
                Log.d("NewBeranda", "   - " + expired.getNamaPromo() + " (Exp: " + expired.getKadaluwarsa() + ")");
            }

            // Process setiap promo yang expired
            for (Promo expiredPromo : expiredPromos) {
                processExpiredPromoManually(expiredPromo);
            }

            // Kirim broadcast
            sendExpiredPromoBroadcastForManual(expiredPromos);

            Log.d("NewBeranda", "✅ Manual processing completed for " + expiredPromos.size() + " expired promos");

            // Tampilkan toast
            runOnUiThread(() -> {
                Toast.makeText(NewBeranda.this,
                        "Ditemukan " + expiredPromos.size() + " promo kadaluwarsa",
                        Toast.LENGTH_LONG).show();
            });

        } else {
            Log.d("NewBeranda", "✅ MANUAL PROCESS - No expired promos found");

            // Tampilkan toast info
            runOnUiThread(() -> {
                Toast.makeText(NewBeranda.this,
                        "Tidak ada promo yang kadaluwarsa (Current: " + currentDate + ")",
                        Toast.LENGTH_LONG).show();
            });
        }
    }

    // ✅ METHOD BARU: Process expired promo dengan error handling
    private void processExpiredPromoManually(Promo expiredPromo) {
        Log.d("NewBeranda", "🕒 Processing EXPIRED promo manually: " + expiredPromo.getNamaPromo());

        try {
            // Langsung simpan ke histori dengan status Kadaluwarsa
            savePromoExpiredToHistori(expiredPromo);

            // Refresh data untuk menghapus dari tampilan beranda
            new Handler().postDelayed(() -> {
                loadPromoData();
            }, 1000);

            Log.d("NewBeranda", "✅ Successfully processed expired promo: " + expiredPromo.getNamaPromo());

        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error processing expired promo: " + expiredPromo.getNamaPromo() + " - " + e.getMessage());
        }
    }


    // ✅ METHOD BARU: Kirim broadcast untuk manual delete expired
    private void sendExpiredPromoBroadcastForManual(List<Promo> expiredPromos) {
        try {
            for (Promo promo : expiredPromos) {
                Intent broadcastIntent = new Intent("REFRESH_DELETED_NEWS_DATA");
                broadcastIntent.putExtra("ACTION", "PROMO_EXPIRED");
                broadcastIntent.putExtra("PROMO_TITLE", promo.getNamaPromo());
                broadcastIntent.putExtra("PENGINPUT", promo.getNamaPenginput());
                sendBroadcast(broadcastIntent);
            }
            Log.d("NewBeranda", "📢 Manual expired broadcast sent for " + expiredPromos.size() + " promos");
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error sending manual expired broadcast: " + e.getMessage());
        }
    }

    // ✅ PERBAIKAN: Enhanced server auto delete dengan response handling yang lebih baik
    private void triggerEnhancedServerAutoDelete() {
        Log.d("NewBeranda", "🎯 Enhanced Server Auto Delete Triggered - " + new Date());

        if (!isNetworkAvailable()) {
            Log.e("NewBeranda", "❌ No network connection - Auto delete skipped");
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Log.d("NewBeranda", "📡 Calling autoDeleteExpiredPromos API...");

        Call<BasicResponse> call = apiService.autoDeleteExpiredPromos();

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                try {
                    Log.d("NewBeranda", "📡 API Response Received - Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse basicResponse = response.body();

                        // ✅ DEBUG DETAIL RESPONSE
                        debugAutoDeleteResponse(basicResponse);

                        if (basicResponse.isSuccess()) {
                            int expiredCount = basicResponse.getExpiredCount();
                            int deletedCount = basicResponse.getDeletedCount();

                            Log.d("NewBeranda", "✅ Auto delete successful - Expired: " + expiredCount + ", Deleted: " + deletedCount);

                            runOnUiThread(() -> {
                                if (expiredCount > 0) {
                                    if (deletedCount > 0) {
                                        // ✅ ADA YANG BERHASIL DIHAPUS
                                        String message = deletedCount + " promo kadaluwarsa telah dihapus";
                                        Toast.makeText(NewBeranda.this, message, Toast.LENGTH_LONG).show();
                                        showExpiredAutoDeleteNotification(deletedCount);

                                        // ✅ REFRESH DATA
                                        loadPromoDataForceRefresh();
                                    } else {
                                        // ✅ DITEMUKAN EXPIRED TAPI GAGAL DIHAPUS
                                        String message = "Ditemukan " + expiredCount + " promo kadaluwarsa tetapi gagal dihapus. Periksa log server.";
                                        Toast.makeText(NewBeranda.this, message, Toast.LENGTH_LONG).show();
                                        Log.e("NewBeranda", "❌ Promo ditemukan kadaluwarsa tetapi tidak terhapus");

                                        // ✅ COBA MANUAL DELETE SEBAGAI FALLBACK
                                        checkAndProcessExpiredPromosManually();
                                    }
                                } else {
                                    Log.d("NewBeranda", "✅ No expired promos found at this time");
                                }
                            });

                            // ✅ KIRIM BROADCAST MESKI TIDAK ADA YANG DIHAPUS
                            sendEnhancedExpiredBroadcast(expiredCount);

                        } else {
                            Log.e("NewBeranda", "❌ Auto delete failed: " + basicResponse.getMessage());
                            // Fallback ke manual check
                            runOnUiThread(() -> {
                                Toast.makeText(NewBeranda.this,
                                        "Auto delete gagal: " + basicResponse.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                            checkAndProcessExpiredPromosManually();
                        }
                    } else {
                        String errorBody = "No error body";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            errorBody = "Error reading error body: " + e.getMessage();
                        }

                        Log.e("NewBeranda", "❌ Server error in auto delete: " + response.code() + " - " + errorBody);

                        runOnUiThread(() -> {
                            Toast.makeText(NewBeranda.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        });

                        checkAndProcessExpiredPromosManually();
                    }
                } catch (Exception e) {
                    Log.e("NewBeranda", "❌ Enhanced auto delete error: " + e.getMessage());

                    runOnUiThread(() -> {
                        Toast.makeText(NewBeranda.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });

                    checkAndProcessExpiredPromosManually();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("NewBeranda", "❌ Enhanced auto delete NETWORK FAILURE: " + t.getMessage());

                runOnUiThread(() -> {
                    Toast.makeText(NewBeranda.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                });

                checkAndProcessExpiredPromosManually();
            }
        });
    }

    // ✅ METHOD BARU: Kirim broadcast untuk jumlah expired
    private void sendExpiredPromoBroadcastForCount(int expiredCount) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_DELETED_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "PROMO_EXPIRED");
            broadcastIntent.putExtra("EXPIRED_COUNT", expiredCount);
            broadcastIntent.putExtra("TIMESTAMP", System.currentTimeMillis());
            sendBroadcast(broadcastIntent);
            Log.d("NewBeranda", "📢 Expired count broadcast sent: " + expiredCount);
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error sending expired count broadcast: " + e.getMessage());
        }
    }


    // ✅ PERBAIKAN: Method stop yang lebih comprehensive
    private void stopAutoDeleteBackgroundService() {
        Log.d("NewBeranda", "🛑 Stopping Auto Delete Background Service");

        if (autoDeleteHandler != null && autoDeleteRunnable != null) {
            autoDeleteHandler.removeCallbacks(autoDeleteRunnable);
            Log.d("NewBeranda", "✅ Auto Delete Handler callbacks removed");
        }

        autoDeleteHandler = null;
        autoDeleteRunnable = null;

        Log.d("NewBeranda", "✅ Auto Delete Background Service Stopped");
    }

    // ✅ METHOD BARU: Debug detail untuk auto delete
    private void debugAutoDeleteResponse(BasicResponse response) {
        Log.d("NewBeranda", "=== AUTO DELETE DEBUG ===");
        Log.d("NewBeranda", "Success: " + response.isSuccess());
        Log.d("NewBeranda", "Message: " + response.getMessage());
        Log.d("NewBeranda", "Expired Count: " + response.getExpiredCount());
        Log.d("NewBeranda", "Deleted Count: " + response.getDeletedCount());
        Log.d("NewBeranda", "Total Processed: " + response.getTotalProcessed());

        if (response.hasExpiredPromos()) {
            Log.d("NewBeranda", "Expired Promos List: " + response.getExpiredPromos().size());
            for (Map<String, Object> promo : response.getExpiredPromos()) {
                Log.d("NewBeranda", " - " + promo.get("Nama_Promo") + " (ID: " + promo.get("Id_promo") + ")");
            }
        }
        Log.d("NewBeranda", "=== END DEBUG ===");
    }


    // ✅ METHOD BARU: Force refresh data dengan lebih agresif
    private void loadPromoDataForceRefresh() {
        Log.d("NewBeranda", "⚡ Force Refreshing Promo Data");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        List<Promo> newData = promoResponse.getData();

                        runOnUiThread(() -> {
                            int previousSize = promoList.size();
                            promoList.clear();
                            promoList.addAll(newData);

                            if (promoAdapter != null) {
                                promoAdapter.notifyDataSetChanged();
                                Log.d("NewBeranda", "✅ Data force refreshed: " + previousSize + " → " + promoList.size());

                                // Tampilkan toast jika ada perubahan
                                if (previousSize != promoList.size()) {
                                    Toast.makeText(NewBeranda.this,
                                            "Data promo diperbarui: " + promoList.size() + " item",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e("NewBeranda", "❌ Force refresh failed: " + t.getMessage());
            }
        });
    }

    // ✅ PERBAIKAN: Method isPromoExpired yang BENAR
    private boolean isPromoExpired(String expiryDate, String currentDate) {
        Log.d("NewBeranda", "🔍 DETAILED EXPIRY CHECK - FIXED LOGIC");
        Log.d("NewBeranda", "  - Expiry Date: '" + expiryDate + "'");
        Log.d("NewBeranda", "  - Current Date: '" + currentDate + "'");

        if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("null") || expiryDate.equals("0000-00-00")) {
            Log.d("NewBeranda", "  ❌ INVALID expiry date");
            return false;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateFormat.setLenient(false);

            Date promoDate = dateFormat.parse(expiryDate.trim());
            Date today = dateFormat.parse(currentDate);

            // ✅ PERBAIKAN KRITIS: Reset waktu untuk perbandingan yang akurat
            Calendar calPromo = Calendar.getInstance();
            calPromo.setTime(promoDate);
            calPromo.set(Calendar.HOUR_OF_DAY, 0);
            calPromo.set(Calendar.MINUTE, 0);
            calPromo.set(Calendar.SECOND, 0);
            calPromo.set(Calendar.MILLISECOND, 0);

            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);

            // ✅ PERBAIKAN LOGIKA: Promo expired jika tanggal kadaluwarsa SEBELUM hari ini
            // Contoh: expiryDate = 2024-01-15, currentDate = 2024-01-16 → expired = true
            boolean isExpired = calPromo.before(calToday);

            Log.d("NewBeranda", "  - Promo Date (ms): " + calPromo.getTimeInMillis());
            Log.d("NewBeranda", "  - Today Date (ms): " + calToday.getTimeInMillis());
            Log.d("NewBeranda", "  - Is Expired: " + isExpired);
            Log.d("NewBeranda", "  - Logic: promoDate (" + expiryDate + ") < today (" + currentDate + ") = " + isExpired);

            return isExpired;

        } catch (ParseException e) {
            Log.e("NewBeranda", "❌ Error parsing date: '" + expiryDate + "' - " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Unexpected error in expiry check: " + e.getMessage());
            return false;
        }
    }

    // ✅ PERBAIKAN: Method untuk save promo expired ke histori dengan status yang BENAR
    private void savePromoExpiredToHistori(Promo expiredPromo) {
        Log.d("NewBeranda", "💾 Saving EXPIRED promo to histori: " + expiredPromo.getNamaPromo());

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("action", "add_promo_histori");
        requestBody.put("promo_id", expiredPromo.getIdPromo());
        requestBody.put("title", expiredPromo.getNamaPromo());
        requestBody.put("penginput", expiredPromo.getNamaPenginput());

        // ✅ PERBAIKAN KRITIS: GUNAKAN STATUS 'Kadaluwarsa'
        requestBody.put("status", "Kadaluwarsa");

        requestBody.put("image_data", expiredPromo.getGambarBase64());
        requestBody.put("kadaluwarsa", expiredPromo.getKadaluwarsa());

        Log.d("NewBeranda", "📤 Sending histori with CORRECT STATUS: Kadaluwarsa");

        Call<BasicResponse> call = apiService.addPromoHistoriWithBody(requestBody);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("NewBeranda", "✅ Histori KADALUWARSA saved: " + expiredPromo.getNamaPromo());

                        // ✅ KIRIM BROADCAST KE DELETED NEWS ACTIVITY DENGAN STATUS YANG BENAR
                        sendExpiredPromoBroadcast(expiredPromo);

                        // ✅ TAMPILKAN NOTIFIKASI KADALUWARSA
                        showExpiredNotification(expiredPromo.getNamaPromo());

                    } else {
                        Log.e("NewBeranda", "❌ Failed to save histori: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e("NewBeranda", "❌ Error response histori: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("NewBeranda", "❌ Error saving histori: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Kirim broadcast khusus untuk promo kadaluwarsa
    private void sendExpiredPromoBroadcast(Promo expiredPromo) {
        try {
            Intent broadcastIntent = new Intent("REFRESH_DELETED_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "PROMO_EXPIRED");
            broadcastIntent.putExtra("PROMO_TITLE", expiredPromo.getNamaPromo());
            broadcastIntent.putExtra("PENGINPUT", expiredPromo.getNamaPenginput());
            broadcastIntent.putExtra("IMAGE_DATA", expiredPromo.getGambarBase64());
            broadcastIntent.putExtra("KADALUWARSA", expiredPromo.getKadaluwarsa());
            broadcastIntent.putExtra("PROMO_ID", expiredPromo.getIdPromo());

            sendBroadcast(broadcastIntent);
            Log.d("NewBeranda", "📢 Broadcast sent for EXPIRED promo: " + expiredPromo.getNamaPromo());

        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error sending expired broadcast: " + e.getMessage());
        }
    }


    // ✅ METHOD BARU: Tampilkan notifikasi lokal
    private void showLocalAutoDeleteNotification(int expiredCount, int deletedCount) {
        try {
            String title = "🔄 Auto Delete Executed";
            String message = expiredCount + " promo kadaluwarsa telah dipindahkan ke arsip";

            NotificationHelper.showSimpleNotification(this, title, message);
            Log.d("NewBeranda", "📢 Local Auto Delete Notification: " + message);

            // ✅ TAMPILKAN TOAST JUGA
            runOnUiThread(() -> {
                Toast.makeText(NewBeranda.this, message, Toast.LENGTH_LONG).show();
            });
        } catch (Exception e) {
            Log.e("NewBeranda", "❌ Error showing local notification: " + e.getMessage());
        }
    }



    // ✅ PERBAIKAN: Load promo data dengan force refresh
    private void loadPromoData() {
        Log.d("NewBeranda", "🔄 Loading Promo Data - FORCE REFRESH");

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

                        Log.d("NewBeranda", "✅ Promo data refreshed: " + promoList.size() + " items");

                        // ✅ PERBAIKAN: Update UI langsung
                        updatePromoUI();

                    } else {
                        Log.e("NewBeranda", "❌ API Response not success: " + promoResponse.getMessage());
                    }
                } else {
                    Log.e("NewBeranda", "❌ Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e("NewBeranda", "❌ Network Error: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Update UI langsung setelah data berubah
    private void updatePromoUI() {
        runOnUiThread(() -> {
            if (promoAdapter != null) {
                promoAdapter.notifyDataSetChanged();

                // Update empty state jika perlu
                if (promoList.isEmpty()) {
                    Toast.makeText(this, "Tidak ada promo aktif saat ini", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // ✅ HENTIKAN SERVICE SAAT ACTIVITY DESTROY
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoDeleteBackgroundService();
        Log.d("NewBeranda", "🛑 Auto Delete Background Service Stopped");
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

    // ✅ METHOD YANG DIPERBAIKI - Hanya untuk NewsActivity saja
    @Override
    public void onPromoDeleted(String promoTitle, String penginput) {
        Log.d("NewBeranda", "Manual promo deleted: " + promoTitle);
        Toast.makeText(this, "Promo '" + promoTitle + "' dihapus", Toast.LENGTH_SHORT).show();

        // ✅ Hanya simpan untuk NewsActivity
        savePromoDeleteForNews(promoTitle, penginput);

        // ❌ JANGAN panggil save histori dari sini
        // Biarkan PHP yang handle status yang benar
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromoData();

        // Refresh access control setiap resume
        setupUserInfo();
        setupAccessBasedOnLevel();

        // ✅ PERBAIKAN: Pastikan auto delete service berjalan
        if (autoDeleteHandler == null) {
            startEnhancedAutoDeleteService();
        }

        Log.d("BerandaActivity", "onResume completed - Level: " + userLevel);
    }
}