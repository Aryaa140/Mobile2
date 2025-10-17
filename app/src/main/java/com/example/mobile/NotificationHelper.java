package com.example.mobile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Map;

/**
 * NOTIFICATION HELPER - VERSION OPTIMIZED FOR FCM
 * Digunakan untuk notifikasi lokal (jika diperlukan)
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "promo_notifications";
    private static final String CHANNEL_NAME = "Promo Notifications";

    /**
     * ✅ BUAT NOTIFICATION CHANNEL (WAJIB untuk Android 8.0+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifications for new promotions and updates");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

            // PERBAIKAN: Gunakan getSystemService dengan cara yang benar
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel created: " + CHANNEL_ID);
            }
        }
    }

    /**
     * ✅ TAMPILKAN NOTIFIKASI PROMO LOKAL (jika diperlukan)
     */
    public static void showPromoNotification(Context context, String title, String body, Map<String, String> data) {
        try {
            createNotificationChannel(context);

            // Intent untuk buka MainActivity ketika notifikasi diklik
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Tambahkan data tambahan ke intent
            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }

            // PendingIntent dengan FLAG_IMMUTABLE untuk Android 12+
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

            // Buat notifikasi
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(android.app.Notification.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

            // Tampilkan notifikasi
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                int notificationId = (int) System.currentTimeMillis(); // ID unik
                manager.notify(notificationId, builder.build());
                Log.d(TAG, "✅ Local notification shown: " + title);
            } else {
                Log.e(TAG, "❌ NotificationManager is null");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing local notification: " + e.getMessage());
        }
    }

    /**
     * ✅ DAPATKAN NOTIFICATION ICON
     */
    private static int getNotificationIcon() {
        // Gunakan icon yang tersedia di project Anda
        // Fallback ke icon default jika tidak ada
        return android.R.drawable.ic_dialog_info;
    }
}