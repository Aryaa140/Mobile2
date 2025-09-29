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

        // Setup SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Create notification channel
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
                // Already in NewsActivity
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
        newsAdapter = new NewsAdapter(this, newsItems);
        recyclerNews.setAdapter(newsAdapter);

        // Load data from SharedPreferences or API
        loadNewsData();

        // Setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshNewsData);

        // Setup swipe to dismiss
        setupSwipeToDismiss();

        // Schedule daily cleanup of old news
        scheduleDailyCleanup();

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

    // METHOD YANG DIPERBAIKI: CEK UPDATE DARI SHARED PREFERENCES
    private void checkForPromoUpdates() {
        long lastCheckTime = getLastNewsCheckTime();
        long currentTime = System.currentTimeMillis();

        // Cek update promo (dalam 5 menit terakhir)
        long lastUpdateTime = newsUpdatePrefs.getLong("last_update_time", 0);
        if (lastUpdateTime > lastCheckTime) {
            int promoId = newsUpdatePrefs.getInt("last_updated_promo_id", -1);
            String status = newsUpdatePrefs.getString("last_updated_status", "Diubah"); // PASTIKAN DEFAULT "Diubah"
            String updatedImage = newsUpdatePrefs.getString("last_updated_image", "");

            if (promoId != -1) {
                addNewsItemForUpdatedPromo(promoId, status, updatedImage);
            }
        }

        // Cek delete promo (dalam 5 menit terakhir)
        long lastDeleteTime = newsUpdatePrefs.getLong("last_delete_time", 0);
        if (lastDeleteTime > lastCheckTime) {
            String deletedTitle = newsUpdatePrefs.getString("last_deleted_title", "");
            String deletedInputter = newsUpdatePrefs.getString("last_deleted_inputter", "");
            String status = newsUpdatePrefs.getString("last_deleted_status", "Dihapus"); // PASTIKAN DEFAULT "Dihapus"

            if (!deletedTitle.isEmpty()) {
                addNewsItemForDeletedPromo(deletedTitle, deletedInputter, status);
            }
        }

        // Update waktu check terakhir
        saveLastNewsCheckTime(currentTime);
    }

    // METHOD YANG DIPERBAIKI: TAMBAH NEWS ITEM UNTUK PROMO YANG DIHAPUS
    private void addNewsItemForDeletedPromo(String promoTitle, String penginput, String status) {
        // Cek apakah sudah ada item untuk promo ini dengan status Dihapus
        boolean alreadyExists = false;
        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) && item.getStatus().equals("Dihapus")) {
                alreadyExists = true;
                break;
            }
        }

        if (!alreadyExists) {
            // Cari promo yang dihapus untuk mendapatkan gambar terakhir
            String lastImageUrl = findLastImageForDeletedPromo(promoTitle);

            NewsItem deletedItem = new NewsItem(
                    generateNewId(),
                    promoTitle,
                    penginput,
                    "Dihapus", // PASTIKAN STATUSNYA "Dihapus"
                    new Date(),
                    lastImageUrl, // GUNAKAN GAMBAR TERAKHIR YANG DITEMUKAN
                    -1 // ID negatif menandakan sudah dihapus
            );

            newsItems.add(0, deletedItem);
            sortNewsByTimestamp(); // URUTKAN ULANG
            saveNewsData();
            newsAdapter.notifyDataSetChanged();
            showNotification(deletedItem);

            Toast.makeText(this, "Promo '" + promoTitle + "' " + status.toLowerCase(), Toast.LENGTH_SHORT).show();
        }
    }

    // METHOD BARU: CARI GAMBAR TERAKHIR UNTUK PROMO YANG DIHAPUS
    private String findLastImageForDeletedPromo(String promoTitle) {
        for (NewsItem item : newsItems) {
            if (item.getTitle().equals(promoTitle) && item.getPromoId() > 0) {
                // Kembalikan gambar dari item yang masih ada
                return item.getImageUrl();
            }
        }
        return null; // Jika tidak ditemukan, kembalikan null
    }

    // METHOD YANG DIPERBAIKI: REFRESH DATA DENGAN UPDATE KHUSUS
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
                        processPromoDataWithUpdate(promoResponse.getData(), updatedPromoId, status, updatedImage);
                    } else {
                        Toast.makeText(NewsActivity.this, "Gagal memuat data: " + promoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NewsActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(NewsActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // METHOD YANG DIPERBAIKI: PROCESS DATA DENGAN UPDATE KHUSUS
    private void processPromoDataWithUpdate(List<Promo> promoList, int updatedPromoId, String status, String updatedImage) {
        boolean promoFound = false;

        // Cari promo yang diupdate di data server
        for (Promo promo : promoList) {
            if (promo.getIdPromo() == updatedPromoId) {
                promoFound = true;

                // Cek apakah sudah ada news item untuk promo ini
                NewsItem existingItem = findNewsItemByPromoId(updatedPromoId);

                if (existingItem != null) {
                    // JANGAN UBAH JIKA SUDAH DIHAPUS
                    if (existingItem.getStatus().equals("Dihapus")) {
                        Log.w("NewsActivity", "Cannot update deleted item: " + promo.getNamaPromo());
                        break;
                    }

                    // PERBAIKAN: UPDATE STATUS HANYA JIKA DITERIMA DARI EDIT
                    if (status.equals("Diubah") || status.equals("Dihapus")) {
                        existingItem.setStatus(status);
                    }

                    // Update data lainnya
                    existingItem.setTitle(promo.getNamaPromo());
                    existingItem.setPenginput(promo.getNamaPenginput());
                    existingItem.setTimestamp(new Date());

                    // Prioritaskan gambar yang di-update
                    if (updatedImage != null && !updatedImage.isEmpty()) {
                        existingItem.setImageUrl(updatedImage);
                    } else {
                        existingItem.setImageUrl(promo.getGambarBase64());
                    }

                    Log.d("NewsActivity", "✅ PROMO MANUALLY UPDATED: " + promo.getNamaPromo() + " | New Status: " + status);
                } else {
                    // Buat item baru jika tidak ada
                    NewsItem updatedItem = new NewsItem(
                            generateNewId(),
                            promo.getNamaPromo(),
                            promo.getNamaPenginput(),
                            status,
                            new Date(),
                            updatedImage != null && !updatedImage.isEmpty() ? updatedImage : promo.getGambarBase64(),
                            promo.getIdPromo()
                    );
                    newsItems.add(0, updatedItem);
                }

                sortNewsByTimestamp();
                saveNewsData();
                newsAdapter.notifyDataSetChanged();

                if (existingItem != null) {
                    showNotification(existingItem);
                }

                Toast.makeText(this, "Promo '" + promo.getNamaPromo() + "' " + status.toLowerCase(), Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (!promoFound) {
            Log.w("NewsActivity", "Updated promo not found in server data");
        }
    }

    // METHOD BARU: URUTKAN BERDASARKAN TIMESTAMP (TERBARU DI ATAS)
    private void sortNewsByTimestamp() {
        Collections.sort(newsItems, new Comparator<NewsItem>() {
            @Override
            public int compare(NewsItem item1, NewsItem item2) {
                return item2.getTimestamp().compareTo(item1.getTimestamp()); // Descending
            }
        });
    }

    // METHOD YANG DIPERBAIKI: LOAD DATA DARI SHARED PREFERENCES
    private void loadNewsData() {
        // First try to load from SharedPreferences
        String json = sharedPreferences.getString(NEWS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<NewsItem>>(){}.getType();
            List<NewsItem> savedNews = gson.fromJson(json, type);
            if (savedNews != null) {
                newsItems.clear();
                newsItems.addAll(savedNews);
                sortNewsByTimestamp(); // URUTKAN SAAT LOAD
                newsAdapter.notifyDataSetChanged();
                removeOldNews();
                return;
            }
        }
        // If no saved data, load from API
        refreshNewsData();
    }

    // METHOD YANG DIPERBAIKI: PROCESS DATA DARI SERVER
    private void processPromoData(List<Promo> promoList) {
        List<NewsItem> newItems = new ArrayList<>();

        // Buat list promo IDs yang sudah ada di newsItems
        List<Integer> existingPromoIds = new ArrayList<>();
        for (NewsItem item : newsItems) {
            if (item.getPromoId() > 0) {
                existingPromoIds.add(item.getPromoId());
            }
        }

        // Proses setiap promo dari server
        for (Promo promo : promoList) {
            // Cek apakah promo ini baru (belum ada di newsItems)
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
                showNotification(newItem);

                Log.d("NewsActivity", "✅ NEW PROMO DETECTED: " + promo.getNamaPromo());
            } else {
                // Update existing promo jika ada perubahan
                updateExistingPromoIfChanged(promo);
            }
        }

        // Tambahkan semua item baru sekaligus
        if (!newItems.isEmpty()) {
            newsItems.addAll(0, newItems);
            sortNewsByTimestamp(); // URUTKAN SETELAH MENAMBAH ITEM BARU
            saveNewsData();
            newsAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Ditemukan " + newItems.size() + " promo baru",
                    Toast.LENGTH_SHORT).show();
        }

        // Handle promo yang dihapus dari server
        removeDeletedPromos(promoList);
        removeOldNews();
    }

    // METHOD YANG DIPERBAIKI: HAPUS NEWS ITEM UNTUK PROMO YANG SUDAH DIHAPUS DARI SERVER
    private void removeDeletedPromos(List<Promo> currentPromos) {
        List<NewsItem> itemsToRemove = new ArrayList<>();
        List<NewsItem> itemsToAddAsDeleted = new ArrayList<>();

        for (NewsItem newsItem : newsItems) {
            if (newsItem.getPromoId() > 0) { // Hanya untuk item yang punya promoId valid
                boolean promoStillExists = false;
                for (Promo promo : currentPromos) {
                    if (promo.getIdPromo() == newsItem.getPromoId()) {
                        promoStillExists = true;
                        break;
                    }
                }

                // PERBAIKAN: HANYA TANDAI SEBAGAI DIHAPUS JIKA STATUSNYA BELUM "Dihapus"
                // DAN PROMO TIDAK ADA DI SERVER
                if (!promoStillExists && !newsItem.getStatus().equals("Dihapus")) {
                    // Simpan gambar sebelum menghapus
                    String deletedImageUrl = newsItem.getImageUrl();

                    // Buat item penghapusan DENGAN GAMBAR
                    NewsItem deletedItem = new NewsItem(
                            generateNewId(),
                            newsItem.getTitle(),
                            newsItem.getPenginput(),
                            "Dihapus", // STATUS DIUBAH MENJADI DIHAPUS
                            new Date(),
                            deletedImageUrl, // PASTIKAN GAMBAR TERSIMPAN
                            -1
                    );
                    itemsToAddAsDeleted.add(deletedItem);
                    itemsToRemove.add(newsItem);

                    Log.d("NewsActivity", "✅ PROMO DELETED FROM SERVER: " + newsItem.getTitle());
                }
            }
        }

        // Hapus item lama dan tambahkan item deleted
        newsItems.removeAll(itemsToRemove);
        if (!itemsToAddAsDeleted.isEmpty()) {
            newsItems.addAll(0, itemsToAddAsDeleted);
            sortNewsByTimestamp(); // URUTKAN SETELAH PERUBAHAN
        }

        if (!itemsToRemove.isEmpty() || !itemsToAddAsDeleted.isEmpty()) {
            saveNewsData();
            newsAdapter.notifyDataSetChanged();

            // Tampilkan notifikasi untuk setiap item yang dihapus
            for (NewsItem deletedItem : itemsToAddAsDeleted) {
                showNotification(deletedItem);
            }
        }
    }

    // METHOD YANG DIPERBAIKI: SIMPAN DATA DENGAN URUTAN YANG BENAR
    private void saveNewsData() {
        sortNewsByTimestamp(); // URUTKAN SEBELUM SIMPAN
        String json = gson.toJson(newsItems);
        sharedPreferences.edit().putString(NEWS_KEY, json).apply();
    }

    // METHOD-METHOD YANG TIDAK BERUBAH (tetap diperlukan)
    private void addNewsItemForUpdatedPromo(int promoId, String status, String updatedImage) {
        refreshNewsDataWithUpdate(promoId, status, updatedImage);
    }

    private NewsItem findNewsItemByPromoId(int promoId) {
        for (NewsItem item : newsItems) {
            if (item.getPromoId() == promoId) {
                return item;
            }
        }
        return null;
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
        SharedPreferences prefs = getSharedPreferences("NewsActivityCheck", MODE_PRIVATE);
        return prefs.getLong("last_news_check_time", 0);
    }

    private void saveLastNewsCheckTime(long time) {
        SharedPreferences prefs = getSharedPreferences("NewsActivityCheck", MODE_PRIVATE);
        prefs.edit().putLong("last_news_check_time", time).apply();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "News Channel";
            String description = "Channel for news notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
                    } else {
                        Toast.makeText(NewsActivity.this, "Gagal memuat data: " + promoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NewsActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(NewsActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("NewsActivity", "Error: " + t.getMessage());
            }
        });
    }

    private void updateExistingPromoIfChanged(Promo promo) {
        for (NewsItem newsItem : newsItems) {
            if (newsItem.getPromoId() == promo.getIdPromo()) {
                // JANGAN UPDATE JIKA SUDAH DIHAPUS
                if (newsItem.getStatus().equals("Dihapus")) {
                    Log.d("NewsActivity", "Item already deleted, skipping update: " + promo.getNamaPromo());
                    break;
                }

                // CEK PERUBAHAN DATA
                boolean hasChanges = !newsItem.getTitle().equals(promo.getNamaPromo()) ||
                        !newsItem.getPenginput().equals(promo.getNamaPenginput()) ||
                        (promo.getGambarBase64() != null &&
                                !promo.getGambarBase64().equals(newsItem.getImageUrl()));

                if (hasChanges) {
                    // PERBAIKAN: UPDATE DATA TANPA MENGUBAH STATUS
                    // Status hanya diubah melalui mekanisme khusus (edit/delete)
                    newsItem.setTitle(promo.getNamaPromo());
                    newsItem.setPenginput(promo.getNamaPenginput());
                    newsItem.setImageUrl(promo.getGambarBase64());
                    newsItem.setTimestamp(new Date());
                    // STATUS TIDAK DIUBAH di sini

                    Log.d("NewsActivity", "✅ PROMO DATA SYNCED: " + promo.getNamaPromo() + " | Status remains: " + newsItem.getStatus());
                }
                break;
            }
        }
    }

    private void setupSwipeToDismiss() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                newsAdapter.removeItem(position);
                saveNewsData();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerNews);
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
        Intent intent = new Intent(this, NewsCleanupReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            } else {
                Log.w("NewsActivity", "Cannot schedule exact alarms - permission not granted");
                requestAlarmPermission();
            }
        } else {
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
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e("NewsActivity", "Failed to open alarm permission settings", e);
            }
        }
    }

    private void showNotification(NewsItem newsItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Promo Baru: " + newsItem.getTitle())
                .setContentText("Status: " + newsItem.getStatus() + " oleh " + newsItem.getPenginput())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        try {
            notificationManager.notify(new Random().nextInt(), builder.build());
        } catch (SecurityException e) {
            Log.e("NewsActivity", "Notification permission denied", e);
        }
    }
}