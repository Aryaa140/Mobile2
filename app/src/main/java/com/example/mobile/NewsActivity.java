package com.example.mobile;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "news_channel";
    private static final String PREF_NAME = "news_prefs";
    private static final String NEWS_KEY = "news_items";
    private final Object updateLock = new Object(); // Tambahkan lock object

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

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



    @Override
    protected void onResume() {
        super.onResume();
        checkForPromoUpdates();
    }

    // SOLUSI BARU: Sistem yang lebih sederhana
    private void checkForPromoUpdates() {
        long lastCheckTime = getLastNewsCheckTime();
        long currentTime = System.currentTimeMillis();

        Log.d("NewsActivity", "=== CHECKING PROMO UPDATES ===");

        synchronized (updateLock) {
            // PRIORITAS 1: Cek DELETE dulu (karena lebih penting)
            boolean hasDelete = checkForDeleteOnly(lastCheckTime);

            // PRIORITAS 2: Cek UPDATE hanya jika tidak ada delete
            if (!hasDelete) {
                checkForUpdateOnly(lastCheckTime);
            }
        }

        saveLastNewsCheckTime(currentTime);
    }

    // Ubah method checkForDeleteOnly untuk return boolean
    private boolean checkForDeleteOnly(long lastCheckTime) {
        long lastDeleteTime = newsUpdatePrefs.getLong("last_delete_time", 0);

        if (lastDeleteTime > lastCheckTime) {
            String deletedTitle = newsUpdatePrefs.getString("last_deleted_title", "");
            String deletedInputter = newsUpdatePrefs.getString("last_deleted_inputter", "");

            Log.d("NewsActivity", "🗑️ PROCESSING DELETE - Title: " + deletedTitle);

            if (!deletedTitle.isEmpty()) {
                processDirectDelete(deletedTitle, deletedInputter);
                clearDeleteData();
                return true; // ADA DELETE YANG DIPROSES
            }
        }
        return false; // TIDAK ADA DELETE
    }

    private void checkForUpdateOnly(long lastCheckTime) {
        long lastUpdateTime = newsUpdatePrefs.getLong("last_update_time", 0);

        if (lastUpdateTime > lastCheckTime) {
            int promoId = newsUpdatePrefs.getInt("last_updated_promo_id", -1);
            String status = newsUpdatePrefs.getString("last_updated_status", "Diubah");
            String updatedImage = newsUpdatePrefs.getString("last_updated_image", "");

            Log.d("NewsActivity", "🔄 PROCESSING UPDATE - ID: " + promoId + ", Status: " + status);

            if (promoId != -1) {
                // Langsung proses update tanpa API call
                processDirectUpdate(promoId, status, updatedImage);
                clearUpdateData();
            }
        }
    }

    private void sortAndSaveData() {
        sortNewsByTimestamp();
        saveNewsData();
        newsAdapter.notifyDataSetChanged();
    }

    // METHOD-METHOD CLEAR DATA
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


    // NOTIFICATION METHODS
    private void showNotificationForUpdate(NewsItem item) {
        showNotification("Promo Diupdate", "Promo '" + item.getTitle() + "' telah diperbarui");
    }

    private void showNotificationForDelete(String title, String penginput) {
        showNotification("Promo Dihapus", "Promo '" + title + "' telah dihapus oleh " + penginput);
    }

    // METHOD-METHOD YANG SUDAH ADA (dengan perbaikan kecil)
    private void refreshNewsDataWithUpdate(int updatedPromoId, String status, String updatedImage) {
        swipeRefreshLayout.setRefreshing(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        processPromoDataForUpdate(promoResponse.getData(), updatedPromoId, status, updatedImage);
                    }
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void processDirectDelete(String promoTitle, String penginput) {
        Log.d("NewsActivity", "🗑️ PROCESSING DIRECT DELETE: " + promoTitle);

        // CARI ITEM YANG SESUAI: hanya item dengan promoId > 0 dan status bukan "Dihapus"
        NewsItem itemToDelete = null;

        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) &&
                    item.getPromoId() > 0 &&
                    !item.getStatus().equals("Dihapus")) {

                itemToDelete = item;
                Log.d("NewsActivity", "Found ACTIVE item to delete: " + item.getTitle() +
                        " | Status: " + item.getStatus() + " | PromoId: " + item.getPromoId());
                break; // Hentikan setelah menemukan item aktif pertama
            }
        }

        if (itemToDelete != null) {
            // SIMPAN DATA SEBELUM DIHAPUS
            String currentImage = itemToDelete.getImageUrl();
            int originalPromoId = itemToDelete.getPromoId();

            // UBAH STATUS MENJADI DIHAPUS
            itemToDelete.setStatus("Dihapus");
            itemToDelete.setTimestamp(new Date());
            itemToDelete.setPromoId(-1); // Tandai sebagai dihapus

            // PASTIKAN GAMBAR TIDAK BERUBAH
            if (currentImage != null) {
                itemToDelete.setImageUrl(currentImage);
            }

            Log.d("NewsActivity", "✅ DELETED ITEM: " + itemToDelete.getTitle() +
                    " | Original Status: " + itemToDelete.getStatus() +
                    " | PromoId: " + originalPromoId + " → -1" +
                    " | Image: " + (currentImage != null ? currentImage.length() + " chars" : "null"));

            sortAndSaveData();
            showNotificationForDelete(promoTitle, penginput);
            Toast.makeText(this, "Promo '" + promoTitle + "' dihapus", Toast.LENGTH_SHORT).show();

        } else {
            Log.d("NewsActivity", "No ACTIVE items found to delete: " + promoTitle);

            // BUAT ITEM DELETE BARU JIKA TIDAK ADA ITEM AKTIF
            createNewDeleteItem(promoTitle, penginput);
        }
    }

    // METHOD BARU: BUAT ITEM DELETE BARU JIKA TIDAK ADA ITEM YANG COCOK
    private void createNewDeleteItem(String promoTitle, String penginput) {
        Log.d("NewsActivity", "Creating new delete item for: " + promoTitle);

        // Coba dapatkan gambar terakhir dari SharedPreferences atau dari data yang ada
        String lastImage = getLastImageForPromo(promoTitle);

        NewsItem deletedItem = new NewsItem(
                generateNewId(),
                promoTitle,
                penginput,
                "Dihapus",
                new Date(),
                lastImage, // Gunakan gambar yang ditemukan
                -1
        );

        newsItems.add(0, deletedItem);
        sortAndSaveData();
        showNotificationForDelete(promoTitle, penginput);

        Log.d("NewsActivity", "✅ CREATED NEW DELETE ITEM: " + promoTitle + " | Image: " +
                (lastImage != null ? lastImage.length() + " chars" : "null"));
    }

    // METHOD YANG DIPERBAIKI: CARI GAMBAR UNTUK PROMO YANG DIHAPUS
    // METHOD YANG DIPERBAIKI: CARI GAMBAR UNTUK PROMO YANG DIHAPUS
    // DI NewsActivity.java - PERBAIKI METHOD getLastImageForPromo()
    private String getLastImageForPromo(String promoTitle) {
        Log.d("NewsActivity", "🔍 Searching for last image of: " + promoTitle);

        // PRIORITY 1: Cari dari item yang MASIH AKTIF (belum dihapus)
        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) &&
                    item.getPromoId() > 0 &&
                    !item.getStatus().equals("Dihapus")) {
                Log.d("NewsActivity", "✅ Found ACTIVE item with image: " + item.getTitle());
                return item.getImageUrl();
            }
        }

        // PRIORITY 2: Cari dari item yang SUDAH DIHAPUS (paling baru)
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

        // PRIORITY 3: Default ke null (akan menggunakan placeholder di adapter)
        Log.d("NewsActivity", "❌ No suitable image found for: " + promoTitle);
        return null;
    }

    // METHOD YANG DIPERBAIKI: PROSES UPDATE LANGSUNG
    private void processDirectUpdate(int promoId, String status, String updatedImage) {
        Log.d("NewsActivity", "🔄 PROCESSING DIRECT UPDATE - ID: " + promoId);

        // VALIDASI: Pastikan data update valid
        if (promoId <= 0) {
            Log.e("NewsActivity", "❌ INVALID PROMO ID FOR UPDATE: " + promoId);
            return;
        }

        // Cari item yang sudah ada
        NewsItem existingItem = findNewsItemByPromoId(promoId);

        if (existingItem != null) {
            // VALIDASI: Jangan update jika item sudah dihapus
            if (existingItem.getStatus().equals("Dihapus")) {
                Log.d("NewsActivity", "⚠️ Cannot update deleted item: " + existingItem.getTitle());

                // BUAT ITEM BARU untuk update karena yang lama sudah dihapus
                refreshNewsDataWithUpdate(promoId, status, updatedImage);
                return;
            }

            // SIMPAN DATA SEBELUM UPDATE UNTUK ROLLBACK JIKA PERLU
            String oldImage = existingItem.getImageUrl();
            String oldStatus = existingItem.getStatus();

            // UPDATE ITEM YANG SUDAH ADA
            existingItem.setStatus("Diubah");
            existingItem.setTimestamp(new Date());

            // VALIDASI GAMBAR: Hanya update gambar jika ada gambar baru yang valid
            if (updatedImage != null && !updatedImage.isEmpty() && !updatedImage.equals(oldImage)) {
                existingItem.setImageUrl(updatedImage);
                Log.d("NewsActivity", "🖼️ Image updated for: " + existingItem.getTitle());
            } else {
                Log.d("NewsActivity", "🖼️ No image change for: " + existingItem.getTitle());
            }

            Log.d("NewsActivity", "✅ UPDATED EXISTING ITEM: " + existingItem.getTitle() +
                    " | Status: " + oldStatus + " → " + existingItem.getStatus() +
                    " | Image changed: " + (updatedImage != null && !updatedImage.equals(oldImage)));

            sortAndSaveData();
            showNotificationForUpdate(existingItem);
            Toast.makeText(this, "Promo '" + existingItem.getTitle() + "' diperbarui", Toast.LENGTH_SHORT).show();

        } else {
            // BUAT ITEM BARU untuk update
            Log.d("NewsActivity", "No existing item found, creating new one for update");
            refreshNewsDataWithUpdate(promoId, status, updatedImage);
        }
    }

    // METHOD YANG DIPERBAIKI: PROCESS DATA UNTUK UPDATE
    private void processPromoDataForUpdate(List<Promo> promoList, int updatedPromoId, String status, String updatedImage) {
        Log.d("NewsActivity", "🔄 PROCESSING DATA FOR UPDATE - ID: " + updatedPromoId);

        for (Promo promo : promoList) {
            if (promo.getIdPromo() == updatedPromoId) {
                NewsItem existingItem = findNewsItemByPromoId(updatedPromoId);

                if (existingItem == null) {
                    // Buat item baru dengan gambar yang tepat
                    String imageToUse = updatedImage != null && !updatedImage.isEmpty() ?
                            updatedImage : promo.getGambarBase64();

                    NewsItem newItem = new NewsItem(
                            generateNewId(),
                            promo.getNamaPromo(),
                            promo.getNamaPenginput(),
                            "Diubah",
                            new Date(),
                            imageToUse,
                            promo.getIdPromo()
                    );
                    newsItems.add(0, newItem);
                    Log.d("NewsActivity", "✅ CREATED NEW ITEM FOR UPDATE: " + promo.getNamaPromo() +
                            " | Image: " + (imageToUse != null ? imageToUse.length() + " chars" : "null"));
                } else {
                    // Update item yang sudah ada
                    if (!existingItem.getStatus().equals("Dihapus")) {
                        String oldImage = existingItem.getImageUrl();

                        // Prioritaskan gambar yang di-update
                        if (updatedImage != null && !updatedImage.isEmpty()) {
                            existingItem.setImageUrl(updatedImage);
                        } else {
                            existingItem.setImageUrl(promo.getGambarBase64());
                        }

                        existingItem.setStatus("Diubah");
                        existingItem.setTimestamp(new Date());

                        Log.d("NewsActivity", "✅ UPDATED EXISTING ITEM FROM SERVER: " + existingItem.getTitle() +
                                " | Image changed: " + !oldImage.equals(existingItem.getImageUrl()));
                    }
                }

                sortAndSaveData();
                break;
            }
        }
    }

    private void loadNewsData() {
        String json = sharedPreferences.getString(NEWS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<NewsItem>>(){}.getType();
            List<NewsItem> savedNews = gson.fromJson(json, type);
            if (savedNews != null) {
                newsItems.clear();
                newsItems.addAll(savedNews);
                sortNewsByTimestamp();
                newsAdapter.notifyDataSetChanged();
                removeOldNews();
                return;
            }
        }
        refreshNewsData();
    }

    private void refreshNewsData() {
        swipeRefreshLayout.setRefreshing(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        processPromoData(promoResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void processPromoData(List<Promo> promoList) {
        List<NewsItem> newItems = new ArrayList<>();
        List<Integer> existingPromoIds = new ArrayList<>();

        for (NewsItem item : newsItems) {
            if (item.getPromoId() > 0) {
                existingPromoIds.add(item.getPromoId());
            }
        }

        for (Promo promo : promoList) {
            if (!existingPromoIds.contains(promo.getIdPromo())) {
                NewsItem newItem = new NewsItem(
                        generateNewId(),
                        promo.getNamaPromo(),
                        promo.getNamaPenginput(),
                        "Ditambahkan",
                        new Date(),
                        promo.getGambarBase64(),
                        promo.getIdPromo()
                );
                newItems.add(newItem);
            }
        }

        if (!newItems.isEmpty()) {
            newsItems.addAll(0, newItems);
            sortAndSaveData();
            Toast.makeText(this, "Ditemukan " + newItems.size() + " promo baru", Toast.LENGTH_SHORT).show();
        }

        removeDeletedPromos(promoList);
        removeOldNews();
    }

    private void removeDeletedPromos(List<Promo> currentPromos) {
        List<NewsItem> itemsToMarkAsDeleted = new ArrayList<>();

        for (NewsItem newsItem : newsItems) {
            // HANYA PROSES ITEM DENGAN promoId > 0 DAN STATUS BUKAN "Dihapus"
            if (newsItem.getPromoId() > 0 && !newsItem.getStatus().equals("Dihapus")) {
                boolean promoStillExists = false;
                for (Promo promo : currentPromos) {
                    if (promo.getIdPromo() == newsItem.getPromoId()) {
                        promoStillExists = true;
                        break;
                    }
                }

                if (!promoStillExists) {
                    itemsToMarkAsDeleted.add(newsItem);
                    Log.d("NewsActivity", "🔄 Promo no longer exists on server: " + newsItem.getTitle() +
                            " | Current Status: " + newsItem.getStatus());
                }
            }
        }

        // MARK ITEMS AS DELETED
        for (NewsItem item : itemsToMarkAsDeleted) {
            String originalStatus = item.getStatus();
            item.setStatus("Dihapus");
            item.setPromoId(-1);
            Log.d("NewsActivity", "🔄 AUTO MARKED AS DELETED: " + item.getTitle() +
                    " | Status: " + originalStatus + " → Dihapus");
        }

        if (!itemsToMarkAsDeleted.isEmpty()) {
            sortAndSaveData();
        }
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

    private void sortNewsByTimestamp() {
        Collections.sort(newsItems, (item1, item2) ->
                item2.getTimestamp().compareTo(item1.getTimestamp()));
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
            notificationManager.createNotificationChannel(channel);
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
                newsAdapter.removeItem(position);
                saveNewsData();
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

        newsItems.removeAll(itemsToRemove);
        newsAdapter.notifyDataSetChanged();
        saveNewsData();
    }

    private void scheduleDailyCleanup() {
        // Implementation remains the same
        // ... existing scheduleDailyCleanup code ...
    }

    private void requestAlarmPermission() {
        // Implementation remains the same
        // ... existing requestAlarmPermission code ...
    }
}