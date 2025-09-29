package com.example.mobile;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {

    private static final String CHANNEL_ID = "promo_channel";
    private static final String CHANNEL_NAME = "Promo Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for promo operations";
    private static int NOTIFICATION_ID = 1000;

    // Method dasar untuk berbagai jenis notifikasi
    public static void showSuccessNotification(Context context, String message) {
        showSystemNotification(context, "Sukses!", message, android.R.drawable.ic_dialog_info);
    }

    public static void showErrorNotification(Context context, String message) {
        showSystemNotification(context, "Error!", message, android.R.drawable.ic_dialog_alert);
    }

    public static void showInfoNotification(Context context, String title, String message) {
        showSystemNotification(context, title, message, android.R.drawable.ic_dialog_info);
    }

    public static void showPromoAddedNotification(Context context, String promoTitle, String username) {
        String message;
        if (username != null && !username.isEmpty()) {
            message = "Promo '" + promoTitle + "' berhasil ditambahkan oleh " + username;
        } else {
            message = "Promo '" + promoTitle + "' berhasil ditambahkan";
        }
        showSystemNotification(context, "Promo Ditambahkan", message, android.R.drawable.ic_dialog_info);
    }

    // NOTIFIKASI UNTUK EDIT PROMO DENGAN USER INFO
    public static void showPromoUpdatedNotification(Context context, String promoTitle, String username) {
        createNotificationChannel(context);

        // Intent untuk ketika notifikasi diklik
        Intent intent = new Intent(context, NewBeranda.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Buat pesan dengan informasi user
        String message;
        if (username != null && !username.isEmpty()) {
            message = "Promo '" + promoTitle + "' berhasil diupdate oleh " + username;
        } else {
            message = "Promo '" + promoTitle + "' berhasil diupdate";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setContentTitle("Promo Diupdate")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.GREEN)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setLights(Color.GREEN, 1000, 1000);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(generateNotificationId(), builder.build());
            Log.d("NotificationUtils", "✅ Update notification shown for: " + promoTitle + " by " + username);
        } else {
            Log.w("NotificationUtils", "No notification permission for update");
        }
    }

    // NOTIFIKASI UNTUK HAPUS PROMO DENGAN USER INFO
    public static void showPromoDeletedNotification(Context context, String promoTitle, String username) {
        createNotificationChannel(context);

        // Intent untuk ketika notifikasi diklik
        Intent intent = new Intent(context, NewBeranda.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Buat pesan dengan informasi user
        String message;
        if (username != null && !username.isEmpty()) {
            message = "Promo '" + promoTitle + "' berhasil dihapus oleh " + username;
        } else {
            message = "Promo '" + promoTitle + "' berhasil dihapus";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_delete)
                .setContentTitle("Promo Dihapus")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.RED)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setLights(Color.RED, 1000, 1000);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(generateNotificationId(), builder.build());
            Log.d("NotificationUtils", "✅ Delete notification shown for: " + promoTitle + " by " + username);
        } else {
            Log.w("NotificationUtils", "No notification permission for delete");
        }
    }

    // METHOD UNTUK NOTIFIKASI SISTEM UMUM
    private static void showSystemNotification(Context context, String title, String message, int icon) {
        try {
            Log.d("NotificationUtils", "Attempting to show notification: " + title + " - " + message);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                Log.e("NotificationUtils", "NotificationManager is null");
                return;
            }

            // Buat notification channel untuk Android 8.0+
            createNotificationChannel(context);

            // Intent untuk ketika notifikasi diklik
            Intent intent = new Intent(context, NewBeranda.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Buat notifikasi
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(icon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setColor(Color.BLUE)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setDefaults(Notification.DEFAULT_ALL) // Sound, vibration, lights
                    .setVibrate(new long[]{0, 500, 200, 500})
                    .setLights(Color.BLUE, 1000, 1000);

            // Tampilkan notifikasi dengan ID yang unik
            int notificationId = generateNotificationId();
            notificationManager.notify(notificationId, builder.build());

            Log.d("NotificationUtils", "✅ Notification shown successfully! ID: " + notificationId);
            Log.d("NotificationUtils", "Title: " + title + ", Message: " + message);

        } catch (Exception e) {
            Log.e("NotificationUtils", "❌ Error showing notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // METHOD UNTUK MEMBUAT NOTIFICATION CHANNEL
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                Log.e("NotificationUtils", "NotificationManager is null in createChannel");
                return;
            }

            // Cek apakah channel sudah ada
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (existingChannel != null) {
                Log.d("NotificationUtils", "Notification channel already exists");
                return;
            }

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);

            notificationManager.createNotificationChannel(channel);
            Log.d("NotificationUtils", "✅ Notification channel created: " + CHANNEL_ID);
        }
    }

    // METHOD UNTUK GENERATE UNIQUE NOTIFICATION ID
    private static int generateNotificationId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    // Method untuk test notification
    public static void testNotification(Context context) {
        showSystemNotification(context, "Test Notification",
                "Ini adalah test notifikasi dari aplikasi",
                android.R.drawable.ic_dialog_info);
    }

    // Method untuk clear notification
    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
            Log.d("NotificationUtils", "All notifications cleared");
        }
    }
}