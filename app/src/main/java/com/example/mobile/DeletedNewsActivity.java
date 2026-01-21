package com.example.mobile;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeletedNewsActivity extends AppCompatActivity {

    private static final String TAG = "DeletedNewsActivity";
    private static final String PREF_NAME = "deleted_news_prefs";
    private static final String DELETED_NEWS_KEY = "deleted_news_items";
    private static final String FILTER_KEY = "deleted_filter";

    // UI Components
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerDeletedNews;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChipGroup chipGroupFilter;
    private TextView tvItemCount;
    private TextView tvEmptyState;

    // Data Lists
    private List<NewsItem> allDeletedItems = new ArrayList<>();
    private List<NewsItem> filteredDeletedItems = new ArrayList<>();

    // Filter State
    private String currentFilter = "all"; // "all", "deleted_promo", "expired_promo", "deleted_hunian", "deleted_proyek"

    // Preferences
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    // Broadcast Receiver
    private BroadcastReceiver refreshReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deleted_news);

        // Initialize components
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerDeletedNews = findViewById(R.id.recyclerDeletedNews);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        setupRefreshReceiver();
        setupBottomNavigation();
        setupToolbar();

        // Setup RecyclerView
        recyclerDeletedNews.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, filteredDeletedItems);
        recyclerDeletedNews.setAdapter(newsAdapter);

        // Setup Filter Chips
        setupFilterChips();

        // Setup Refresh Button
        findViewById(R.id.btnRefresh).setOnClickListener(v -> refreshData());

        // Setup Swipe to Dismiss
        setupSwipeToDismiss();

        // Load saved filter
        loadSavedFilter();

        // Load data
        loadDeletedNewsData();

        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(DeletedNewsActivity.this, NewsActivity.class);
            startActivity(intent);
            finish();
        });
        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ================================
    // SETUP METHODS
    // ================================

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_news);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, NewBeranda.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_folder) {
                Intent intent = new Intent(this, LihatDataActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_news) {
                Intent intent = new Intent(this, NewsActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deleted_news_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            refreshData();
            return true;
        } else if (id == R.id.menu_back_to_news) {
            Intent intent = new Intent(this, NewsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ================================
    // FILTER METHODS
    // ================================

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (checkedIds.isEmpty()) return;

                int checkedId = checkedIds.get(0);

                if (checkedId == R.id.chipAll) {
                    currentFilter = "all";
                } else if (checkedId == R.id.chipDeletedPromo) {
                    currentFilter = "deleted_promo";
                } else if (checkedId == R.id.chipExpiredPromo) {
                    currentFilter = "expired_promo";
                } else if (checkedId == R.id.chipDeletedHunian) {
                    currentFilter = "deleted_hunian";
                } else if (checkedId == R.id.chipDeletedProyek) {
                    currentFilter = "deleted_proyek";
                }

                saveFilter();
                applyFilter();

                Log.d(TAG, "Chip filter applied: " + currentFilter);
            }
        });
    }

    private void applyFilter() {
        filteredDeletedItems.clear();

        if ("all".equals(currentFilter)) {
            filteredDeletedItems.addAll(allDeletedItems);
        } else {
            for (NewsItem item : allDeletedItems) {
                String status = item.getStatus();
                String itemType = item.getItemType();

                if ("deleted_promo".equals(currentFilter) &&
                        "Dihapus".equals(status) && "promo".equals(itemType)) {
                    filteredDeletedItems.add(item);
                } else if ("expired_promo".equals(currentFilter) &&
                        "Kadaluwarsa".equals(status) && "promo".equals(itemType)) {
                    filteredDeletedItems.add(item);
                } else if ("deleted_hunian".equals(currentFilter) &&
                        "Dihapus".equals(status) && "hunian".equals(itemType)) {
                    filteredDeletedItems.add(item);
                } else if ("deleted_proyek".equals(currentFilter) &&
                        "Dihapus".equals(status) && "proyek".equals(itemType)) {
                    filteredDeletedItems.add(item);
                }
            }
        }

        // Sort ulang berdasarkan timestamp
        Collections.sort(filteredDeletedItems, (item1, item2) ->
                item2.getTimestamp().compareTo(item1.getTimestamp()));

        // Update adapter
        newsAdapter.updateData(filteredDeletedItems);

        // Update item count
        updateItemCount();

        // Update empty state
        updateEmptyState();

        Log.d(TAG, "Filter applied: " + currentFilter +
                " | Total items: " + allDeletedItems.size() +
                " | Filtered items: " + filteredDeletedItems.size());
    }

    private void updateItemCount() {
        String countText = String.format(Locale.getDefault(),
                "Menampilkan %d dari %d item",
                filteredDeletedItems.size(),
                allDeletedItems.size());

        tvItemCount.setText(countText);
    }

    private void updateEmptyState() {
        if (filteredDeletedItems.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);

            String message = "Tidak ada arsip berita";
            if (!"all".equals(currentFilter)) {
                if ("deleted_promo".equals(currentFilter)) {
                    message = "Tidak ada promo yang dihapus";
                } else if ("expired_promo".equals(currentFilter)) {
                    message = "Tidak ada promo yang kadaluwarsa";
                } else if ("deleted_hunian".equals(currentFilter)) {
                    message = "Tidak ada hunian yang dihapus";
                } else if ("deleted_proyek".equals(currentFilter)) {
                    message = "Tidak ada proyek yang dihapus";
                }
            }

            tvEmptyState.setText(message);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void saveFilter() {
        SharedPreferences prefs = getSharedPreferences("DeletedNewsFilter", MODE_PRIVATE);
        prefs.edit().putString(FILTER_KEY, currentFilter).apply();
        updateChipSelection();
    }

    private void loadSavedFilter() {
        SharedPreferences prefs = getSharedPreferences("DeletedNewsFilter", MODE_PRIVATE);
        currentFilter = prefs.getString(FILTER_KEY, "all");
        updateChipSelection();
    }

    private void updateChipSelection() {
        int chipId = R.id.chipAll;

        if ("all".equals(currentFilter)) {
            chipId = R.id.chipAll;
        } else if ("deleted_promo".equals(currentFilter)) {
            chipId = R.id.chipDeletedPromo;
        } else if ("expired_promo".equals(currentFilter)) {
            chipId = R.id.chipExpiredPromo;
        } else if ("deleted_hunian".equals(currentFilter)) {
            chipId = R.id.chipDeletedHunian;
        } else if ("deleted_proyek".equals(currentFilter)) {
            chipId = R.id.chipDeletedProyek;
        }

        Chip chip = findViewById(chipId);
        if (chip != null) {
            chip.setChecked(true);
        }
    }

    // ================================
    // DATA LOADING METHODS
    // ================================

    private void loadDeletedNewsData() {
        Log.d(TAG, "🔄 Loading deleted news data...");
        swipeRefreshLayout.setRefreshing(true);

        new Thread(() -> {
            try {
                List<NewsItem> loadedItems = new ArrayList<>();

                // Load data dari API
                loadDataFromAPI(loadedItems);

                runOnUiThread(() -> {
                    allDeletedItems.clear();
                    allDeletedItems.addAll(loadedItems);

                    // Terapkan filter
                    applyFilter();

                    // Simpan ke SharedPreferences
                    saveDeletedNewsData();

                    swipeRefreshLayout.setRefreshing(false);

                    if (!allDeletedItems.isEmpty()) {
                        Toast.makeText(DeletedNewsActivity.this,
                                "Data berhasil dimuat: " + allDeletedItems.size() + " item",
                                Toast.LENGTH_SHORT).show();
                    }

                    Log.d(TAG, "🎯 TOTAL DELETED ITEMS LOADED: " + allDeletedItems.size());
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading deleted news data: " + e.getMessage());
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(DeletedNewsActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    loadLocalDeletedNewsData();
                });
            }
        }).start();
    }

    private void loadDataFromAPI(List<NewsItem> loadedItems) {
        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

            // 1. Load deleted and expired promo
            loadDeletedAndExpiredPromo(apiService, loadedItems);

            // 2. Load deleted hunian
            loadDeletedHunian(apiService, loadedItems);

            // 3. Load deleted proyek
            loadDeletedProyek(apiService, loadedItems);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading from API: " + e.getMessage());
        }
    }

    private void loadDeletedAndExpiredPromo(ApiService apiService, List<NewsItem> loadedItems) {
        try {
            Call<NewsHistoriResponse> call = apiService.getDeletedNews(200, 0, "true");
            Response<NewsHistoriResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                NewsHistoriResponse historiResponse = response.body();

                if (historiResponse.isSuccess() && historiResponse.hasData()) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    for (NewsHistoriItem item : historiResponse.getData()) {
                        // Hanya ambil item dengan status Dihapus atau Kadaluwarsa
                        if ("Dihapus".equals(item.getStatus()) || "Kadaluwarsa".equals(item.getStatus())) {
                            Date timestamp;
                            try {
                                timestamp = format.parse(item.getTimestamp());
                            } catch (Exception e) {
                                timestamp = new Date();
                            }

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

                            loadedItems.add(newsItem);
                            Log.d(TAG, "✅ Added deleted/expired promo: " + item.getTitle());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading deleted promo: " + e.getMessage());
        }
    }

    private void loadDeletedHunian(ApiService apiService, List<NewsItem> loadedItems) {
        try {
            Call<List<DeletedHunianResponse>> call = apiService.getDeletedHunianHistori();
            Response<List<DeletedHunianResponse>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                for (DeletedHunianResponse hunian : response.body()) {
                    if ("Dihapus".equals(hunian.getStatus())) {
                        Date timestamp;
                        try {
                            timestamp = format.parse(hunian.getTimestamp());
                        } catch (Exception e) {
                            timestamp = new Date();
                        }

                        NewsItem item = new NewsItem(
                                hunian.getIdNewsHunian(),
                                "Hunian: " + hunian.getNamaHunian() + " (Proyek: " + hunian.getNamaProyek() + ")",
                                hunian.getPenginput(),
                                hunian.getStatus(),
                                timestamp,
                                hunian.getImageData(),
                                0,
                                "",
                                "hunian"
                        );

                        loadedItems.add(item);
                        Log.d(TAG, "✅ Added deleted hunian: " + hunian.getNamaHunian());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading deleted hunian: " + e.getMessage());
        }
    }

    private void loadDeletedProyek(ApiService apiService, List<NewsItem> loadedItems) {
        try {
            Call<List<DeletedProyekResponse>> call = apiService.getDeletedProyekHistori();
            Response<List<DeletedProyekResponse>> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                for (DeletedProyekResponse proyek : response.body()) {
                    if ("Dihapus".equals(proyek.getStatus())) {
                        Date timestamp;
                        try {
                            timestamp = format.parse(proyek.getTimestamp());
                        } catch (Exception e) {
                            timestamp = new Date();
                        }

                        String title = "Proyek: " + proyek.getNamaProyek();
                        if (proyek.getLokasiProyek() != null && !proyek.getLokasiProyek().isEmpty()) {
                            title += " (" + proyek.getLokasiProyek() + ")";
                        }

                        String imageData = null;
                        if (proyek.getImageData() != null && !proyek.getImageData().isEmpty()) {
                            String rawImageData = proyek.getImageData().trim();
                            if (rawImageData.length() >= 50 && !rawImageData.equalsIgnoreCase("null")) {
                                imageData = rawImageData;
                            }
                        }

                        NewsItem item = new NewsItem.Builder()
                                .setId(proyek.getIdNewsProyek())
                                .setTitle(title)
                                .setPenginput(proyek.getPenginput() != null ? proyek.getPenginput() : "System")
                                .setStatus(proyek.getStatus())
                                .setTimestamp(timestamp)
                                .setImageUrl(imageData)
                                .setPromoId(0)
                                .setKadaluwarsa("")
                                .setItemType("proyek")
                                .build();

                        loadedItems.add(item);
                        Log.d(TAG, "✅ Added deleted proyek: " + proyek.getNamaProyek());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading deleted proyek: " + e.getMessage());
        }
    }

    private void refreshData() {
        Log.d(TAG, "🔄 Refresh triggered");
        loadDeletedNewsData();
    }

    // ================================
    // LOCAL DATA METHODS
    // ================================

    private void saveDeletedNewsData() {
        try {
            String json = gson.toJson(allDeletedItems);
            sharedPreferences.edit().putString(DELETED_NEWS_KEY, json).apply();
            Log.d(TAG, "💾 Saved " + allDeletedItems.size() + " items to SharedPreferences");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving deleted news data: " + e.getMessage());
        }
    }

    private void loadLocalDeletedNewsData() {
        try {
            String json = sharedPreferences.getString(DELETED_NEWS_KEY, null);
            if (json != null) {
                Type type = new TypeToken<List<NewsItem>>() {}.getType();
                List<NewsItem> savedNews = gson.fromJson(json, type);
                if (savedNews != null && !savedNews.isEmpty()) {
                    allDeletedItems.clear();
                    allDeletedItems.addAll(savedNews);
                    applyFilter();
                    Log.d(TAG, "📁 Loaded " + allDeletedItems.size() + " items from SharedPreferences");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading local deleted news data: " + e.getMessage());
        }
    }

    // ================================
    // HELPER METHODS
    // ================================

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
                if (position >= 0 && position < filteredDeletedItems.size()) {
                    NewsItem removedItem = filteredDeletedItems.get(position);

                    // Hapus dari all items
                    Iterator<NewsItem> iterator = allDeletedItems.iterator();
                    while (iterator.hasNext()) {
                        NewsItem item = iterator.next();
                        if (item.getId() == removedItem.getId()) {
                            iterator.remove();
                            break;
                        }
                    }

                    // Hapus dari filtered list
                    newsAdapter.removeItem(position);

                    // Update UI
                    updateItemCount();
                    updateEmptyState();

                    // Simpan perubahan
                    saveDeletedNewsData();

                    Toast.makeText(DeletedNewsActivity.this, "Item dihapus dari arsip", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerDeletedNews);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void setupRefreshReceiver() {
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("REFRESH_DELETED_NEWS_DATA".equals(action)) {
                    Log.d(TAG, "📢 Received refresh broadcast");
                    refreshData();
                }
            }
        };

        IntentFilter filter = new IntentFilter("REFRESH_DELETED_NEWS_DATA");

        // SOLUSI: Tambahkan penanganan untuk Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+): HARUS spesifikasikan flag ekspor
            // Gunakan RECEIVER_NOT_EXPORTED karena broadcast hanya internal aplikasi
            registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // Android versi lama
            registerReceiver(refreshReceiver, filter);
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
        Log.d(TAG, "🔄 onResume - Refreshing deleted news");
        new Handler().postDelayed(this::loadDeletedNewsData, 500);
    }
}