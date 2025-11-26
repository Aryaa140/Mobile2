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
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
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
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "news_channel";
    private static final String PREF_NAME = "news_prefs";
    private static final String NEWS_KEY = "news_items";
    private final Object updateLock = new Object();
    private boolean isFirstLoad = true;
    MaterialToolbar topAppBar;
    BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerNews;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsItems = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences sharedPreferences;

    private SharedPreferences newsUpdatePrefs;
    private static final String NEWS_UPDATES_PREFS = "NewsUpdates";
    private Gson gson = new Gson();

    private BroadcastReceiver refreshReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        setupRefreshReceiver();

        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerNews = findViewById(R.id.recyclerNews);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        newsUpdatePrefs = getSharedPreferences(NEWS_UPDATES_PREFS, MODE_PRIVATE);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        createNotificationChannel();

        bottomNavigationView.setSelectedItemId(R.id.nav_news);

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

        recyclerNews.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, newsItems);
        recyclerNews.setAdapter(newsAdapter);

        loadNewsData();
        swipeRefreshLayout.setOnRefreshListener(this::refreshNewsData);
        setupSwipeToDismiss();
        scheduleDailyCleanup();
        checkForPromoUpdates();

        setupFloatingActionButton();

        // PERBAIKAN: Juga cek update yang mungkin terlewat
        new Handler().postDelayed(() -> {
            checkForUpdatesFromPreferences();
        }, 2000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupFloatingActionButton() {
        FloatingActionButton fabDeletedNews = findViewById(R.id.fabDeletedNews);

        // ✅ PERBAIKAN: Gunakan SharedPreferences yang sama dengan NewBeranda
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String userLevel = prefs.getString("level", "Operator");

        Log.d("NewsActivity", "User Level: " + userLevel);

        if ("Admin".equals(userLevel)) {
            fabDeletedNews.setVisibility(View.VISIBLE);
            fabDeletedNews.setOnClickListener(v -> {
                Intent intent = new Intent(NewsActivity.this, DeletedNewsActivity.class);
                startActivity(intent);
            });
            Log.d("NewsActivity", "✅ FAB Deleted News ditampilkan untuk Admin");
        } else {
            fabDeletedNews.setVisibility(View.GONE);
            Log.d("NewsActivity", "❌ FAB Deleted News disembunyikan untuk level: " + userLevel);
        }
    }
    // DI NewsActivity.java - PERBAIKI setupRefreshReceiver
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void setupRefreshReceiver() {
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if ("REFRESH_NEWS_DATA".equals(action)) {
                    String source = intent.getStringExtra("SOURCE");
                    String actionType = intent.getStringExtra("ACTION");

                    // ✅ PERBAIKAN: Handle different actions
                    if ("PROMO_DELETED".equals(actionType)) {
                        String promoTitle = intent.getStringExtra("PROMO_TITLE");
                        String penginput = intent.getStringExtra("PENGINPUT");
                        String imageData = intent.getStringExtra("IMAGE_DATA");

                        Log.d("NewsActivity", "🗑️ Received DELETE broadcast: " + promoTitle);
                        processPromoDelete(promoTitle, penginput, imageData);

                    } else if ("NEW_PROMO_ADDED".equals(actionType)) {
                        int promoId = intent.getIntExtra("PROMO_ID", -1);
                        String updatedImage = intent.getStringExtra("IMAGE_DATA");
                        String updatedTitle = intent.getStringExtra("PROMO_TITLE");
                        String updatedUser = intent.getStringExtra("PENGINPUT");
                        String kadaluwarsa = intent.getStringExtra("KADALUWARSA");
                        String status = intent.getStringExtra("STATUS");

                        Log.d("NewsActivity", "📢 Broadcast received - Source: " + source +
                                ", PromoID: " + promoId + ", Status: " + status +
                                ", Kadaluwarsa: " + kadaluwarsa);

                        processPromoUpdateWithKadaluwarsa(promoId, status, updatedImage, updatedTitle, updatedUser, kadaluwarsa);

                    } else {
                        // Fallback: refresh biasa
                        refreshNewsDataImmediately();
                    }
                }
            }
        };

        try {
            IntentFilter filter = new IntentFilter("REFRESH_NEWS_DATA");
            registerReceiver(refreshReceiver, filter);
            Log.d(TAG, "✅ Broadcast receiver registered for all actions");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error registering receiver: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: Process delete dengan benar
    private void processPromoDelete(String promoTitle, String penginput, String imageData) {
        Log.d("NewsActivity", "🗑️ PROCESSING DELETE: " + promoTitle);

        // Cari dan hapus item dari NewsActivity
        Iterator<NewsItem> iterator = newsItems.iterator();
        boolean found = false;

        while (iterator.hasNext()) {
            NewsItem item = iterator.next();
            if (item.getTitle().equals(promoTitle)) {
                iterator.remove();
                found = true;
                Log.d("NewsActivity", "✅ Removed from NewsActivity: " + promoTitle);
                break;
            }
        }

        if (found) {
            newsAdapter.notifyDataSetChanged();
            updateEmptyState();
            showNotificationForDelete(promoTitle, penginput);
        } else {
            Log.d("NewsActivity", "⚠️ Item not found in NewsActivity: " + promoTitle);
        }
    }

    // ✅ METHOD BARU: Process update dengan kadaluwarsa
    private void processPromoUpdateWithKadaluwarsa(int promoId, String status, String imageData, String title, String user, String kadaluwarsa) {
        Log.d("NewsActivity", "🎯 Processing update with kadaluwarsa - ID: " + promoId + ", Kadaluwarsa: " + kadaluwarsa);

        // Cari item existing
        NewsItem existingItem = findNewsItemByPromoId(promoId);

        if (existingItem != null) {
            // Update existing item
            existingItem.setStatus("Diubah");
            existingItem.setTimestamp(new Date());
            existingItem.setTitle(title != null ? title : existingItem.getTitle());
            existingItem.setPenginput(user != null ? user : existingItem.getPenginput());
            existingItem.setImageUrl(imageData);
            existingItem.setKadaluwarsa(kadaluwarsa); // ✅ SET KADALUWARSA

            // Pindah ke atas
            newsItems.remove(existingItem);
            newsItems.add(0, existingItem);

            Log.d("NewsActivity", "✅ Updated existing item with kadaluwarsa: " + existingItem.getTitle());
        } else {
            // Buat item baru dengan kadaluwarsa
            createNewNewsItemFromUpdateWithKadaluwarsa(promoId, title, user, imageData, kadaluwarsa);
        }

        sortAndSaveData();
        showNotificationForUpdate(title != null ? title : "Promo", user);
    }

    // ✅ METHOD BARU: Buat item baru dengan kadaluwarsa
    private void createNewNewsItemFromUpdateWithKadaluwarsa(int promoId, String title, String user, String imageData, String kadaluwarsa) {
        Log.d("NewsActivity", "🔄 Creating new item from update with kadaluwarsa: " + title);

        NewsItem newItem = new NewsItem(
                generateNewId(),
                title,
                user,
                "Diubah",
                new Date(),
                isValidImageForNewsDisplay(imageData) ? imageData : null,
                promoId,
                kadaluwarsa // ✅ SET KADALUWARSA
        );

        newsItems.add(0, newItem);
        sortAndSaveData();

        Log.d("NewsActivity", "✅ Created new item from update with kadaluwarsa: " + title);
    }


    // ✅ METHOD BARU: Buat item baru dari update
    private void createNewNewsItemFromUpdate(int promoId, String title, String user, String imageData) {
        Log.d("NewsActivity", "🔄 Creating new item from update: " + title);

        NewsItem newItem = new NewsItem(
                generateNewId(),
                title,
                user,
                "Diubah",
                new Date(),
                isValidImageForNewsDisplay(imageData) ? imageData : null,
                promoId
        );

        newsItems.add(0, newItem);
        sortAndSaveData();

        Log.d("NewsActivity", "✅ Created new item from update: " + title);
    }

    // ✅ METHOD BARU: Refresh data immediately
    private void refreshNewsDataImmediately() {
        Log.d("NewsActivity", "⚡ Immediate refresh triggered");
        runOnUiThread(() -> {
            swipeRefreshLayout.setRefreshing(true);
            loadNewsHistoriFromServer();
        });
    }

    // DI NEWSACTIVITY.JAVA - PERBAIKI METHOD loadNewsHistoriFromServer
    private void loadNewsHistoriFromServer() {
        Log.d("NewsActivity", "📡 Loading news histori from server...");
        swipeRefreshLayout.setRefreshing(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // ✅ PERBAIKAN: Gunakan parameter for_news=true
        Call<NewsHistoriResponse> call = apiService.getNewsActivityData(100, 0, "true");

        call.enqueue(new Callback<NewsHistoriResponse>() {
            @Override
            public void onResponse(Call<NewsHistoriResponse> call, Response<NewsHistoriResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    NewsHistoriResponse historiResponse = response.body();
                    Log.d("NewsActivity", "📊 Response: " + historiResponse.toString());

                    if (historiResponse.isSuccess() && historiResponse.hasData()) {
                        Log.d("NewsActivity", "🎉 Data received: " + historiResponse.getDataCount() + " items");

                        // ✅ PERBAIKAN: Filter untuk menampilkan HANYA item aktif (Ditambahkan/Diubah)
                        filterAndDisplayActiveNews(historiResponse.getData());
                    } else {
                        Log.w("NewsActivity", "⚠️ No data from server");
                        loadLocalNewsData();
                    }
                } else {
                    Log.e("NewsActivity", "❌ HTTP error: " + response.code());
                    loadLocalNewsData();
                }
            }

            @Override
            public void onFailure(Call<NewsHistoriResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("NewsActivity", "❌ Network failure: " + t.getMessage());
                loadLocalNewsData();
            }
        });
    }

    // PERBAIKAN METHOD: filterAndDisplayActiveNews
    private void filterAndDisplayActiveNews(List<NewsHistoriItem> historiItems) {
        Log.d(TAG, "🔄 Filtering ACTIVE news from " + historiItems.size() + " items");

        List<NewsItem> activeNewsItems = new ArrayList<>();

        for (NewsHistoriItem historiItem : historiItems) {
            // ✅ PERBAIKAN: TAMPILKAN HANYA item dengan status "Ditambahkan" atau "Diubah"
            // ✅ JANGAN tampilkan "Dihapus" atau "Kadaluwarsa" di NewsActivity
            if ("Ditambahkan".equals(historiItem.getStatus()) || "Diubah".equals(historiItem.getStatus())) {

                Date timestamp;
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    timestamp = format.parse(historiItem.getTimestamp());
                } catch (Exception e) {
                    timestamp = new Date();
                }

                // Validasi gambar
                String imageData = null;
                if (historiItem.getImage_base64() != null && !historiItem.getImage_base64().isEmpty()) {
                    String rawImageData = historiItem.getImage_base64().trim();
                    if (rawImageData.length() >= 100 && !rawImageData.equals("null")) {
                        imageData = rawImageData;
                    }
                }

                NewsItem newsItem = new NewsItem(
                        historiItem.getId_news(),
                        historiItem.getTitle(),
                        historiItem.getPenginput(),
                        historiItem.getStatus(),
                        timestamp,
                        imageData,
                        historiItem.getPromo_id(),
                        historiItem.getKadaluwarsa()
                );

                activeNewsItems.add(newsItem);
                Log.d(TAG, "✅ ADDED TO ACTIVE NEWS: " + historiItem.getTitle() + " | Status: " + historiItem.getStatus());
            } else {
                Log.d(TAG, "❌ SKIPPED FOR NEWS: " + historiItem.getTitle() + " | Status: " + historiItem.getStatus());
            }
        }

        // Update data
        updateNewsData(activeNewsItems);
        Log.d(TAG, "🎉 ACTIVE NEWS - Total: " + activeNewsItems.size() + " items");
    }

    // ✅ METHOD BARU: Update data dengan benar
    private void updateNewsData(List<NewsItem> newItems) {
        newsItems.clear();
        newsItems.addAll(newItems);

        // Sort by timestamp descending
        Collections.sort(newsItems, (item1, item2) ->
                item2.getTimestamp().compareTo(item1.getTimestamp()));

        newsAdapter.notifyDataSetChanged();
        updateEmptyState();

        Log.d("NewsActivity", "🔄 News data updated - Total: " + newsItems.size() + " items");
    }

    // DI NewsActivity.java - PERBAIKI method handleIncomingUpdate
    private void handleIncomingUpdate(int promoId, String imageData, String title, String user, String status) {
        Log.d("NewsActivity", "🔄 Handling incoming update - ID: " + promoId +
                ", Title: " + title + ", Status: " + status +
                ", Image: " + (imageData != null ? imageData.length() + " chars" : "null"));

        // ✅ PERBAIKAN: Validasi gambar lebih ketat
        if (isValidImageForNewsDisplay(imageData)) {
            Log.d("NewsActivity", "✅ Processing update with VALID image");
            processPromoUpdateWithImage(promoId, status, imageData, title, user);
        } else {
            Log.d("NewsActivity", "🔄 Processing update without valid image - loading from server");
            // Load gambar terbaru dari server
            loadUpdatedPromoForNews(promoId, status);
        }
    }

    // ✅ PERBAIKAN: Validasi gambar yang lebih ketat
    private boolean isValidImageForNewsDisplay(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            Log.d("NewsActivity", "❌ Image data is null or empty");
            return false;
        }

        String cleanData = imageData.trim();

        // ✅ KRITERIA LEBIH KETAT: minimal 500 karakter untuk memastikan gambar valid
        boolean isValid = cleanData.length() >= 500 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL") &&
                !cleanData.endsWith("..") &&
                !cleanData.endsWith("...") &&
                cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$");

        Log.d("NewsActivity", "🖼️ Image validation - Length: " + cleanData.length() +
                ", Valid: " + isValid +
                ", First 50: " + (cleanData.length() > 50 ? cleanData.substring(0, 50) + "..." : cleanData));

        return isValid;
    }

    // ✅ PERBAIKAN: Process update dengan gambar dari server
    private void loadUpdatedPromoForNews(int promoId, String status) {
        Log.d("NewsActivity", "📡 Loading updated promo for News - ID: " + promoId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && promoResponse.getData() != null) {

                        Promo updatedPromo = null;
                        for (Promo promo : promoResponse.getData()) {
                            if (promo.getIdPromo() == promoId) {
                                updatedPromo = promo;
                                break;
                            }
                        }

                        if (updatedPromo != null) {
                            String serverImage = updatedPromo.getGambarBase64();
                            Log.d("NewsActivity", "✅ Found updated promo for News: " + updatedPromo.getNamaPromo());
                            Log.d("NewsActivity", "📷 Server image: " +
                                    (serverImage != null ? serverImage.length() + " chars" : "null"));

                            // ✅ PERBAIKAN: Gunakan gambar dari server jika valid
                            if (isValidImageForNewsDisplay(serverImage)) {
                                createOrUpdateNewsItemFromPromo(updatedPromo, "Diubah", serverImage);
                            } else {
                                // Jika gambar dari server tidak valid, cari di histori
                                loadImageFromHistori(promoId, updatedPromo);
                            }
                        } else {
                            Log.e("NewsActivity", "❌ Promo not found in server data");
                        }
                    } else {
                        Log.e("NewsActivity", "❌ Failed to load promo data");
                    }
                } else {
                    Log.e("NewsActivity", "❌ Error loading promo: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e("NewsActivity", "❌ Network error loading promo: " + t.getMessage());
            }
        });
    }

    // ✅ METHOD BARU: Load gambar dari histori jika dari server tidak valid
    private void loadImageFromHistori(int promoId, Promo promo) {
        Log.d("NewsActivity", "📚 Loading image from histori for promo: " + promo.getNamaPromo());

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<NewsHistoriResponse> call = apiService.getNewsHistori(50, 0);

        call.enqueue(new Callback<NewsHistoriResponse>() {
            @Override
            public void onResponse(Call<NewsHistoriResponse> call, Response<NewsHistoriResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NewsHistoriResponse historiResponse = response.body();
                    if (historiResponse.isSuccess() && historiResponse.hasData()) {

                        // Cari histori terbaru untuk promo ini dengan gambar valid
                        String latestValidImage = null;
                        for (NewsHistoriItem item : historiResponse.getData()) {
                            if (item.getPromo_id() == promoId &&
                                    isValidImageForNewsDisplay(item.getImage_base64())) {
                                latestValidImage = item.getImage_base64();
                                break;
                            }
                        }

                        if (latestValidImage != null) {
                            Log.d("NewsActivity", "✅ Found valid image from histori for: " + promo.getNamaPromo());
                            createOrUpdateNewsItemFromPromo(promo, "Diubah", latestValidImage);
                        } else {
                            Log.w("NewsActivity", "⚠️ No valid image found in histori for: " + promo.getNamaPromo());
                            createOrUpdateNewsItemFromPromo(promo, "Diubah", null);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<NewsHistoriResponse> call, Throwable t) {
                Log.e("NewsActivity", "❌ Error loading histori: " + t.getMessage());
                createOrUpdateNewsItemFromPromo(promo, "Diubah", null);
            }
        });
    }

    // ✅ METHOD BARU: Process update dengan gambar
    private void processPromoUpdateWithImage(int promoId, String status, String imageData, String title, String user) {
        Log.d("NewsActivity", "🎯 Processing update WITH image - ID: " + promoId);

        // Cari item existing
        NewsItem existingItem = findNewsItemByPromoId(promoId);

        if (existingItem != null) {
            // Update existing item
            existingItem.setStatus("Diubah");
            existingItem.setTimestamp(new Date());
            existingItem.setTitle(title != null ? title : existingItem.getTitle());
            existingItem.setPenginput(user != null ? user : existingItem.getPenginput());
            existingItem.setImageUrl(imageData);

            // Pindah ke atas
            newsItems.remove(existingItem);
            newsItems.add(0, existingItem);

            Log.d("NewsActivity", "✅ Updated existing item with image: " + existingItem.getTitle());
        } else {
            // Buat item baru
            createNewNewsItemFromUpdate(promoId, title, user, imageData);
        }

        sortAndSaveData();
        showNotificationForUpdate(title != null ? title : "Promo", user);
    }

    // ✅ METHOD BARU: Process update tanpa gambar
    private void processPromoUpdateWithoutImage(int promoId, String status, String title, String user) {
        Log.d("NewsActivity", "🎯 Processing update WITHOUT image - ID: " + promoId);

        // Load data terbaru dari server untuk mendapatkan gambar
        loadUpdatedPromoForNews(promoId, status);
    }

    // ✅ METHOD BARU: Cek update dari SharedPreferences
    private void checkForUpdatesFromPreferences() {
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        long lastUpdateTime = newsPrefs.getLong("last_update_time", 0);
        long currentTime = System.currentTimeMillis();

        // Jika ada update dalam 5 menit terakhir
        if (currentTime - lastUpdateTime < 5 * 60 * 1000) {
            int promoId = newsPrefs.getInt("last_updated_promo_id", -1);
            String title = newsPrefs.getString("last_updated_title", "");
            String inputter = newsPrefs.getString("last_updated_inputter", "");
            String imageData = newsPrefs.getString("last_updated_image", "");

            if (promoId != -1 && !title.isEmpty()) {
                Log.d("NewsActivity", "🔄 Found recent update in preferences: " + title);
                handleIncomingUpdate(promoId, imageData, title, inputter, "Diubah");
            }
        }
    }

    // PERBAIKAN method processPromoUpdateWithImage
    private void processPromoUpdateWithImage(int promoId, String status, String imageData) {
        Log.d("NewsActivity", "⚡ PROCESS UPDATE WITH IMAGE - ID: " + promoId +
                ", Image length: " + (imageData != null ? imageData.length() : 0));

        // Cari item existing
        NewsItem existingItem = findNewsItemByPromoId(promoId);

        if (existingItem != null) {
            // Update existing item dengan gambar baru
            existingItem.setStatus("Diubah");
            existingItem.setTimestamp(new Date());

            // PERBAIKAN: Selalu update gambar jika tersedia
            if (isValidImageForNewsDisplay(imageData)) {
                existingItem.setImageUrl(imageData);
                Log.d("NewsActivity", "✅ Updated image for promo: " + existingItem.getTitle() +
                        ", New image length: " + imageData.length());
            } else {
                Log.w("NewsActivity", "⚠️ Invalid image data, keeping existing image");
                // Jangan ubah gambar jika data tidak valid
            }

            // Pindah ke atas
            newsItems.remove(existingItem);
            newsItems.add(0, existingItem);

            sortAndSaveData();
            showNotificationForUpdate(existingItem.getTitle(), existingItem.getPenginput());

            Log.d("NewsActivity", "✅ Updated existing item: " + existingItem.getTitle());
        } else {
            // Jika tidak ditemukan, load dari server
            Log.d("NewsActivity", "🔄 Item not found, loading from server");
            loadUpdatedPromoForNews(promoId, status);
        }
    }

    // PERBAIKAN method createOrUpdateNewsItemFromPromo
    private void createOrUpdateNewsItemFromPromo(Promo promo, String status, String imageData) {
        Log.d("NewsActivity", "🔄 Creating/updating news item from promo: " + promo.getNamaPromo() +
                ", Image: " + (imageData != null ? imageData.length() + " chars" : "null"));

        // Cari item existing berdasarkan promo_id
        NewsItem existingItem = findNewsItemByPromoId(promo.getIdPromo());

        if (existingItem != null) {
            // Update existing item
            updateExistingNewsItem(existingItem, promo, imageData);
        } else {
            // Buat item baru
            createNewNewsItem(promo, status, imageData);
        }

        sortAndSaveData();
        showNotificationForUpdate(promo.getNamaPromo(), promo.getNamaPenginput());
    }

    // PERBAIKAN method updateExistingNewsItem
    private void updateExistingNewsItem(NewsItem existingItem, Promo promo, String imageData) {
        Log.d("NewsActivity", "✏️ Updating existing news item: " + existingItem.getTitle());

        // Update data
        existingItem.setTitle(promo.getNamaPromo());
        existingItem.setPenginput(promo.getNamaPenginput());
        existingItem.setStatus("Diubah");
        existingItem.setTimestamp(new Date());

        // PERBAIKAN: Update gambar hanya jika valid dan tersedia
        if (isValidImageForNewsDisplay(imageData)) {
            existingItem.setImageUrl(imageData);
            Log.d("NewsActivity", "✅ Updated image for: " + promo.getNamaPromo() +
                    ", New length: " + imageData.length());
        } else {
            // Jika gambar tidak valid, pertahankan gambar lama
            Log.w("NewsActivity", "⚠️ No valid image for update, keeping existing image");
        }

        // Pindah ke atas
        newsItems.remove(existingItem);
        newsItems.add(0, existingItem);

        Log.d("NewsActivity", "✅ Updated existing item: " + existingItem.getTitle());
    }

    // PERBAIKAN method createNewNewsItem
    private void createNewNewsItem(Promo promo, String status, String imageData) {
        // PERBAIKAN: Validasi gambar sebelum membuat item baru
        String finalImage = isValidImageForNewsDisplay(imageData) ? imageData : null;

        NewsItem newItem = new NewsItem(
                generateNewId(),
                promo.getNamaPromo(),
                promo.getNamaPenginput(),
                status,
                new Date(),
                finalImage,
                promo.getIdPromo()
        );

        newsItems.add(0, newItem);
        Log.d("NewsActivity", "✅ Created new news item: " + newItem.getTitle() +
                " | Image: " + (finalImage != null ? finalImage.length() + " chars" : "NO"));
    }


    // PERBAIKAN METHOD convertAndDisplayHistoriData
    private void convertAndDisplayHistoriData(List<NewsHistoriItem> historiItems) {
        Log.d("NewsActivity", "🔄 Converting " + historiItems.size() + " histori items");

        List<NewsItem> newNewsItems = new ArrayList<>();
        int displayedCount = 0;
        int withImagesCount = 0;

        for (NewsHistoriItem historiItem : historiItems) {
            try {
                if (("Ditambahkan".equals(historiItem.getStatus()) || "Diubah".equals(historiItem.getStatus()))
                        && historiItem.getPromo_id() > 0) {

                    Date timestamp;
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        timestamp = format.parse(historiItem.getTimestamp());
                    } catch (Exception e) {
                        timestamp = new Date();
                    }

                    // ✅ PERBAIKAN: Validasi gambar yang lebih baik
                    String imageData = null;
                    if (historiItem.getImage_base64() != null && !historiItem.getImage_base64().isEmpty()) {
                        String rawImageData = historiItem.getImage_base64().trim();

                        // PERBAIKAN: Kriteria lebih longgar
                        if (rawImageData.length() >= 100 && // Minimal 100 karakter
                                !rawImageData.equals("null") &&
                                !rawImageData.equals("NULL")) {
                            imageData = rawImageData;
                            withImagesCount++;
                            Log.d("NewsActivity", "✅ Valid image for: " + historiItem.getTitle() + " - " + imageData.length() + " chars");
                        } else {
                            Log.w("NewsActivity", "⚠️ Invalid image for: " + historiItem.getTitle() + " - length: " + rawImageData.length());
                        }
                    }

                    NewsItem newsItem = new NewsItem(
                            historiItem.getId_news(),
                            historiItem.getTitle(),
                            historiItem.getPenginput(),
                            historiItem.getStatus(),
                            timestamp,
                            imageData,
                            historiItem.getPromo_id(),
                            historiItem.getKadaluwarsa()
                    );

                    newNewsItems.add(newsItem);
                    displayedCount++;

                    Log.d("NewsActivity", "✅ ADDED TO NEWS: " + historiItem.getTitle() +
                            " | Status: " + historiItem.getStatus() +
                            " | Image: " + (imageData != null ? imageData.length() + " chars" : "NO IMAGE"));
                }
            } catch (Exception e) {
                Log.e("NewsActivity", "Error converting item: " + e.getMessage());
            }
        }

        // Update data
        newsItems.clear();
        newsItems.addAll(newNewsItems);

        sortNewsByTimestamp();
        saveNewsData();
        newsAdapter.notifyDataSetChanged();

        Log.d("NewsActivity", "🎉 FINAL NEWS - Total: " + displayedCount +
                ", With Images: " + withImagesCount);
    }

    // ✅ METHOD BARU: Load hanya data terbaru
    private void loadLatestNewsOnly() {
        Log.d(TAG, "🔄 Loading latest news only for first open");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<NewsHistoriResponse> call = apiService.getNewsActivityData(10, 0, "true"); // Limit 10 item terbaru

        call.enqueue(new Callback<NewsHistoriResponse>() {
            @Override
            public void onResponse(Call<NewsHistoriResponse> call, Response<NewsHistoriResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NewsHistoriResponse historiResponse = response.body();
                    if (historiResponse.isSuccess() && historiResponse.hasData()) {
                        filterAndDisplayActiveNews(historiResponse.getData());
                        Log.d(TAG, "✅ Latest news loaded: " + historiResponse.getDataCount() + " items");
                    }
                }
            }

            @Override
            public void onFailure(Call<NewsHistoriResponse> call, Throwable t) {
                Log.e(TAG, "❌ Failed to load latest news: " + t.getMessage());
                // Fallback ke load normal
                loadNewsHistoriFromServer();
            }
        });
    }

    // ✅ PERBAIKAN: Method refresh untuk swipe refresh - selalu load data lengkap
    private void refreshNewsData() {
        Log.d(TAG, "🔄 Full refresh with swipe");
        swipeRefreshLayout.setRefreshing(true);
        loadNewsHistoriFromServer(); // Load data lengkap
    }

    // ✅ PERBAIKAN METHOD: Filter untuk NewsActivity dengan logika yang benar
    // ✅ PERBAIKAN METHOD: Filter untuk NewsActivity - TAMPILKAN SEMUA ITEM AKTIF
    private void filterAndDisplayNewsData(List<NewsHistoriItem> historiItems) {
        Log.d("NewsActivity", "🔄 Filtering news data from " + historiItems.size() + " items");

        List<NewsItem> newNewsItems = new ArrayList<>();
        int displayedCount = 0;
        int withImagesCount = 0;

        // PERBAIKAN: Gunakan Map untuk menghindari duplikasi berdasarkan promo_id
        Map<Integer, NewsItem> uniquePromoMap = new HashMap<>();

        for (NewsHistoriItem historiItem : historiItems) {
            try {
                // ✅ PERBAIKAN KRITIS: Tampilkan SEMUA item dengan status "Ditambahkan" dan "Diubah"
                // TANPA peduli promo_id atau kadaluwarsa
                boolean isValidForNews = "Ditambahkan".equals(historiItem.getStatus()) ||
                        "Diubah".equals(historiItem.getStatus());

                if (!isValidForNews) {
                    Log.d("NewsActivity", "❌ SKIPPED - Not for News: " + historiItem.getTitle() +
                            " | Status: " + historiItem.getStatus());
                    continue;
                }

                Date timestamp;
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    timestamp = format.parse(historiItem.getTimestamp());
                } catch (Exception e) {
                    timestamp = new Date();
                }

                // Validasi gambar
                String imageData = null;
                if (historiItem.getImage_base64() != null && !historiItem.getImage_base64().isEmpty()) {
                    String rawImageData = historiItem.getImage_base64().trim();
                    if (rawImageData.length() >= 100 &&
                            !rawImageData.equals("null") &&
                            !rawImageData.equals("NULL")) {
                        imageData = rawImageData;
                        withImagesCount++;
                    }
                }

                // Ambil data kadaluwarsa
                String kadaluwarsa = historiItem.getKadaluwarsa() != null ?
                        historiItem.getKadaluwarsa() : "";

                NewsItem newsItem = new NewsItem(
                        historiItem.getId_news(),
                        historiItem.getTitle(),
                        historiItem.getPenginput(),
                        historiItem.getStatus(),
                        timestamp,
                        imageData,
                        historiItem.getPromo_id(),
                        kadaluwarsa
                );

                // ✅ PERBAIKAN: Untuk item dengan promo_id > 0, cegah duplikasi
                // Untuk item tanpa promo_id (null), selalu tampilkan
                if (historiItem.getPromo_id() > 0) {
                    int promoId = historiItem.getPromo_id();
                    if (!uniquePromoMap.containsKey(promoId)) {
                        uniquePromoMap.put(promoId, newsItem);
                        newNewsItems.add(newsItem);
                        displayedCount++;

                        Log.d("NewsActivity", "✅ ADDED TO NEWS: " + historiItem.getTitle() +
                                " | Status: " + historiItem.getStatus() +
                                " | Promo ID: " + historiItem.getPromo_id());
                    } else {
                        // Update dengan data yang lebih baru
                        NewsItem existingItem = uniquePromoMap.get(promoId);
                        if (timestamp.after(existingItem.getTimestamp())) {
                            uniquePromoMap.put(promoId, newsItem);
                            Log.d("NewsActivity", "🔄 UPDATED EXISTING: " + historiItem.getTitle());
                        }
                    }
                } else {
                    // Untuk item tanpa promo_id, selalu tambahkan
                    newNewsItems.add(newsItem);
                    displayedCount++;
                    Log.d("NewsActivity", "✅ ADDED TO NEWS (no promo_id): " + historiItem.getTitle() +
                            " | Status: " + historiItem.getStatus());
                }

            } catch (Exception e) {
                Log.e("NewsActivity", "Error converting item: " + e.getMessage());
            }
        }

        // PERBAIKAN: Update data dengan items yang unik
        updateNewsDataSmoothly(newNewsItems);

        Log.d("NewsActivity", "🎉 FINAL NEWS - Total: " + displayedCount +
                ", With Images: " + withImagesCount);
    }

    // ✅ METHOD BARU: Cek apakah promo sudah kadaluwarsa
    private boolean isPromoExpired(String kadaluwarsa) {
        if (kadaluwarsa == null || kadaluwarsa.isEmpty() || kadaluwarsa.equals("null")) {
            return false;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiryDate = format.parse(kadaluwarsa);
            Date currentDate = new Date();

            // Reset waktu untuk perbandingan yang akurat
            Calendar expiryCal = Calendar.getInstance();
            expiryCal.setTime(expiryDate);
            expiryCal.set(Calendar.HOUR_OF_DAY, 0);
            expiryCal.set(Calendar.MINUTE, 0);
            expiryCal.set(Calendar.SECOND, 0);
            expiryCal.set(Calendar.MILLISECOND, 0);

            Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(currentDate);
            currentCal.set(Calendar.HOUR_OF_DAY, 0);
            currentCal.set(Calendar.MINUTE, 0);
            currentCal.set(Calendar.SECOND, 0);
            currentCal.set(Calendar.MILLISECOND, 0);

            boolean isExpired = currentCal.after(expiryCal);

            Log.d("NewsActivity", "📅 Expiry Check - Date: " + kadaluwarsa +
                    " vs Today: " + format.format(currentDate) + " = " + isExpired);

            return isExpired;

        } catch (ParseException e) {
            Log.e("NewsActivity", "Error parsing expiry date: " + e.getMessage());
            return false;
        }
    }

    // ✅ METHOD BARU: Tampilkan empty state yang informatif
    private void showInformativeEmptyState() {
        runOnUiThread(() -> {
            TextView tvEmptyState = findViewById(R.id.tvEmptyState);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Tidak ada promo aktif saat ini\n\nPromo yang sudah kadaluwarsa atau dihapus dapat dilihat di halaman Promo Terhapus");

                // Optional: Tambahkan button untuk navigasi ke DeletedNews
                setupEmptyStateNavigation();
            }

            // Clear existing data
            newsItems.clear();
            newsAdapter.notifyDataSetChanged();
        });
    }

    // ✅ METHOD BARU: Setup navigasi dari empty state
    private void setupEmptyStateNavigation() {
        // Optional: Anda bisa menambahkan button atau click listener di sini
        // untuk mengarahkan user ke DeletedNewsActivity
        TextView tvEmptyState = findViewById(R.id.tvEmptyState);
        if (tvEmptyState != null) {
            tvEmptyState.setOnClickListener(v -> {
                Intent intent = new Intent(NewsActivity.this, DeletedNewsActivity.class);
                startActivity(intent);
            });
        }
    }

    // ✅ METHOD BARU: Debug data yang diterima dari server
    private void debugNewsData(List<NewsHistoriItem> historiItems) {
        Log.d("NewsActivity", "=== 🎯 DEBUG NEWS DATA ===");
        Log.d("NewsActivity", "Total items from server: " + historiItems.size());

        for (int i = 0; i < Math.min(historiItems.size(), 5); i++) {
            NewsHistoriItem item = historiItems.get(i);
            String imageData = item.getImage_base64();

            Log.d("NewsActivity", "Item " + i + ": " + item.getTitle());
            Log.d("NewsActivity", "  - Status: " + item.getStatus());
            Log.d("NewsActivity", "  - Promo ID: " + item.getPromo_id());
            Log.d("NewsActivity", "  - Image Data: " + (imageData != null ?
                    "Length=" + imageData.length() +
                            ", First50=" + (imageData.length() > 50 ? imageData.substring(0, 50) : imageData)
                    : "NULL"));
            Log.d("NewsActivity", "  ---");
        }
        Log.d("NewsActivity", "=== 🎯 END DEBUG ===");
    }

    // ✅ METHOD BARU: Handle delete promo dari broadcast
    private void processPromoDelete(String promoTitle, String penginput) {
        Log.d("NewsActivity", "🗑️ PROCESSING PROMO DELETE: " + promoTitle);

        // Cari dan tandai item sebagai dihapus
        boolean found = false;
        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) &&
                    !item.getStatus().equals("Dihapus") &&
                    !item.getStatus().equals("Kadaluwarsa")) {

                item.setStatus("Dihapus");
                item.setTimestamp(new Date());
                found = true;
                Log.d("NewsActivity", "✅ Marked as deleted: " + item.getTitle());
                break;
            }
        }

        if (found) {
            sortAndSaveData();
            showNotificationForDelete(promoTitle, penginput);
            Log.d("NewsActivity", "✅ Delete processed successfully: " + promoTitle);
        } else {
            Log.d("NewsActivity", "⚠️ Item not found for delete: " + promoTitle);
        }
    }

    // ✅ METHOD BARU: Handle promo kadaluwarsa dari broadcast
    private void processPromoExpired(String promoTitle, String penginput) {
        Log.d("NewsActivity", "🕒 PROCESSING PROMO EXPIRED: " + promoTitle);

        // Cari dan tandai item sebagai kadaluwarsa
        boolean found = false;
        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) &&
                    !item.getStatus().equals("Dihapus") &&
                    !item.getStatus().equals("Kadaluwarsa")) {

                item.setStatus("Kadaluwarsa");
                item.setTimestamp(new Date());
                found = true;
                Log.d("NewsActivity", "✅ Marked as expired: " + item.getTitle());
                break;
            }
        }

        if (found) {
            sortAndSaveData();
            showNotificationForExpired(promoTitle, penginput);
            Log.d("NewsActivity", "✅ Expired processed successfully: " + promoTitle);
        } else {
            Log.d("NewsActivity", "⚠️ Item not found for expired: " + promoTitle);
        }
    }

    // ✅ METHOD BARU: Notifikasi untuk promo kadaluwarsa
    private void showNotificationForExpired(String title, String penginput) {
        showNotification("🕒 Promo Kadaluwarsa", "Promo '" + title + "' telah kadaluwarsa");
    }

    // ✅ PERBAIKAN: Update setupRefreshReceiver untuk handle delete & expired


    private void loadUpdatedPromoFromServer(int promoId, String updatedImage) {
        Log.d("NewsActivity", "📡 Loading updated promo from server - ID: " + promoId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Coba get promo by ID dulu, jika tidak ada gunakan get semua promo
        Call<PromoResponse> call;
        try {
            // Coba method getPromoById jika ada
            call = apiService.getPromoById(promoId);
        } catch (Exception e) {
            // Fallback ke get semua promo
            Log.w("NewsActivity", "getPromoById not available, using getSemuaPromo");
            call = apiService.getSemuaPromo();
        }

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess() && promoResponse.getData() != null && !promoResponse.getData().isEmpty()) {

                        Promo updatedPromo = findPromoById(promoResponse.getData(), promoId);

                        if (updatedPromo != null) {
                            // Gunakan gambar dari server yang sudah diupdate
                            String finalImage = updatedPromo.getGambarBase64();

                            Log.d("NewsActivity", "✅ Loaded updated promo: " + updatedPromo.getNamaPromo() +
                                    " | Image: " + (finalImage != null ? finalImage.length() + " chars" : "null"));

                            // Proses update di News Activity
                            processPromoUpdateInNews(updatedPromo, "Diubah", finalImage);
                        } else {
                            Log.e("NewsActivity", "❌ Promo not found in response data");
                            processFallbackUpdate(promoId, updatedImage);
                        }
                    } else {
                        Log.e("NewsActivity", "❌ Failed to load updated promo data");
                        processFallbackUpdate(promoId, updatedImage);
                    }
                } else {
                    Log.e("NewsActivity", "❌ Error loading updated promo: " + response.code());
                    processFallbackUpdate(promoId, updatedImage);
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e("NewsActivity", "❌ Network error loading updated promo: " + t.getMessage());
                processFallbackUpdate(promoId, updatedImage);
            }
        });
    }

    // Helper method untuk mencari promo by ID
    private Promo findPromoById(List<Promo> promoList, int promoId) {
        for (Promo promo : promoList) {
            if (promo.getIdPromo() == promoId) {
                return promo;
            }
        }
        return null;
    }

    private void processPromoUpdateInNews(Promo updatedPromo, String status, String imageData) {
        Log.d("NewsActivity", "🔄 Processing promo update in news: " + updatedPromo.getNamaPromo());

        // Cari item existing
        NewsItem existingItem = findNewsItemByPromoId(updatedPromo.getIdPromo());

        if (existingItem != null) {
            // Update item yang sudah ada
            updateExistingNewsItemWithCompleteData(existingItem, updatedPromo, imageData);
        } else {
            // Buat item baru
            createNewNewsItemFromPromo(updatedPromo, status, imageData);
        }

        sortAndSaveData();
        showNotificationForUpdate(updatedPromo.getNamaPromo(), updatedPromo.getNamaPenginput());
    }

    // ✅ PERBAIKAN METHOD: Process update promo di NewsActivity
    private void processPromoUpdate(int promoId, String status, String updatedImage) {
        Log.d("NewsActivity", "🔄 PROCESSING PROMO UPDATE - ID: " + promoId + ", Status: " + status);

        if (promoId <= 0) {
            Log.e("NewsActivity", "❌ INVALID PROMO ID: " + promoId);
            return;
        }

        // Load data promo terbaru dari server untuk mendapatkan gambar yang valid
        loadUpdatedPromoForNews(promoId, status);
    }

    // ✅ METHOD BARU: Overloaded method untuk handle fallback tanpa image
    private void processFallbackUpdate(int promoId) {
        Log.d("NewsActivity", "🔄 Processing fallback update for ID: " + promoId + " (no image)");

        // Ambil data dari SharedPreferences
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        String title = newsPrefs.getString("last_updated_title", "Updated Promo");
        String inputter = newsPrefs.getString("last_updated_inputter", "Unknown");
        String updatedImage = newsPrefs.getString("last_updated_image", "");

        NewsItem fallbackItem = new NewsItem(
                generateNewId(),
                title,
                inputter,
                "Diubah",
                new Date(),
                isValidImageData(updatedImage) ? updatedImage : null,
                promoId
        );

        newsItems.add(0, fallbackItem);
        sortAndSaveData();

        Log.d("NewsActivity", "✅ Created fallback update item: " + title);
    }

    private boolean isValidImageData(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return false;
        }

        String cleanData = imageData.trim();

        // ✅ KRITERIA SANGAT LONGGAAR: minimal 50 karakter, bukan string "null"
        boolean isValid = cleanData.length() >= 50 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL");

        Log.d("NewsActivity", "🖼️ Image validation - Length: " + cleanData.length() +
                ", Is 'null': " + cleanData.equals("null") +
                ", Valid: " + isValid);

        return isValid;
    }

    // ✅ PERBAIKAN: Validasi gambar untuk News - LEBIH LONGGAAR
    private boolean isValidImageForNews(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return false;
        }

        String cleanData = imageData.trim();

        // ✅ KRITERIA LEBIH LONGGAAR: minimal 50 karakter saja
        return cleanData.length() >= 50 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL");
    }

    private void updateExistingNewsItemWithCompleteData(NewsItem existingItem, Promo updatedPromo, String imageData) {
        Log.d("NewsActivity", "✏️ Updating existing item with complete data: " + updatedPromo.getNamaPromo());

        // Update semua data
        existingItem.setTitle(updatedPromo.getNamaPromo());
        existingItem.setPenginput(updatedPromo.getNamaPenginput());
        existingItem.setStatus("Diubah");
        existingItem.setTimestamp(new Date());

        // Prioritaskan gambar dari server
        String finalImage = determineBestImageForNews(imageData, updatedPromo.getGambarBase64());
        existingItem.setImageUrl(finalImage);

        // Pindah ke atas
        newsItems.remove(existingItem);
        newsItems.add(0, existingItem);

        Log.d("NewsActivity", "✅ Updated existing item: " + existingItem.getTitle() +
                " | Image: " + (finalImage != null ? finalImage.length() + " chars" : "null"));
    }

    private void createNewNewsItemFromPromo(Promo promo, String status, String imageData) {
        String finalImage = determineBestImageForNews(imageData, promo.getGambarBase64());

        NewsItem newItem = new NewsItem(
                generateNewId(),
                promo.getNamaPromo(),
                promo.getNamaPenginput(),
                status,
                new Date(),
                finalImage,
                promo.getIdPromo()
        );

        newsItems.add(0, newItem);
        Log.d("NewsActivity", "✅ Created new item: " + newItem.getTitle() +
                " | Image: " + (finalImage != null ? finalImage.length() + " chars" : "null"));
    }

    private String determineBestImageForNews(String updatedImage, String serverImage) {
        // Prioritas 1: Gambar dari server (paling terpercaya)
        if (isValidImageData(serverImage)) {
            Log.d("NewsActivity", "🎯 Using SERVER image");
            return serverImage;
        }

        // Prioritas 2: Gambar yang di-update
        if (isValidImageData(updatedImage)) {
            Log.d("NewsActivity", "🎯 Using UPDATED image");
            return updatedImage;
        }

        // Prioritas 3: Tidak ada gambar
        Log.d("NewsActivity", "🎯 No valid image available");
        return null;
    }

    private void processFallbackUpdate(int promoId, String updatedImage) {
        Log.d("NewsActivity", "🔄 Processing fallback update for ID: " + promoId);

        // Ambil data dari SharedPreferences
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        String title = newsPrefs.getString("last_updated_title", "Updated Promo");
        String inputter = newsPrefs.getString("last_updated_inputter", "Unknown");

        NewsItem fallbackItem = new NewsItem(
                generateNewId(),
                title,
                inputter,
                "Diubah",
                new Date(),
                isValidImageData(updatedImage) ? updatedImage : null,
                promoId
        );

        newsItems.add(0, fallbackItem);
        sortAndSaveData();

        Log.d("NewsActivity", "✅ Created fallback update item: " + title);
    }

    // ✅ METHOD BARU: HAPUS ITEM DUPLIKAT SEBELUM PROSES BARU
    private void removeExistingPromoItems(String promoTitle) {
        Iterator<NewsItem> iterator = newsItems.iterator();
        while (iterator.hasNext()) {
            NewsItem item = iterator.next();
            if (item.getTitle().equals(promoTitle) && item.getPromoId() > 0) {
                iterator.remove();
                Log.d("NewsActivity", "🔄 Removed existing item before processing: " + promoTitle);
            }
        }
    }


    // ✅ METHOD UNTUK PROSES DELETE LANGSUNG
    private void processDirectDelete(String promoTitle, String penginput) {
        Log.d("NewsActivity", "🗑 PROCESSING DIRECT DELETE: " + promoTitle);

        NewsItem itemToDelete = null;

        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) &&
                    item.getPromoId() > 0 &&
                    !item.getStatus().equals("Dihapus")) {

                itemToDelete = item;
                Log.d("NewsActivity", "Found ACTIVE item to delete: " + item.getTitle());
                break;
            }
        }

        if (itemToDelete != null) {
            String currentImage = itemToDelete.getImageUrl();
            int originalPromoId = itemToDelete.getPromoId();

            itemToDelete.setStatus("Dihapus");
            itemToDelete.setTimestamp(new Date());
            itemToDelete.setPromoId(-1);

            if (currentImage != null) {
                itemToDelete.setImageUrl(currentImage);
            }

            Log.d("NewsActivity", "✅ DELETED ITEM: " + itemToDelete.getTitle());

            sortAndSaveData();
            showNotificationForDelete(promoTitle, penginput);
            Toast.makeText(this, "Promo '" + promoTitle + "' dihapus", Toast.LENGTH_SHORT).show();

        } else {
            Log.d("NewsActivity", "No ACTIVE items found to delete: " + promoTitle);
            createNewDeleteItem(promoTitle, penginput);
        }
    }

    // ✅ METHOD UNTUK BUAT ITEM DELETE BARU
    private void createNewDeleteItem(String promoTitle, String penginput) {
        Log.d("NewsActivity", "Creating new delete item for: " + promoTitle);

        String lastImage = getLastImageForPromo(promoTitle);

        NewsItem deletedItem = new NewsItem(
                generateNewId(),
                promoTitle,
                penginput,
                "Dihapus",
                new Date(),
                lastImage,
                -1
        );

        newsItems.add(0, deletedItem);
        sortAndSaveData();
        showNotificationForDelete(promoTitle, penginput);

        Log.d("NewsActivity", "✅ CREATED NEW DELETE ITEM: " + promoTitle);
    }

    // ✅ METHOD UNTUK CARI GAMBAR TERAKHIR
    private String getLastImageForPromo(String promoTitle) {
        Log.d("NewsActivity", "🔍 Searching for last image of: " + promoTitle);

        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) &&
                    item.getPromoId() > 0 &&
                    !item.getStatus().equals("Dihapus")) {
                Log.d("NewsActivity", "✅ Found ACTIVE item with image: " + item.getTitle());
                return item.getImageUrl();
            }
        }

        NewsItem mostRecentDeleted = null;
        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) && item.getStatus().equals("Dihapus")) {
                if (mostRecentDeleted == null || item.getTimestamp().after(mostRecentDeleted.getTimestamp())) {
                    mostRecentDeleted = item;
                }
            }
        }

        if (mostRecentDeleted != null && mostRecentDeleted.getImageUrl() != null) {
            Log.d("NewsActivity", "✅ Found RECENT DELETED item with image: " + mostRecentDeleted.getTitle());
            return mostRecentDeleted.getImageUrl();
        }

        Log.d("NewsActivity", "❌ No suitable image found for: " + promoTitle);
        return null;
    }

    // ✅ METHOD-METHOD CLEAR DATA
    private void clearUpdateData() {
        newsUpdatePrefs.edit()
                .remove("last_update_time")
                .remove("last_updated_promo_id")
                .remove("last_updated_status")
                .remove("last_updated_image")
                .apply();
    }

    // ✅ NOTIFICATION METHODS
    private void showNotificationForUpdate(String title, String penginput) {
        showNotification("Promo Diupdate", "Promo '" + title + "' telah diperbarui oleh " + penginput);
    }

    private void showNotificationForDelete(String title, String penginput) {
        showNotification("Promo Dihapus", "Promo '" + title + "' telah dihapus oleh " + penginput);
    }

    private void sortAndSaveData() {
        sortNewsByTimestamp();
        saveNewsData();
        newsAdapter.notifyDataSetChanged();
    }

    // HELPER METHODS
    private NewsItem findNewsItemByPromoId(int promoId) {
        for (NewsItem item : newsItems) {
            if (item.getPromoId() == promoId) {
                return item;
            }
        }
        return null;
    }

    private void saveNewsData() {
        sortNewsByTimestamp();
        String json = gson.toJson(newsItems);
        sharedPreferences.edit().putString(NEWS_KEY, json).apply();
    }

    private int generateNewId() {
        int maxId = 0;
        for (NewsItem item : newsItems) {
            if (item.getId() > maxId) {
                maxId = item.getId();
            }
        }
        return maxId + 1;
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
            Log.e("NewsActivity", "Notification permission denied", e);
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
                if (position >= 0 && position < newsItems.size()) {
                    newsAdapter.removeItem(position);
                    saveNewsData();
                    Toast.makeText(NewsActivity.this, "Berita dihapus", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerNews);
    }

    private void removeOldNews() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date oneWeekAgo = calendar.getTime();

        List<NewsItem> itemsToRemove = new ArrayList<>();
        for (NewsItem item : newsItems) {
            if (item.getTimestamp().before(oneWeekAgo)) {
                itemsToRemove.add(item);
            }
        }

        if (!itemsToRemove.isEmpty()) {
            newsItems.removeAll(itemsToRemove);
            newsAdapter.notifyDataSetChanged();
            saveNewsData();
            Log.d("NewsActivity", "Removed " + itemsToRemove.size() + " old news items");
        }
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

    private void requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private boolean isValidImageDataForDisplay(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return false;
        }

        String cleanData = imageData.trim();

        // ✅ KRITERIA LEBIH LONGGAAR: minimal 100 karakter saja
        boolean isValid = cleanData.length() >= 100 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL") &&
                !cleanData.endsWith("..") &&
                !cleanData.endsWith("...");

        Log.d("NewsActivity", "🖼️ Image validation result: " + isValid +
                " | Length: " + cleanData.length());

        return isValid;
    }

    // ✅ METHOD BARU: Debug semua data yang diterima
    private void debugAllReceivedData(List<NewsHistoriItem> historiItems) {
        Log.d("NewsActivity", "=== 📊 ALL RECEIVED DATA DEBUG ===");
        Log.d("NewsActivity", "Total items from server: " + historiItems.size());

        for (int i = 0; i < Math.min(historiItems.size(), 10); i++) {
            NewsHistoriItem item = historiItems.get(i);
            String imageData = item.getImage_base64();

            boolean hasImageData = imageData != null && !imageData.isEmpty() && !imageData.equals("null");
            boolean hasValidImage = hasImageData && imageData.length() >= 500;

            Log.d("NewsActivity", "Item " + i + ": " + item.getTitle());
            Log.d("NewsActivity", "  - Status: " + item.getStatus());
            Log.d("NewsActivity", "  - Promo ID: " + item.getPromo_id());
            Log.d("NewsActivity", "  - Has Image Data: " + hasImageData);

            if (hasImageData) {
                Log.d("NewsActivity", "  - Image Length: " + imageData.length());
                Log.d("NewsActivity", "  - First 100 chars: " + (imageData.length() > 100 ?
                        imageData.substring(0, 100) + "..." : imageData));
                Log.d("NewsActivity", "  - Valid for display: " + hasValidImage);
            }
            Log.d("NewsActivity", "  ---");
        }
        Log.d("NewsActivity", "=== 📊 END DATA DEBUG ===");
    }

    // ✅ PERBAIKAN: Validasi gambar yang lebih longgar dan akurat
    private boolean isValidImageDataForNewsDisplay(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            Log.d("NewsActivity", "❌ Image data is null or empty");
            return false;
        }

        String cleanData = imageData.trim();

        // ✅ KRITERIA LEBIH LONGGAAR DAN AKURAT:
        // 1. Minimal 100 karakter (bukan 500)
        // 2. Bukan string "null" atau "NULL"
        // 3. Tidak berakhir dengan tanda truncation
        // 4. Format base64 dasar valid

        boolean isValid = cleanData.length() >= 100 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL") &&
                !cleanData.endsWith("..") &&
                !cleanData.endsWith("...");

        Log.d("NewsActivity", "🖼️ Image validation - Length: " + cleanData.length() +
                ", Valid: " + isValid +
                ", First 30: " + (cleanData.length() > 30 ? cleanData.substring(0, 30) + "..." : cleanData));

        return isValid;
    }

    // ✅ METHOD BARU: Update empty state
    private void updateEmptyState() {
        runOnUiThread(() -> {
            TextView tvEmptyState = findViewById(R.id.tvEmptyState);
            if (tvEmptyState != null) {
                if (newsItems.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Tidak ada berita terbaru");
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }
        });
    }



    private String mapStatusToConsistent(String status) {
        if (status == null) return "Ditambahkan";

        switch (status.toLowerCase()) {
            case "ditambahkan":
            case "added":
            case "inserted":
                return "Ditambahkan";

            case "diubah":
            case "updated":
            case "modified":
                return "Diubah";

            case "dihapus":
            case "deleted":
            case "removed":
                return "Dihapus";

            default:
                return "Ditambahkan";
        }
    }

    // ✅ METHOD DEBUG: Cek data image yang diterima
    private void debugImageData(NewsItem item) {
        Log.d("NewsActivity", "=== DEBUG IMAGE DATA ===");
        Log.d("NewsActivity", "Title: " + item.getTitle());
        Log.d("NewsActivity", "Image URL: " + (item.getImageUrl() != null ?
                "Length=" + item.getImageUrl().length() +
                        ", First50=" + (item.getImageUrl().length() > 50 ?
                        item.getImageUrl().substring(0, 50) : item.getImageUrl()) : "NULL"));
        Log.d("NewsActivity", "Promo ID: " + item.getPromoId());
        Log.d("NewsActivity", "Status: " + item.getStatus());
    }

    private void sortNewsByTimestamp() {
        Collections.sort(newsItems, (item1, item2) -> {
            return item2.getTimestamp().compareTo(item1.getTimestamp());
        });

        Log.d("NewsActivity", "Sorted " + newsItems.size() + " items. First item: " +
                (newsItems.size() > 0 ? newsItems.get(0).getTitle() + " - " + newsItems.get(0).getTimestamp() : "none"));
    }

    private void loadNewsData() {
        Log.d("NewsActivity", "🔄 Loading news data on create");
        loadNewsHistoriFromServer();

        // PERBAIKAN: Juga load dari local storage sebagai fallback
        loadLocalNewsData();
    }

    private void loadLocalNewsData() {
        String json = sharedPreferences.getString(NEWS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<NewsItem>>(){}.getType();
            List<NewsItem> savedNews = gson.fromJson(json, type);
            if (savedNews != null && !savedNews.isEmpty()) {
                newsItems.clear();
                newsItems.addAll(savedNews);
                sortNewsByTimestamp();
                newsAdapter.notifyDataSetChanged();
                removeOldNews();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
        }
    }


    // ✅ METHOD BARU: Refresh data dengan polling (jika broadcast tidak work)
    private void startAutoRefresh() {
        Log.d("NewsActivity", "🔄 Starting auto-refresh polling");

        // Refresh setiap 3 detik selama 30 detik setelah kembali
        Handler handler = new Handler();
        Runnable refreshRunnable = new Runnable() {
            int attempt = 0;
            final int maxAttempts = 10;

            @Override
            public void run() {
                if (attempt < maxAttempts) {
                    Log.d("NewsActivity", "🔄 Auto-refresh attempt: " + (attempt + 1));
                    refreshNewsDataSilent(); // Refresh tanpa swipe indicator
                    attempt++;
                    handler.postDelayed(this, 3000); // Coba lagi setiap 3 detik
                }
            }
        };

        handler.postDelayed(refreshRunnable, 1000); // Mulai setelah 1 detik
    }


    // ✅ METHOD BARU DI NEWSACTIVITY: Refresh silent (tanpa loading indicator)
    private void refreshNewsDataSilent() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<NewsHistoriResponse> call = apiService.getNewsHistori(20, 0);

        call.enqueue(new Callback<NewsHistoriResponse>() {
            @Override
            public void onResponse(Call<NewsHistoriResponse> call, Response<NewsHistoriResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NewsHistoriResponse historiResponse = response.body();
                    if (historiResponse.isSuccess() && historiResponse.hasData()) {
                        convertAndDisplayHistoriData(historiResponse.getData());
                        Log.d("NewsActivity", "✅ Silent refresh successful");
                    }
                }
            }

            @Override
            public void onFailure(Call<NewsHistoriResponse> call, Throwable t) {
                Log.e("NewsActivity", "❌ Silent refresh failed: " + t.getMessage());
            }
        });
    }


    // ✅ METHOD BARU DI NEWSACTIVITY: Check for promo updates
    private void checkForPromoUpdates() {
        long lastCheckTime = getLastNewsCheckTime();
        long currentTime = System.currentTimeMillis();

        Log.d("NewsActivity", "=== CHECKING PROMO UPDATES ===");

        synchronized (updateLock) {
            checkForPromoAdditions();
            checkForPromoUpdatesFromPrefs(lastCheckTime);
            checkForPromoDeletionsFromPrefs(lastCheckTime);
        }

        saveLastNewsCheckTime(currentTime);
    }

    // ✅ METHOD BARU DI NEWSACTIVITY: Check promo additions
    private void checkForPromoAdditions() {
        refreshNewsData();
    }

    // ✅ METHOD BARU DI NEWSACTIVITY: Check updates from prefs
    private void checkForPromoUpdatesFromPrefs(long lastCheckTime) {
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        long lastUpdateTime = newsPrefs.getLong("last_update_time", 0);

        if (lastUpdateTime > lastCheckTime) {
            int promoId = newsPrefs.getInt("last_updated_promo_id", -1);
            String status = newsPrefs.getString("last_updated_status", "Diubah");

            if (promoId != -1) {
                processPromoUpdate(promoId, status, null);
                clearUpdateDataFromPrefs();
            }
        }
    }

    // ✅ METHOD BARU DI NEWSACTIVITY: Check deletions from prefs
    private void checkForPromoDeletionsFromPrefs(long lastCheckTime) {
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        long lastDeleteTime = newsPrefs.getLong("last_delete_time", 0);

        if (lastDeleteTime > lastCheckTime) {
            String deletedTitle = newsPrefs.getString("last_deleted_title", "");
            String deletedInputter = newsPrefs.getString("last_deleted_inputter", "");

            if (!deletedTitle.isEmpty()) {
                processDirectDelete(deletedTitle, deletedInputter);
                clearDeleteData();
            }
        }
    }

    // ✅ METHOD BARU DI NEWSACTIVITY: Clear update data
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

    // ✅ METHOD BARU DI NEWSACTIVITY: Clear delete data
    private void clearDeleteData() {
        SharedPreferences prefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        prefs.edit()
                .remove("last_delete_time")
                .remove("last_deleted_title")
                .remove("last_deleted_inputter")
                .remove("last_deleted_status")
                .apply();
    }

    // ✅ METHOD BARU DI NEWSACTIVITY: Get last check time
    private long getLastNewsCheckTime() {
        return getSharedPreferences("NewsActivityCheck", MODE_PRIVATE)
                .getLong("last_news_check_time", 0);
    }

    // ✅ METHOD BARU DI NEWSACTIVITY: Save last check time
    private void saveLastNewsCheckTime(long time) {
        getSharedPreferences("NewsActivityCheck", MODE_PRIVATE)
                .edit()
                .putLong("last_news_check_time", time)
                .apply();
    }

    // PERBAIKAN: Method untuk debug data yang diterima
    private void debugHistoriData(List<NewsHistoriItem> historiItems) {
        Log.d("NewsActivity", "=== 🎯 DEBUG HISTORI DATA ===");
        Log.d("NewsActivity", "Total items from server: " + historiItems.size());

        for (int i = 0; i < Math.min(historiItems.size(), 5); i++) {
            NewsHistoriItem item = historiItems.get(i);
            Log.d("NewsActivity", "Item " + i + ":");
            Log.d("NewsActivity", "  - Title: " + item.getTitle());
            Log.d("NewsActivity", "  - Status: " + item.getStatus());
            Log.d("NewsActivity", "  - Promo ID: " + item.getPromo_id());
            Log.d("NewsActivity", "  - Penginput: " + item.getPenginput());
            Log.d("NewsActivity", "  - Kadaluwarsa: " + item.getKadaluwarsa());
            Log.d("NewsActivity", "  - Image Length: " + (item.getImage_base64() != null ? item.getImage_base64().length() : 0));
            Log.d("NewsActivity", "  ---");
        }
        Log.d("NewsActivity", "=== 🎯 END DEBUG ===");
    }

    // ✅ METHOD BARU: Update data dengan animasi smooth
    private void updateNewsDataSmoothly(List<NewsItem> newItems) {
        // Simpan data lama untuk comparison
        List<NewsItem> oldItems = new ArrayList<>(newsItems);

        // Update data
        newsItems.clear();
        newsItems.addAll(newItems);

        // Sort dan save
        sortNewsByTimestamp();
        saveNewsData();

        // Notify adapter dengan animasi
        newsAdapter.updateData(newsItems);

        Log.d("NewsActivity", "🔄 Data updated smoothly - Old: " + oldItems.size() +
                ", New: " + newItems.size());
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (isFirstLoad) {
            // Load data terbaru saja saat pertama kali buka
            loadLatestNewsOnly();
            isFirstLoad = false;
        } else {
            // Refresh lengkap saat resume biasa
            startAutoRefresh();
        }

        checkForPromoUpdates();
    }

    // ✅ METHOD BARU: Debug detail gambar yang diterima dari server
    private void debugImageDetails(List<NewsHistoriItem> historiItems) {
        Log.d("NewsActivity", "=== 🖼️ IMAGE DEBUG START ===");
        Log.d("NewsActivity", "Total items from server: " + historiItems.size());

        int totalWithImageData = 0;
        int totalWithValidImage = 0;

        for (int i = 0; i < Math.min(historiItems.size(), 10); i++) { // Debug 10 item pertama
            NewsHistoriItem item = historiItems.get(i);
            String imageData = item.getImage_base64();

            boolean hasImageData = imageData != null && !imageData.isEmpty() && !imageData.equals("null");
            boolean hasValidImage = hasImageData && imageData.length() >= 100;

            if (hasImageData) totalWithImageData++;
            if (hasValidImage) totalWithValidImage++;

            Log.d("NewsActivity", "Item " + i + ": " + item.getTitle());
            Log.d("NewsActivity", "  - Status: " + item.getStatus());
            Log.d("NewsActivity", "  - Promo ID: " + item.getPromo_id());
            Log.d("NewsActivity", "  - Has Image Data: " + hasImageData);

            if (hasImageData) {
                Log.d("NewsActivity", "  - Image Length: " + imageData.length());
                Log.d("NewsActivity", "  - First 30 chars: " + (imageData.length() > 30 ? imageData.substring(0, 30) + "..." : imageData));
                Log.d("NewsActivity", "  - Last 30 chars: " + (imageData.length() > 30 ? "..." + imageData.substring(imageData.length() - 30) : imageData));
                Log.d("NewsActivity", "  - Ends with '...': " + (imageData.endsWith("...")));
                Log.d("NewsActivity", "  - Is 'null' string: " + (imageData.equals("null")));
                Log.d("NewsActivity", "  - Valid for display: " + hasValidImage);
            } else {
                Log.d("NewsActivity", "  - ❌ NO IMAGE DATA");
            }
            Log.d("NewsActivity", "  ---");
        }

        Log.d("NewsActivity", "=== IMAGE DEBUG SUMMARY ===");
        Log.d("NewsActivity", "Total items: " + historiItems.size());
        Log.d("NewsActivity", "With image data: " + totalWithImageData);
        Log.d("NewsActivity", "With valid image: " + totalWithValidImage);
        Log.d("NewsActivity", "Without image: " + (historiItems.size() - totalWithImageData));
        Log.d("NewsActivity", "=== 🖼️ IMAGE DEBUG END ===");
    }

    // ✅ METHOD BARU: Debug gambar yang akhirnya ditampilkan
    private void debugDisplayedImages() {
        Log.d("NewsActivity", "=== 📱 DISPLAYED IMAGES DEBUG ===");

        int displayedWithImage = 0;
        int displayedWithoutImage = 0;

        for (int i = 0; i < Math.min(newsItems.size(), 5); i++) {
            NewsItem item = newsItems.get(i);
            String imageUrl = item.getImageUrl();

            boolean hasImage = imageUrl != null;

            if (hasImage) displayedWithImage++;
            else displayedWithoutImage++;

            Log.d("NewsActivity", "Displayed " + i + ": " + item.getTitle());
            Log.d("NewsActivity", "  - Status: " + item.getStatus());
            Log.d("NewsActivity", "  - Has Image: " + hasImage);

            if (hasImage) {
                Log.d("NewsActivity", "  - Image Length: " + imageUrl.length());
                Log.d("NewsActivity", "  - First 20: " + (imageUrl.length() > 20 ? imageUrl.substring(0, 20) + "..." : imageUrl));
            }
            Log.d("NewsActivity", "  ---");
        }

        Log.d("NewsActivity", "DISPLAYED SUMMARY - With Image: " + displayedWithImage + ", Without: " + displayedWithoutImage);
        Log.d("NewsActivity", "=== 📱 DISPLAYED IMAGES DEBUG END ===");
    }

}