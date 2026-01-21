package com.example.mobile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "news_channel";
    private static final String PREF_NAME = "news_prefs";
    private static final String NEWS_KEY = "news_items";
    private static final String FILTER_KEY = "current_filter";
    private static final String NEWS_UPDATES_PREFS = "NewsUpdates";

    // UI Components
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerNews;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup chipGroupFilter;
    private TextView tvItemCount;

    // Data Lists
    private List<NewsItem> allNewsItems = new ArrayList<>();
    private List<NewsItem> filteredNewsItems = new ArrayList<>();

    // Filter State
    private String currentFilter = "all";

    // Preferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences newsUpdatePrefs;
    private Gson gson = new Gson();

    // Broadcast Receiver
    private BroadcastReceiver refreshReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        // Initialize components
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerNews = findViewById(R.id.recyclerNews);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        tvItemCount = findViewById(R.id.tvItemCount);

        newsUpdatePrefs = getSharedPreferences(NEWS_UPDATES_PREFS, MODE_PRIVATE);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        setupRefreshReceiver();
        createNotificationChannel();

        bottomNavigationView.setSelectedItemId(R.id.nav_news);

        // Navigation
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(NewsActivity.this, NewBeranda.class);
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
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Setup RecyclerView
        recyclerNews.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, filteredNewsItems);
        recyclerNews.setAdapter(newsAdapter);

        // Setup Filter Chips
        setupFilterChips();

        // Setup Refresh Button
        findViewById(R.id.btnRefresh).setOnClickListener(v -> refreshNewsData());

        // Load saved filter
        loadSavedFilter();

        // Load data
        loadNewsData();

        // Setup listeners
        swipeRefreshLayout.setOnRefreshListener(this::refreshNewsData);
        setupSwipeToDismiss();
        scheduleDailyCleanup();
        checkForPromoUpdates();

        setupFloatingActionButton();

        // Check for updates (diperbaiki)
        new Handler().postDelayed(this::checkForUpdatesFromPreferences, 2000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ================================
    // FILTER METHODS - CHIP VERSION
    // ================================

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (checkedIds.isEmpty()) return;

                int checkedId = checkedIds.get(0);

                // Perbaiki switch case dengan benar
                if (checkedId == R.id.chipAll) {
                    currentFilter = "all";
                } else if (checkedId == R.id.chipPromo) {
                    currentFilter = "promo";
                } else if (checkedId == R.id.chipHunian) {
                    currentFilter = "hunian";
                } else if (checkedId == R.id.chipProyek) {
                    currentFilter = "proyek";
                }

                // Simpan filter yang dipilih
                saveFilter();

                // Terapkan filter
                applyFilter();

                Log.d(TAG, "Chip filter applied: " + currentFilter);
            }
        });
    }

    private void applyFilter() {
        filteredNewsItems.clear();

        if ("all".equals(currentFilter)) {
            filteredNewsItems.addAll(allNewsItems);
        } else {
            for (NewsItem item : allNewsItems) {
                String itemType = item.getItemType();
                if (currentFilter.equals(itemType)) {
                    filteredNewsItems.add(item);
                }
            }
        }

        // Sort ulang berdasarkan timestamp
        Collections.sort(filteredNewsItems, (item1, item2) ->
                item2.getTimestamp().compareTo(item1.getTimestamp()));

        // Update adapter
        newsAdapter.updateData(filteredNewsItems);

        // Update item count
        updateItemCount();

        // Update empty state
        updateEmptyState();

        Log.d(TAG, "Filter applied: " + currentFilter +
                " | Total items: " + allNewsItems.size() +
                " | Filtered items: " + filteredNewsItems.size());
    }

    private void updateItemCount() {
        String countText = String.format(Locale.getDefault(),
                "Menampilkan %d dari %d item",
                filteredNewsItems.size(),
                allNewsItems.size());

        tvItemCount.setText(countText);
    }

    private void saveFilter() {
        SharedPreferences prefs = getSharedPreferences("NewsFilter", MODE_PRIVATE);
        prefs.edit().putString(FILTER_KEY, currentFilter).apply();

        // Update chip selection
        updateChipSelection();
    }

    private void loadSavedFilter() {
        SharedPreferences prefs = getSharedPreferences("NewsFilter", MODE_PRIVATE);
        currentFilter = prefs.getString(FILTER_KEY, "all");
        updateChipSelection();
    }

    private void updateChipSelection() {
        int chipId = R.id.chipAll;

        if ("all".equals(currentFilter)) {
            chipId = R.id.chipAll;
        } else if ("promo".equals(currentFilter)) {
            chipId = R.id.chipPromo;
        } else if ("hunian".equals(currentFilter)) {
            chipId = R.id.chipHunian;
        } else if ("proyek".equals(currentFilter)) {
            chipId = R.id.chipProyek;
        }

        Chip chip = findViewById(chipId);
        if (chip != null) {
            chip.setChecked(true);
        }
    }

    // ================================
    // MENU HANDLING
    // ================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            refreshNewsData();
            return true;
        } else if (id == R.id.menu_view_deleted) {
            Intent intent = new Intent(this, DeletedNewsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_settings) {
            showSettingsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        Toast.makeText(this, "Fitur pengaturan akan segera hadir", Toast.LENGTH_SHORT).show();
    }

    // ================================
    // DATA LOADING METHODS
    // ================================

    private void loadNewsData() {
        Log.d(TAG, "🔄 Loading all news data with filter: " + currentFilter);
        loadAllNewsDataSequential();
    }

    private void refreshNewsData() {
        Log.d(TAG, "🔄 Full refresh triggered");

        swipeRefreshLayout.setRefreshing(true);
        loadAllNewsDataSequential();
    }

    private void loadAllNewsDataSequential() {
        Log.d(TAG, "🔄 Loading all news data sequentially");

        runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(true);
            allNewsItems.clear();
            filteredNewsItems.clear();
            newsAdapter.notifyDataSetChanged();
            updateItemCount();
        });

        new Thread(() -> {
            try {
                List<NewsItem> loadedNewsItems = new ArrayList<>();

                // 1. Load PROMO data
                Log.d(TAG, "1️⃣ Loading PROMO histori...");
                loadPromoHistoriData(loadedNewsItems);

                // 2. Load HUNIAN data
                Log.d(TAG, "2️⃣ Loading HUNIAN histori...");
                loadHunianHistoriData(loadedNewsItems);

                // 3. Load PROYEK data
                Log.d(TAG, "3️⃣ Loading PROYEK histori...");
                loadProyekHistoriData(loadedNewsItems);

                // Simpan semua data dan terapkan filter
                runOnUiThread(() -> {
                    allNewsItems.clear();
                    allNewsItems.addAll(loadedNewsItems);

                    // Terapkan filter yang aktif
                    applyFilter();

                    // Simpan ke SharedPreferences untuk cache
                    saveNewsData();

                    swipeRefreshLayout.setRefreshing(false);

                    // Tampilkan toast sukses
                    if (!allNewsItems.isEmpty()) {
                        Toast.makeText(NewsActivity.this,
                                "Data berhasil dimuat: " + allNewsItems.size() + " item",
                                Toast.LENGTH_SHORT).show();
                    }

                    Log.d(TAG, "🎯 TOTAL ITEMS LOADED: " + allNewsItems.size());
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading all news data: " + e.getMessage());
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(NewsActivity.this, "Gagal memuat data berita", Toast.LENGTH_SHORT).show();
                    loadLocalNewsData();
                });
            }
        }).start();
    }

    private void loadPromoHistoriData(List<NewsItem> newsList) {
        try {
            Log.d(TAG, "🔄 Loading promo histori...");

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

            Call<NewsHistoriResponse> call = apiService.getNewsActivityData(50, 0, "true");
            Response<NewsHistoriResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                NewsHistoriResponse historiResponse = response.body();
                Log.d(TAG, "📊 Promo Response - Success: " + historiResponse.isSuccess() +
                        ", Data Count: " + (historiResponse.hasData() ? historiResponse.getDataCount() : 0));

                if (historiResponse.isSuccess() && historiResponse.hasData()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    for (NewsHistoriItem item : historiResponse.getData()) {
                        // Hanya tampilkan "Ditambahkan" atau "Diubah"
                        if ("Ditambahkan".equals(item.getStatus()) || "Diubah".equals(item.getStatus())) {
                            Date timestamp;
                            try {
                                timestamp = format.parse(item.getTimestamp());
                            } catch (Exception e) {
                                timestamp = new Date();
                            }

                            // Validasi gambar
                            String imageData = null;
                            if (item.getImage_base64() != null && !item.getImage_base64().isEmpty()) {
                                String rawImageData = item.getImage_base64().trim();
                                if (rawImageData.length() >= 100 && !rawImageData.equals("null")) {
                                    imageData = rawImageData;
                                }
                            }

                            NewsItem newsItem = new NewsItem(
                                    item.getId_news(),
                                    item.getTitle(),
                                    item.getPenginput(),
                                    item.getStatus(),
                                    timestamp,
                                    imageData,
                                    item.getPromo_id(),
                                    item.getKadaluwarsa(),
                                    "promo"
                            );

                            newsList.add(newsItem);
                            Log.d(TAG, "✅ Added promo: " + item.getTitle() + " | Status: " + item.getStatus());
                        }
                    }
                    Log.d(TAG, "✅ Loaded " + newsList.size() + " promo items");
                }
            } else {
                Log.e(TAG, "❌ Promo response not successful: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading promo histori: " + e.getMessage());
        }
    }

    private void loadHunianHistoriData(List<NewsItem> newsList) {
        try {
            Log.d(TAG, "🔄 Loading hunian histori...");

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

            Call<HunianHistoriResponse> call = apiService.getHunianHistori(50, 0, "true");
            Response<HunianHistoriResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                HunianHistoriResponse historiResponse = response.body();

                if (historiResponse.isSuccess() && historiResponse.getData() != null) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    Map<Integer, NewsItem> hunianMap = new HashMap<>();

                    for (HunianHistoriItem item : historiResponse.getData()) {
                        if ("Ditambahkan".equals(item.getStatus()) || "Diubah".equals(item.getStatus())) {

                            if (hunianMap.containsKey(item.getHunianId())) {
                                NewsItem existing = hunianMap.get(item.getHunianId());

                                if (existing.getImageUrl() == null || existing.getImageUrl().isEmpty()) {
                                    Date timestamp;
                                    try {
                                        timestamp = format.parse(item.getTimestamp());
                                    } catch (Exception e) {
                                        timestamp = new Date();
                                    }

                                    String title = "Hunian: " + item.getNamaHunian() +
                                            " (Proyek: " + item.getNamaProyek() + ")";

                                    String imageData = null;
                                    if (item.getImageData() != null && !item.getImageData().isEmpty()) {
                                        String rawImageData = item.getImageData().trim();
                                        if (rawImageData.length() >= 30 &&
                                                !rawImageData.equals("null") &&
                                                !rawImageData.equals("NULL") &&
                                                !rawImageData.startsWith("data:image")) {
                                            imageData = rawImageData;
                                        }
                                    }

                                    NewsItem newsItem = new NewsItem(
                                            item.getIdNewsHunian(),
                                            title,
                                            item.getPenginput(),
                                            item.getStatus(),
                                            timestamp,
                                            imageData,
                                            item.getHunianId(),
                                            "",
                                            "hunian"
                                    );

                                    hunianMap.put(item.getHunianId(), newsItem);
                                    Log.d(TAG, "🔄 Updated hunian with image: " + item.getNamaHunian());
                                }
                            } else {
                                Date timestamp;
                                try {
                                    timestamp = format.parse(item.getTimestamp());
                                } catch (Exception e) {
                                    timestamp = new Date();
                                }

                                String title = "Hunian: " + item.getNamaHunian() +
                                        " (Proyek: " + item.getNamaProyek() + ")";

                                String imageData = null;
                                if (item.getImageData() != null && !item.getImageData().isEmpty()) {
                                    String rawImageData = item.getImageData().trim();
                                    if (rawImageData.length() >= 30 &&
                                            !rawImageData.equals("null") &&
                                            !rawImageData.equals("NULL") &&
                                            !rawImageData.startsWith("data:image")) {
                                        imageData = rawImageData;
                                    }
                                }

                                NewsItem newsItem = new NewsItem(
                                        item.getIdNewsHunian(),
                                        title,
                                        item.getPenginput(),
                                        item.getStatus(),
                                        timestamp,
                                        imageData,
                                        item.getHunianId(),
                                        "",
                                        "hunian"
                                );

                                hunianMap.put(item.getHunianId(), newsItem);
                                Log.d(TAG, "✅ Added new hunian to map: " + item.getNamaHunian() +
                                        " | Image: " + (imageData != null ? "YES" : "NO"));
                            }
                        }
                    }

                    newsList.addAll(hunianMap.values());
                    Log.d(TAG, "✅ Loaded " + hunianMap.size() + " unique hunian items");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading hunian histori: " + e.getMessage());
        }
    }

    private void loadProyekHistoriData(List<NewsItem> newsList) {
        try {
            Log.d(TAG, "🔄 Loading proyek histori...");

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

            Call<ProyekHistoriResponse> call = apiService.getProyekHistori(50, 0, "true");
            Response<ProyekHistoriResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                ProyekHistoriResponse historiResponse = response.body();
                Log.d(TAG, "📊 Proyek Histori - Success: " + historiResponse.isSuccess() +
                        ", Data Count: " + (historiResponse.getData() != null ? historiResponse.getData().size() : 0));

                if (historiResponse.isSuccess() && historiResponse.getData() != null) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    for (ProyekHistoriItem item : historiResponse.getData()) {
                        if ("Ditambahkan".equals(item.getStatus()) || "Diubah".equals(item.getStatus())) {

                            Date timestamp;
                            try {
                                timestamp = format.parse(item.getTimestamp());
                            } catch (Exception e) {
                                timestamp = new Date();
                            }

                            String title = "Proyek: " + item.getNamaProyek();
                            if (item.getLokasiProyek() != null && !item.getLokasiProyek().isEmpty()) {
                                title += " (" + item.getLokasiProyek() + ")";
                            }

                            String imageData = null;
                            if (item.getImageData() != null && !item.getImageData().isEmpty()) {
                                String rawImageData = item.getImageData().trim();
                                if (rawImageData.length() >= 50 &&
                                        !rawImageData.equals("null") &&
                                        !rawImageData.equals("NULL")) {
                                    imageData = rawImageData;
                                }
                            }

                            NewsItem newsItem = new NewsItem(
                                    item.getIdNewsProyek(),
                                    title,
                                    item.getPenginput(),
                                    item.getStatus(),
                                    timestamp,
                                    imageData,
                                    item.getProyekId(),
                                    "",
                                    "proyek"
                            );

                            newsList.add(newsItem);
                            Log.d(TAG, "✅ Added proyek to news: " + item.getNamaProyek() +
                                    " | Status: " + item.getStatus() +
                                    " | Image: " + (imageData != null ? "YES" : "NO"));
                        }
                    }
                    Log.d(TAG, "✅ Loaded " + newsList.size() + " proyek items for news");
                }
            } else {
                Log.e(TAG, "❌ Proyek histori response error: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading proyek histori: " + e.getMessage());
        }
    }

    // ================================
    // LOCAL DATA METHODS
    // ================================

    private void saveNewsData() {
        try {
            String json = gson.toJson(allNewsItems);
            sharedPreferences.edit().putString(NEWS_KEY, json).apply();
            Log.d(TAG, "💾 Saved " + allNewsItems.size() + " items to SharedPreferences");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving news data: " + e.getMessage());
        }
    }

    private void loadLocalNewsData() {
        try {
            String json = sharedPreferences.getString(NEWS_KEY, null);
            if (json != null) {
                Type type = new TypeToken<List<NewsItem>>(){}.getType();
                List<NewsItem> savedNews = gson.fromJson(json, type);
                if (savedNews != null && !savedNews.isEmpty()) {
                    allNewsItems.clear();
                    allNewsItems.addAll(savedNews);

                    // Terapkan filter
                    applyFilter();

                    Log.d(TAG, "📁 Loaded " + allNewsItems.size() + " items from SharedPreferences");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading local news data: " + e.getMessage());
        }
    }

    // ================================
    // HELPER METHODS
    // ================================

    private void setupFloatingActionButton() {
        FloatingActionButton fabDeletedNews = findViewById(R.id.fabDeletedNews);

        if (fabDeletedNews == null) {
            Log.e(TAG, "❌ FAB tidak ditemukan di layout!");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userLevel = prefs.getString("level", "Operator");

        Log.d(TAG, "User Level: " + userLevel);

        if ("Admin".equals(userLevel)) {
            fabDeletedNews.setVisibility(View.VISIBLE);
            fabDeletedNews.setOnClickListener(v -> {
                Log.d(TAG, "🎯 FAB diklik, membuka DeletedNewsActivity");

                try {
                    Intent intent = new Intent(NewsActivity.this, DeletedNewsActivity.class);
                    startActivity(intent);

                    // Tambahkan animasi transisi (opsional)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                    Log.d(TAG, "✅ Berhasil membuka DeletedNewsActivity");
                } catch (Exception e) {
                    Log.e(TAG, "❌ Gagal membuka DeletedNewsActivity: " + e.getMessage());
                    Toast.makeText(NewsActivity.this,
                            "Gagal membuka halaman berita terhapus",
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Tambahkan efek hover/ripple (opsional)
            fabDeletedNews.setOnLongClickListener(v -> {
                Toast.makeText(NewsActivity.this,
                        "Lihat berita yang telah dihapus",
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            Log.d(TAG, "✅ FAB Deleted News ditampilkan untuk Admin");
        } else {
            fabDeletedNews.setVisibility(View.GONE);
            Log.d(TAG, "❌ FAB Deleted News disembunyikan untuk level: " + userLevel);
        }
    }

    private void updateEmptyState() {
        runOnUiThread(() -> {
            TextView tvEmptyState = findViewById(R.id.tvEmptyState);
            if (tvEmptyState != null) {
                if (filteredNewsItems.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);

                    // Custom message berdasarkan filter
                    String message = "Tidak ada berita terbaru";

                    if (!"all".equals(currentFilter)) {
                        if ("promo".equals(currentFilter)) {
                            message = "Belum ada promo terbaru";
                        } else if ("hunian".equals(currentFilter)) {
                            message = "Belum ada hunian terbaru";
                        } else if ("proyek".equals(currentFilter)) {
                            message = "Belum ada proyek terbaru";
                        }
                    }

                    tvEmptyState.setText(message);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }
        });
    }

    // ================================
    // UPDATE CHECK METHODS
    // ================================

    private void checkForUpdatesFromPreferences() {
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        long lastUpdateTime = newsPrefs.getLong("last_update_time", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdateTime < 5 * 60 * 1000) {
            int promoId = newsPrefs.getInt("last_updated_promo_id", -1);
            String title = newsPrefs.getString("last_updated_title", "");
            String inputter = newsPrefs.getString("last_updated_inputter", "");
            String imageData = newsPrefs.getString("last_updated_image", "");

            if (promoId != -1 && !title.isEmpty()) {
                Log.d(TAG, "🔄 Found recent update in preferences: " + title);
                // handleIncomingUpdate(promoId, imageData, title, inputter, "Diubah");
            }
        }
    }

    private void checkForPromoUpdates() {
        long lastCheckTime = getLastNewsCheckTime();
        long currentTime = System.currentTimeMillis();

        Log.d(TAG, "=== CHECKING PROMO UPDATES ===");

        synchronized (this) {
            checkForPromoAdditions();
            checkForPromoUpdatesFromPrefs(lastCheckTime);
            checkForPromoDeletionsFromPrefs(lastCheckTime);
        }

        saveLastNewsCheckTime(currentTime);
    }

    private void checkForPromoAdditions() {
        refreshNewsData();
    }

    private void checkForPromoUpdatesFromPrefs(long lastCheckTime) {
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        long lastUpdateTime = newsPrefs.getLong("last_update_time", 0);

        if (lastUpdateTime > lastCheckTime) {
            int promoId = newsPrefs.getInt("last_updated_promo_id", -1);
            String status = newsPrefs.getString("last_updated_status", "Diubah");

            if (promoId != -1) {
                clearUpdateDataFromPrefs();
            }
        }
    }

    private void checkForPromoDeletionsFromPrefs(long lastCheckTime) {
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        long lastDeleteTime = newsPrefs.getLong("last_delete_time", 0);

        if (lastDeleteTime > lastCheckTime) {
            String deletedTitle = newsPrefs.getString("last_deleted_title", "");
            String deletedInputter = newsPrefs.getString("last_deleted_inputter", "");

            if (!deletedTitle.isEmpty()) {
                clearDeleteData();
            }
        }
    }

    private void clearUpdateDataFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        prefs.edit()
                .remove("last_updated_promo_id")
                .remove("last_updated_title")
                .remove("last_updated_inputter")
                .remove("last_updated_status")
                .remove("last_updated_image")
                .remove("last_update_time")
                .apply();
    }

    private void clearDeleteData() {
        SharedPreferences prefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        prefs.edit()
                .remove("last_delete_time")
                .remove("last_deleted_title")
                .remove("last_deleted_inputter")
                .remove("last_deleted_status")
                .apply();
    }

    private long getLastNewsCheckTime() {
        return getSharedPreferences("NewsActivityCheck", MODE_PRIVATE)
                .getLong("last_news_check_time", 0);
    }

    private void saveLastNewsCheckTime(long time) {
        getSharedPreferences("NewsActivityCheck", MODE_PRIVATE)
                .edit()
                .putLong("last_news_check_time", time)
                .apply();
    }

    // ================================
    // EXISTING METHODS (tetap sama)
    // ================================

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void setupRefreshReceiver() {
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if ("REFRESH_NEWS_DATA".equals(action)) {
                    Log.d(TAG, "📢 Received refresh broadcast");
                    refreshNewsDataImmediately();
                }
            }
        };

        IntentFilter filter = new IntentFilter("REFRESH_NEWS_DATA");

        // Perbaikan untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Gunakan RECEIVER_NOT_EXPORTED karena ini broadcast internal
            registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(refreshReceiver, filter);
        }
    }

    private void refreshNewsDataImmediately() {
        Log.d(TAG, "⚡ Immediate refresh triggered");
        runOnUiThread(this::refreshNewsData);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "News Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for news notifications");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify(new Random().nextInt(), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission denied", e);
        }
    }

    private void setupSwipeToDismiss() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < filteredNewsItems.size()) {
                    NewsItem removedItem = filteredNewsItems.get(position);

                    // Hapus dari all items
                    Iterator<NewsItem> iterator = allNewsItems.iterator();
                    while (iterator.hasNext()) {
                        NewsItem item = iterator.next();
                        if (item.getId() == removedItem.getId()) {
                            iterator.remove();
                            break;
                        }
                    }

                    // Hapus dari filtered list
                    newsAdapter.removeItem(position);

                    // Update item count
                    updateItemCount();

                    // Simpan perubahan
                    saveNewsData();

                    Toast.makeText(NewsActivity.this, "Berita dihapus", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerNews);
    }

    private void scheduleDailyCleanup() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NewsCleanupReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    // ================================
    // LIFECYCLE METHODS
    // ================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "🔄 onResume - Refreshing news data");

        // Refresh data setiap kali activity kembali ke foreground
        new Handler().postDelayed(this::loadAllNewsDataSequential, 500);
    }
}