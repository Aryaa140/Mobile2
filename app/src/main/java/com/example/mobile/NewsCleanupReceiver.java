package com.example.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsCleanupReceiver extends BroadcastReceiver {
    private static final String TAG = "NewsCleanupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🔄 Starting news cleanup...");

        SharedPreferences sharedPreferences = context.getSharedPreferences("news_prefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String json = sharedPreferences.getString("news_items", null);
        if (json != null) {
            Type type = new TypeToken<List<NewsItem>>(){}.getType();
            List<NewsItem> newsItems = gson.fromJson(json, type);

            if (newsItems != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                Date oneWeekAgo = calendar.getTime();

                List<NewsItem> itemsToKeep = new ArrayList<>();
                List<NewsItem> itemsToRemove = new ArrayList<>();

                for (NewsItem item : newsItems) {
                    Date itemDate = getItemTimestamp(item);
                    if (itemDate != null && itemDate.after(oneWeekAgo)) {
                        itemsToKeep.add(item);
                    } else {
                        itemsToRemove.add(item);
                        Log.d(TAG, "🗑️ Removing old item: " + item.getTitle() +
                                " | Date: " + itemDate +
                                " | Status: " + item.getStatus());
                    }
                }

                // Simpan data yang sudah dibersihkan
                String updatedJson = gson.toJson(itemsToKeep);
                sharedPreferences.edit().putString("news_items", updatedJson).apply();

                Log.d(TAG, "✅ Cleanup completed. Removed: " + itemsToRemove.size() +
                        " | Kept: " + itemsToKeep.size() +
                        " | Total before: " + newsItems.size());

                // Simpan log cleanup
                saveCleanupLog(context, itemsToRemove.size(), itemsToKeep.size());

            } else {
                Log.d(TAG, "📭 News items list is null");
            }
        } else {
            Log.d(TAG, "📭 No news items to clean up");
        }
    }

    // ✅ METHOD YANG DIPERBAIKI: AMBIL TIMESTAMP DARI NewsItem
    private Date getItemTimestamp(NewsItem item) {
        if (item == null) {
            Log.w(TAG, "⚠️ Item is null, using current date");
            return new Date();
        }

        try {
            // ✅ PRIORITAS 1: Gunakan timestamp langsung dari objek
            if (item.getTimestamp() != null) {
                return item.getTimestamp();
            }

            // ✅ PRIORITAS 2: Coba parse dari string time (jika ada method getTime())
            try {
                String timeMethod = item.getClass().getMethod("getTime").invoke(item).toString();
                if (timeMethod != null && !timeMethod.isEmpty()) {
                    // Coba berbagai format date
                    Date parsedDate = tryParseMultipleFormats(timeMethod);
                    if (parsedDate != null) {
                        return parsedDate;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "getTime() method not available or failed");
            }

            // ✅ PRIORITAS 3: Gunakan waktu saat ini sebagai fallback
            Log.w(TAG, "⚠️ Using current date for item: " + item.getTitle());
            return new Date();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting timestamp for item: " + item.getTitle() + " - " + e.getMessage());
            return new Date(); // Fallback ke tanggal sekarang
        }
    }

    // ✅ METHOD BARU: COBA PARSE DARI BERBAGAI FORMAT DATE
    private Date tryParseMultipleFormats(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        SimpleDateFormat[] formats = {
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US), // Format default Date.toString()
                new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        };

        for (SimpleDateFormat format : formats) {
            try {
                format.setLenient(false);
                return format.parse(dateString);
            } catch (ParseException e) {
                // Continue to next format
            }
        }

        Log.d(TAG, "❌ Could not parse date string: " + dateString);
        return null;
    }

    // ✅ METHOD BARU: SIMPAN LOG CLEANUP
    private void saveCleanupLog(Context context, int removedCount, int keptCount) {
        SharedPreferences cleanupPrefs = context.getSharedPreferences("cleanup_logs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cleanupPrefs.edit();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = timestamp + " - Removed: " + removedCount + " | Kept: " + keptCount;

        // Simpan log terbaru (maksimal 10 entri)
        String existingLogs = cleanupPrefs.getString("cleanup_history", "");
        String newLogs = logEntry + "\n" + existingLogs;

        // Batasi hanya 10 entri terbaru
        String[] logs = newLogs.split("\n");
        StringBuilder limitedLogs = new StringBuilder();
        for (int i = 0; i < Math.min(logs.length, 10); i++) {
            if (!logs[i].trim().isEmpty()) {
                limitedLogs.append(logs[i]).append("\n");
            }
        }

        editor.putString("cleanup_history", limitedLogs.toString());
        editor.putLong("last_cleanup_time", System.currentTimeMillis());
        editor.putInt("last_removed_count", removedCount);
        editor.apply();

        Log.d(TAG, "📝 Cleanup log saved: " + logEntry);
    }
}