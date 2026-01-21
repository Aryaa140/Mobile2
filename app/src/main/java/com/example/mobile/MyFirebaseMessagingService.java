package com.example.mobile;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.*;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "promo_channel";

    // ✅ PERBAIKAN: Tambahkan logging detail di onMessageReceived
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "=== FCM MESSAGE RECEIVED ===");
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "Message Type: " + remoteMessage.getMessageType());
        Log.d(TAG, "Collapse Key: " + remoteMessage.getCollapseKey());
        Log.d(TAG, "Sent Time: " + remoteMessage.getSentTime());

        // ✅ LOG DATA DAN NOTIFICATION
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Log.d(TAG, "Notification Title: " + notification.getTitle());
            Log.d(TAG, "Notification Body: " + notification.getBody());
            Log.d(TAG, "Notification Icon: " + notification.getIcon());
            Log.d(TAG, "Notification Sound: " + notification.getSound());
            Log.d(TAG, "Notification Tag: " + notification.getTag());
        }

        handleIncomingMessage(remoteMessage);
    }


    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        try {
            String title = notification.getTitle();
            String body = notification.getBody();

            if (title == null || title.isEmpty()) title = "Notifikasi Baru";
            if (body == null || body.isEmpty()) body = "Ada aktivitas terbaru";

            showNotification(title, body, null);

        } catch (Exception e) {
            Log.e(TAG, "💥 Failed to handle notification message: " + e.getMessage());
        }
    }

    private void showNotification(String title, String messageBody, Map<String, String> data) {
        try {
            Log.d(TAG, "🔔 Creating notification: " + title);

            // Intent untuk buka MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("from_notification", true);
            intent.putExtra("timestamp", System.currentTimeMillis());

            // Tambahkan data tambahan jika ada
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }

            int requestCode = new Random().nextInt(10000);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Buat notifikasi
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setColor(Color.parseColor("#FF2196F3"));

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null && notificationManager.areNotificationsEnabled()) {
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, notificationBuilder.build());

                Log.d(TAG, "✅ Notification displayed - ID: " + notificationId + " - Title: " + title);
                saveNotificationLog(title, messageBody, notificationId, "fcm_message");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Error showing notification: " + e.getMessage());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager == null) return;

                NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
                if (existingChannel != null) {
                    Log.d(TAG, "✅ Notification channel already exists: " + CHANNEL_ID);
                    return;
                }

                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Promo Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.setDescription("Notifications for promotions and updates");
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                channel.setShowBadge(true);

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel created");

            } catch (Exception e) {
                Log.e(TAG, "💥 Error creating notification channel: " + e.getMessage());
            }
        }
    }

    private int getNotificationIcon() {
        int icon = getResources().getIdentifier("ic_notification", "drawable", getPackageName());
        if (icon == 0) {
            icon = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
            if (icon == 0) {
                icon = android.R.drawable.ic_dialog_info;
            }
        }
        return icon;
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "🔄 Refreshed FCM token: " + token);
        // Subscribe ke topic
        subscribeToPromoTopic();
    }

    private void subscribeToPromoTopic() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("all_devices")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "✅ Successfully subscribed to 'all_devices' topic");
                        } else {
                            Log.e(TAG, "❌ Failed to subscribe to topic: " + task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "💥 Error subscribing to topic: " + e.getMessage());
        }
    }

    private void saveNotificationLog(String title, String body, int notificationId, String source) {
        try {
            Log.d(TAG, "📝 Notification Log - Title: " + title + ", Body: " + body + ", Source: " + source);

            // Simpan ke SharedPreferences untuk tracking
            SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_notification_title", title);
            editor.putString("last_notification_body", body);
            editor.putString("last_notification_source", source);
            editor.putLong("last_notification_time", System.currentTimeMillis());
            editor.apply();

        } catch (Exception e) {
            Log.e(TAG, "💥 Error saving notification log: " + e.getMessage());
        }
    }

    // ✅ PERBAIKAN: Tambahkan duplicate prevention yang lebih kuat
    private boolean isDuplicateNotification(Map<String, String> data) {
        try {
            String messageId = data.get("message_id");
            String timestampStr = data.get("timestamp");
            long timestamp = 0;

            try {
                timestamp = timestampStr != null ? Long.parseLong(timestampStr) : 0;
            } catch (NumberFormatException e) {
                timestamp = 0;
            }

            SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
            String lastMessageId = prefs.getString("last_proyek_message_id", "");
            long lastTimestamp = prefs.getLong("last_proyek_timestamp", 0);

            // ✅ CEK BERDASARKAN PROYEK NAME + TIMESTAMP
            String proyekName = data.get("proyek_name");
            String currentKey = proyekName + "_" + timestamp;
            String lastKey = prefs.getString("last_proyek_key", "");

            Log.d(TAG, "🔍 Duplicate Check:");
            Log.d(TAG, "  Current - MessageID: " + messageId + ", Timestamp: " + timestamp + ", Key: " + currentKey);
            Log.d(TAG, "  Last - MessageID: " + lastMessageId + ", Timestamp: " + lastTimestamp + ", Key: " + lastKey);

            // Cek duplicate berdasarkan:
            // 1. Message ID sama
            // 2. Proyek key sama (nama + timestamp)
            // 3. Timestamp sangat berdekatan (< 3 detik)
            boolean isDuplicate =
                    (!messageId.isEmpty() && messageId.equals(lastMessageId)) ||
                            (!currentKey.isEmpty() && currentKey.equals(lastKey)) ||
                            (timestamp > 0 && lastTimestamp > 0 && (timestamp - lastTimestamp) < 3000);

            if (isDuplicate) {
                Log.d(TAG, "🔕 DUPLICATE DETECTED - Skipping notification");
                return true;
            }

            // Simpan data notifikasi terbaru
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_proyek_message_id", messageId);
            editor.putLong("last_proyek_timestamp", timestamp);
            editor.putString("last_proyek_key", currentKey);
            editor.apply();

            Log.d(TAG, "✅ NOT DUPLICATE - Proceeding with notification");
            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error in duplicate check: " + e.getMessage());
            return false;
        }
    }

    // ✅ PERBAIKAN: handleProyekNotification dengan duplicate check
    private void handleProyekNotification(Map<String, String> data) {
        try {
            Log.d(TAG, "📦 PROYEK NOTIFICATION RECEIVED - START PROCESSING");

            // ✅ CEK DUPLIKAT SEBELUM APAPUN
            if (isDuplicateNotification(data)) {
                Log.d(TAG, "🔕 SKIPPING - Duplicate notification detected");
                return;
            }

            // ✅ CEK APAKAH PERLU MENAMPILKAN NOTIFIKASI
            boolean shouldShow = shouldShowNotification(data);
            Log.d(TAG, "🔍 Should show notification: " + shouldShow);

            if (!shouldShow) {
                Log.d(TAG, "🔕 SKIPPING - User filter restriction");
                return;
            }

            String title = data.get("title");
            String body = data.get("body");
            String proyekName = data.get("proyek_name");
            String lokasiProyek = data.get("lokasi_proyek");
            String addedBy = data.get("added_by");

            // ✅ GUNAKAN DATA YANG SUDAH ADA, JANGAN GENERATE ULANG
            if (title == null || title.isEmpty()) {
                title = "Proyek Ditambahkan 🏗️";
            }
            if (body == null || body.isEmpty()) {
                body = "Ada proyek baru yang ditambahkan";
            }

            Log.d(TAG, "🎯 Final notification - Title: " + title + ", Body: " + body);

            // ✅ Tampilkan notifikasi khusus proyek
            showProyekNotification(title, body, data);
            Log.d(TAG, "✅ Proyek notification DISPLAYED");

        } catch (Exception e) {
            Log.e(TAG, "💥 Error handling proyek notification: " + e.getMessage());
        }
    }

    // ✅ PERBAIKAN: shouldShowNotification dengan logging detail
    private boolean shouldShowNotification(Map<String, String> data) {
        try {
            // Dapatkan informasi user saat ini
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String currentUsername = prefs.getString("username", "");
            String currentNamaUser = prefs.getString("nama_user", "");

            // Dapatkan informasi penginput dari notifikasi
            String addedBy = data.get("added_by");
            String notificationUsername = data.get("username");
            String type = data.get("type");

            Log.d(TAG, "🔍 CHECKING NOTIFICATION PERMISSION:");
            Log.d(TAG, "  Current User - Username: '" + currentUsername + "', Nama: '" + currentNamaUser + "'");
            Log.d(TAG, "  Notification From - AddedBy: '" + addedBy + "', Username: '" + notificationUsername + "'");
            Log.d(TAG, "  Notification Type: '" + type + "'");

            // ✅ PERBAIKAN: Filter untuk notifikasi proyek
            if ("new_proyek".equals(type)) {
                boolean isCurrentUser =
                        (!currentUsername.isEmpty() && currentUsername.equals(notificationUsername)) ||
                                (!currentNamaUser.isEmpty() && currentNamaUser.equals(addedBy));

                Log.d(TAG, "  Is Current User: " + isCurrentUser);

                if (isCurrentUser) {
                    Log.d(TAG, "🔕 SKIPPING - Notification from current user");
                    return false;
                }
            }

            Log.d(TAG, "✅ SHOWING - Notification from other user or different conditions");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "💥 Error checking notification permission: " + e.getMessage());
            Log.e(TAG, "💥 Defaulting to show notification");
            return true;
        }
    }

    private void handleIncomingMessage(RemoteMessage remoteMessage) {
        try {
            Map<String, String> data = remoteMessage.getData();
            RemoteMessage.Notification notification = remoteMessage.getNotification();

            Log.d(TAG, "Data payload: " + data);
            Log.d(TAG, "Notification payload: " + notification);

            // Prioritaskan data payload daripada notification payload
            if (data != null && !data.isEmpty()) {
                Log.d(TAG, "📦 DATA PAYLOAD DETECTED - Size: " + data.size());
                handleDataMessage(data);
            } else if (notification != null) {
                Log.d(TAG, "📢 NOTIFICATION PAYLOAD DETECTED");
                handleNotificationMessage(notification);
            } else {
                Log.w(TAG, "⚠️ Unknown message format");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Error handling incoming message: " + e.getMessage());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        try {
            String type = data.get("type");
            Log.d(TAG, "Handling data message - Type: " + type);

            // ✅ PERBAIKAN: Handle proyek notifications - CEK TYPE YANG BENAR
            if ("new_proyek".equals(type)) {
                handleProyekNotification(data);
                return;
            }

            // Handle promo notifications
            else if ("new_promo".equals(type)) {
                String title = data.get("title");
                String body = data.get("body");

                if (title == null || title.isEmpty()) {
                    title = "Promo Baru! 🎉";
                }
                if (body == null || body.isEmpty()) {
                    body = "Ada promo baru yang ditambahkan";
                }
                showNotification(title, body, data);
            }
            // Default case - fallback
            else {
                String title = data.get("title");
                String body = data.get("body");

                if (title == null || title.isEmpty()) {
                    title = "Notifikasi Baru";
                }
                if (body == null || body.isEmpty()) {
                    body = "Ada aktivitas terbaru";
                }
                showNotification(title, body, data);
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Failed to handle data message: " + e.getMessage());
        }
    }



    // ✅ METHOD BARU: Cek duplicate notification
    private boolean isDuplicateNotification(String messageId, long timestamp) {
        try {
            SharedPreferences prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
            String lastMessageId = prefs.getString("last_message_id", "");
            long lastTimestamp = prefs.getLong("last_notification_time", 0);

            // Cek jika messageId sama atau timestamp sangat berdekatan (< 2 detik)
            boolean isDuplicate =
                    (!messageId.isEmpty() && messageId.equals(lastMessageId)) ||
                            (timestamp > 0 && lastTimestamp > 0 && (timestamp - lastTimestamp) < 2000);

            if (isDuplicate) {
                Log.d(TAG, "🔕 Duplicate notification detected - MessageID: " + messageId);
                return true;
            }

            // Simpan notifikasi terbaru
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_message_id", messageId);
            editor.putLong("last_notification_time", timestamp);
            editor.apply();

            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error checking duplicate notification: " + e.getMessage());
            return false;
        }
    }

    // ✅ PERBAIKAN METHOD: showProyekNotification
    private void showProyekNotification(String title, String body, Map<String, String> data) {
        try {
            Log.d(TAG, "🔔 Creating PROYEK notification: " + title);

            // Intent untuk buka activity yang menampilkan proyek
            Intent intent = new Intent(this, LihatDataActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("from_notification", true);
            intent.putExtra("notification_type", "new_proyek");
            intent.putExtra("fragment_to_open", "proyek");
            intent.putExtra("timestamp", System.currentTimeMillis());

            // Tambahkan data proyek ke intent
            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }

            int requestCode = new Random().nextInt(10000);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Buat notifikasi khusus proyek
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setColor(Color.parseColor("#FF4CAF50")) // Warna hijau untuk proyek
                    .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null && notificationManager.areNotificationsEnabled()) {
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, notificationBuilder.build());

                Log.d(TAG, "✅ PROYEK Notification displayed - ID: " + notificationId + " - Title: " + title);
                saveNotificationLog(title, body, notificationId, "fcm_proyek");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Error showing proyek notification: " + e.getMessage());
        }
    }
}