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
    private static final String PROMO_WORK_NAME = "promo_notification_worker";
    private static final String PROYEK_WORK_NAME = "proyek_notification_worker";

    // ✅ SCHEDULE UNTUK PROMO
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
                PROMO_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
        );

        Log.d(TAG, "✅ Promo notifications scheduled every 15 minutes");
    }


    // ✅ CANCEL UNTUK PROMO
    public static void cancelPromoNotifications(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(PROMO_WORK_NAME);
        Log.d(TAG, "❌ Promo notifications cancelled");
    }

    // ✅ CANCEL UNTUK PROYEK
    public static void cancelProyekNotifications(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(PROYEK_WORK_NAME);
        Log.d(TAG, "❌ Project notifications cancelled");
    }

    // ✅ CANCEL SEMUA NOTIFIKASI
    public static void cancelAllNotifications(Context context) {
        cancelPromoNotifications(context);
        cancelProyekNotifications(context);
        Log.d(TAG, "❌ All notifications cancelled");
    }
}