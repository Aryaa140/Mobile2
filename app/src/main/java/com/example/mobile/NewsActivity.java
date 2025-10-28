package com.example.mobile;

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
import android.util.Base64;
import android.util.Log;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void setupRefreshReceiver() {
        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("NewsActivity", "🔄 Received refresh broadcast");
                refreshNewsData();
            }
        };

        try {
            IntentFilter filter = new IntentFilter("REFRESH_NEWS_DATA");
            registerReceiver(refreshReceiver, filter);
            Log.d("NewsActivity", "✅ Broadcast receiver registered successfully");
        } catch (Exception e) {
            Log.e("NewsActivity", "❌ Error registering receiver: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU YANG DIPERBAIKI: Process update promo
    private void processDirectUpdate(int promoId, String status, String updatedImage) {
        Log.d("NewsActivity", "🔄 PROCESSING DIRECT UPDATE - ID: " + promoId);

        if (promoId <= 0) {
            Log.e("NewsActivity", "❌ INVALID PROMO ID: " + promoId);
            return;
        }

        // Load data promo terbaru dari server
        loadUpdatedPromoFromServer(promoId, updatedImage);
    }

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

    // ✅ METHOD BARU: Load promo yang diupdate untuk News
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
                            Log.d("NewsActivity", "📷 Server image length: " + (serverImage != null ? serverImage.length() : 0));

                            // Process update dengan gambar dari server
                            createOrUpdateNewsItemFromPromo(updatedPromo, "Diubah", serverImage);
                        } else {
                            Log.e("NewsActivity", "❌ Promo not found in server data");
                            processFallbackUpdate(promoId);
                        }
                    } else {
                        Log.e("NewsActivity", "❌ Failed to load promo data");
                        processFallbackUpdate(promoId);
                    }
                } else {
                    Log.e("NewsActivity", "❌ Error loading promo: " + response.code());
                    processFallbackUpdate(promoId);
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e("NewsActivity", "❌ Network error loading promo: " + t.getMessage());
                processFallbackUpdate(promoId);
            }
        });
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

    // ✅ METHOD BARU: Create atau update news item dari promo
    private void createOrUpdateNewsItemFromPromo(Promo promo, String status, String imageData) {
        Log.d("NewsActivity", "🔄 Creating/updating news item from promo: " + promo.getNamaPromo());

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

    // ✅ METHOD BARU: Update existing news item
    private void updateExistingNewsItem(NewsItem existingItem, Promo promo, String imageData) {
        Log.d("NewsActivity", "✏️ Updating existing news item: " + existingItem.getTitle());

        // Update data
        existingItem.setTitle(promo.getNamaPromo());
        existingItem.setPenginput(promo.getNamaPenginput());
        existingItem.setStatus("Diubah");
        existingItem.setTimestamp(new Date());

        // Update gambar hanya jika valid
        if (isValidImageForNews(imageData)) {
            existingItem.setImageUrl(imageData);
            Log.d("NewsActivity", "✅ Updated image for: " + promo.getNamaPromo());
        } else {
            Log.w("NewsActivity", "⚠️ No valid image for update, keeping existing");
        }

        // Pindah ke atas
        newsItems.remove(existingItem);
        newsItems.add(0, existingItem);

        Log.d("NewsActivity", "✅ Updated existing item: " + existingItem.getTitle());
    }

    // ✅ METHOD BARU: Create new news item
    private void createNewNewsItem(Promo promo, String status, String imageData) {
        String finalImage = isValidImageForNews(imageData) ? imageData : null;

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
                " | Image: " + (finalImage != null ? "YES" : "NO"));
    }

    // ✅ METHOD BARU: Validasi gambar untuk News
    private boolean isValidImageForNews(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return false;
        }

        String cleanData = imageData.trim();
        return cleanData.length() > 500 &&
                !cleanData.equals("null") &&
                !cleanData.endsWith("..") &&
                cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$");
    }

    // ✅ PERBAIKAN METHOD: checkForPromoUpdatesFromPrefs
    private void checkForPromoUpdatesFromPrefs(long lastCheckTime) {
        SharedPreferences newsPrefs = getSharedPreferences("NewsUpdates", MODE_PRIVATE);
        long lastUpdateTime = newsPrefs.getLong("last_update_time", 0);

        if (lastUpdateTime > lastCheckTime) {
            int promoId = newsPrefs.getInt("last_updated_promo_id", -1);
            String status = newsPrefs.getString("last_updated_status", "Diubah");
            String updatedImage = newsPrefs.getString("last_updated_image", "");

            Log.d("NewsActivity", "🔄 PROCESSING UPDATE FROM PREFS - ID: " + promoId +
                    ", Image: " + (updatedImage != null && !updatedImage.isEmpty() ?
                    updatedImage.length() + " chars" : "NULL"));

            if (promoId != -1) {
                processPromoUpdate(promoId, status, updatedImage);

                // Clear data setelah diproses
                clearUpdateDataFromPrefs();
            }
        }
    }

    // ✅ METHOD BARU: Clear update data dari prefs
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

        Log.d("NewsActivity", "🧹 Cleared update data from prefs");
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

    // ✅ METHOD BARU: Validasi data gambar yang lebih baik
    private boolean isValidImageData(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return false;
        }

        String cleanData = imageData.trim();
        return cleanData.length() > 100 &&
                !cleanData.equals("null") &&
                !cleanData.endsWith("..") &&
                cleanData.matches("^[a-zA-Z0-9+/]*={0,2}$");
    }


    // ✅ PERBAIKAN: PROSES DELETE DENGAN PRIORITAS TINGGI
    private void checkForPromoDeletionsFromPrefs(long lastCheckTime) {
        long lastDeleteTime = newsUpdatePrefs.getLong("last_delete_time", 0);

        if (lastDeleteTime > lastCheckTime) {
            String deletedTitle = newsUpdatePrefs.getString("last_deleted_title", "");
            String deletedInputter = newsUpdatePrefs.getString("last_deleted_inputter", "");

            Log.d("NewsActivity", "🗑️ HIGH PRIORITY - PROCESSING DELETE: " + deletedTitle);

            if (!deletedTitle.isEmpty()) {
                removeExistingPromoItems(deletedTitle);
                processDirectDelete(deletedTitle, deletedInputter);
                clearDeleteData();
            }
        }
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

    // ✅ SISTEM YANG LEBIH SEDERHANA DAN EFISIEN
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

    private void checkForPromoAdditions() {
        refreshNewsData();
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

    private void clearDeleteData() {
        newsUpdatePrefs.edit()
                .remove("last_delete_time")
                .remove("last_deleted_title")
                .remove("last_deleted_inputter")
                .remove("last_deleted_status")
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

    private void loadNewsHistoriFromServer() {
        Log.d("NewsActivity", "📡 Loading news histori from server...");
        swipeRefreshLayout.setRefreshing(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<NewsHistoriResponse> call = apiService.getNewsHistori(20, 0);

        call.enqueue(new Callback<NewsHistoriResponse>() {
            @Override
            public void onResponse(Call<NewsHistoriResponse> call, Response<NewsHistoriResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                Log.d("NewsActivity", "📡 Response Code: " + response.code());

                if (response.body() != null) {
                    Log.d("NewsActivity", "📡 Response Body: " + new Gson().toJson(response.body()));
                }

                if (response.isSuccessful() && response.body() != null) {
                    NewsHistoriResponse historiResponse = response.body();
                    Log.d("NewsActivity", "✅ Server Success: " + historiResponse.isSuccess());
                    Log.d("NewsActivity", "✅ Server Message: " + historiResponse.getMessage());
                    Log.d("NewsActivity", "✅ Data Count: " + historiResponse.getDataCount());

                    if (!historiResponse.isSuccess()) {
                        Log.e("NewsActivity", "❌ Server returned error: " + historiResponse.getMessage());
                        Toast.makeText(NewsActivity.this,
                                "Server error: " + historiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        loadLocalNewsData();
                        return;
                    }

                    if (historiResponse.hasData()) {
                        Log.d("NewsActivity", "🎉 Data received: " + historiResponse.getDataCount() + " items");
                        convertAndDisplayHistoriData(historiResponse.getData());
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
                t.printStackTrace();
                loadLocalNewsData();
            }
        });
    }

    private void convertAndDisplayHistoriData(List<NewsHistoriItem> historiItems) {
        Log.d("NewsActivity", "🔄 Converting " + historiItems.size() + " histori items");

        newsItems.clear();

        int successCount = 0;
        int errorCount = 0;
        int withImageCount = 0;
        int validImageCount = 0;

        for (NewsHistoriItem historiItem : historiItems) {
            try {
                Date timestamp;
                try {
                    java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timestamp = format.parse(historiItem.getTimestamp());
                } catch (Exception e) {
                    Log.w("NewsActivity", "⚠ Using current date for invalid timestamp: " + historiItem.getTimestamp());
                    timestamp = new Date();
                }

                String status = mapStatusToConsistent(historiItem.getStatus());

                String imageData = null;
                boolean hasImageData = false;
                boolean isValidImage = false;

                if (historiItem.getImage_base64() != null &&
                        !historiItem.getImage_base64().isEmpty() &&
                        !historiItem.getImage_base64().equals("null")) {

                    hasImageData = true;
                    withImageCount++;

                    String rawImageData = historiItem.getImage_base64().trim();
                    Log.d("NewsActivity", "📷 RAW Image data for: " + historiItem.getTitle() +
                            " | Length: " + rawImageData.length() +
                            " | First 50 chars: " + (rawImageData.length() > 50 ?
                            rawImageData.substring(0, 50) + "..." : rawImageData));

                    if (rawImageData.length() > 100) {
                        if (rawImageData.matches("^[a-zA-Z0-9+/]*={0,2}$")) {
                            try {
                                byte[] testDecode = Base64.decode(rawImageData, Base64.DEFAULT);
                                if (testDecode != null && testDecode.length > 0) {
                                    imageData = rawImageData;
                                    isValidImage = true;
                                    validImageCount++;
                                    Log.d("NewsActivity", "✅ VALID Base64 - Decoded bytes: " + testDecode.length);
                                } else {
                                    Log.w("NewsActivity", "❌ Base64 decode returned null/empty");
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e("NewsActivity", "❌ INVALID Base64 format: " + e.getMessage());
                            }
                        } else {
                            Log.w("NewsActivity", "❌ NOT Base64 - Contains invalid characters");
                        }
                    } else {
                        Log.w("NewsActivity", "❌ Image data too short: " + rawImageData.length() + " chars");
                    }
                } else {
                    Log.d("NewsActivity", "📷 No image data for: " + historiItem.getTitle() +
                            " | image_base64: " + (historiItem.getImage_base64() == null ? "null" :
                            "empty/" + historiItem.getImage_base64()));
                }

                if (hasImageData) {
                    if (isValidImage) {
                        Log.d("NewsActivity", "🎉 USING Image for: " + historiItem.getTitle());
                    } else {
                        Log.w("NewsActivity", "🚫 SKIPPING Invalid image for: " + historiItem.getTitle());
                    }
                }

                NewsItem newsItem = new NewsItem(
                        historiItem.getId_news(),
                        historiItem.getTitle(),
                        historiItem.getPenginput(),
                        status,
                        timestamp,
                        imageData,
                        historiItem.getPromo_id()
                );

                newsItems.add(newsItem);
                successCount++;

                Log.d("NewsActivity", "✅ Added NewsItem: " + newsItem.getTitle() +
                        " | Status: " + newsItem.getStatus() +
                        " | Has Image: " + (imageData != null) +
                        " | Time: " + newsItem.getTime());

            } catch (Exception e) {
                errorCount++;
                Log.e("NewsActivity", "❌ Error converting item: " + historiItem.getTitle() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }

        sortNewsByTimestamp();
        saveNewsData();
        newsAdapter.notifyDataSetChanged();

        Log.d("NewsActivity", "🎉 CONVERSION SUMMARY:");
        Log.d("NewsActivity", "   📊 Total Items: " + historiItems.size());
        Log.d("NewsActivity", "   ✅ Success: " + successCount);
        Log.d("NewsActivity", "   ❌ Errors: " + errorCount);
        Log.d("NewsActivity", "   📷 With Image Data: " + withImageCount);
        Log.d("NewsActivity", "   ✅ Valid Images: " + validImageCount);
        Log.d("NewsActivity", "   🚫 Invalid Images: " + (withImageCount - validImageCount));
        Log.d("NewsActivity", "   📱 Displaying: " + newsItems.size() + " news items");

        if (newsItems.size() > 0) {
            String toastMessage = "Loaded " + newsItems.size() + " news items";
            if (validImageCount > 0) {
                toastMessage += " (" + validImageCount + " with images)";
            }
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No news items found", Toast.LENGTH_LONG).show();
        }
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

    private void refreshNewsData() {
        swipeRefreshLayout.setRefreshing(true);
        loadNewsHistoriFromServer();
    }

    private void loadNewsData() {
        loadNewsHistoriFromServer();
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

    @Override
    protected void onResume() {
        super.onResume();
        checkForPromoUpdates();
    }
}