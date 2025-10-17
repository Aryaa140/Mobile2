package com.example.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";
    private static final String WORK_NAME = "promo_notification_worker";

    public static void schedulePromoNotifications(Context context) {
        Log.d(TAG, "📅 Scheduling promo notifications...");

        // Constraints: Hanya jalan ketika ada koneksi internet
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Schedule setiap 15 menit
        PeriodicWorkRequest notificationWork =
                new PeriodicWorkRequest.Builder(
                        NotificationWorker.class,
                        15, // Setiap 15 menit
                        TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .build();

        // Enqueue dengan policy KEEP (jangan duplicate)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
        );

        Log.d(TAG, "✅ Promo notifications scheduled every 15 minutes");
    }

    public static void cancelPromoNotifications(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
        Log.d(TAG, "❌ Promo notifications cancelled");
    }
}