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
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerNews = findViewById(R.id.recyclerNews);
        swipeRefreshLayout = new SwipeRefreshLayout(this);

        // Setup SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Create notification channel
        createNotificationChannel();

        bottomNavigationView.setSelectedItemId(R.id.nav_news);

        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(NewsActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, BerandaActivity.class));
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

    private void loadNewsData() {
        // First try to load from SharedPreferences
        String json = sharedPreferences.getString(NEWS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<NewsItem>>(){}.getType();
            List<NewsItem> savedNews = gson.fromJson(json, type);
            if (savedNews != null) {
                newsItems.clear();
                newsItems.addAll(savedNews);
                newsAdapter.notifyDataSetChanged();

                // Remove items older than 7 days
                removeOldNews();
                return;
            }
        }

        // If no saved data, load from API
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

    private void processPromoData(List<Promo> promoList) {
        List<NewsItem> newItems = new ArrayList<>();

        for (Promo promo : promoList) {
            boolean exists = false;
            for (NewsItem newsItem : newsItems) {
                if (newsItem.getPromoId() == promo.getIdPromo()) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                String imageData = promo.getGambarBase64();

                // Handle base64 image data
                String imageUrl = null;
                if (imageData != null && !imageData.isEmpty()) {
                    // Jika data adalah base64, kita simpan sebagai string base64
                    // NewsAdapter akan menangani decoding-nya
                    imageUrl = imageData;
                }

                NewsItem newItem = new NewsItem(
                        newsItems.size() + 1,
                        promo.getNamaPromo(),
                        promo.getNamaPenginput(),
                        "Ditambahkan",
                        new Date(),
                        imageUrl, // Ini sekarang berisi string base64
                        promo.getIdPromo()
                );

                newItems.add(newItem);
                showNotification(newItem);
            }
        }

        // Tambahkan semua item baru sekaligus
        if (!newItems.isEmpty()) {
            newsItems.addAll(0, newItems);
            saveNewsData();
            newsAdapter.notifyDataSetChanged();
            removeOldNews();
        }
    }

    private void saveNewsData() {
        String json = gson.toJson(newsItems);
        sharedPreferences.edit().putString(NEWS_KEY, json).apply();
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

        // Set alarm to trigger at 2:00 AM daily
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);

        // If it's already past 2:00 AM, schedule for next day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Check for permissions on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            } else {
                // Handle case where permission is not granted
                Log.w("NewsActivity", "Cannot schedule exact alarms - permission not granted");
                // You might want to request the permission here
                requestAlarmPermission();
            }
        } else {
            // For older versions, just set the alarm
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
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, don't show notification
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        refreshNewsData();
    }
}