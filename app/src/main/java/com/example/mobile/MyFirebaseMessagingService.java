package com.example.mobile;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.media.AudioAttributes;
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

    // ✅ PERBAIKAN: Definisikan semua konstanta dengan benar
    private static final String CHANNEL_ID = "promo_channel";
    private static final String CHANNEL_NAME = "Promo Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for promotions and updates";
    private static final String CHANNEL_ID_FOREGROUND = "fcm_foreground_service";
    private static final String FOREGROUND_CHANNEL_NAME = "FCM Background Service";
    private static final String FOREGROUND_CHANNEL_DESCRIPTION = "Service untuk menerima notifikasi promo";
    private static final int FOREGROUND_SERVICE_ID = 9999;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🚀 FCM Service Created - Starting foreground service");

        // ✅ JALANKAN SEBAGAI FOREGROUND SERVICE
        startForegroundService();
        createNotificationChannel();
        subscribeToPromoTopic();
    }

    // ✅ METHOD BARU: START FOREGROUND SERVICE UNTUK JAGA SERVICE TETAP HIDUP
    private void startForegroundService() {
        try {
            // Buat channel untuk foreground service
            createForegroundChannel();

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // ✅ PERBAIKAN: Gunakan channel yang benar untuk foreground service
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
                    .setContentTitle("Aplikasi Promo")
                    .setContentText("Menerima notifikasi promo...")
                    .setSmallIcon(getNotificationIcon())
                    .setColor(Color.parseColor("#FF2196F3"))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();

            // ✅ PERBAIKAN: Gunakan startForeground biasa tanpa foregroundServiceType
            startForeground(FOREGROUND_SERVICE_ID, notification);

            Log.d(TAG, "✅ Foreground service started");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting foreground service: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: CHANNEL UNTUK FOREGROUND SERVICE
    private void createForegroundChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_FOREGROUND,
                    FOREGROUND_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(FOREGROUND_CHANNEL_DESCRIPTION);
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Foreground service channel created");
            }
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "=== FCM MESSAGE RECEIVED ===");
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // ✅ ACQUIRE WAKE LOCK SEBELUM PROCESS NOTIFIKASI
        acquireWakeLock();

        try {
            saveNotificationReceipt();

            Map<String, String> data = remoteMessage.getData();

            if (data != null && !data.isEmpty()) {
                Log.d(TAG, "📦 DATA PAYLOAD DETECTED - Size: " + data.size());
                handleDataMessage(data);
            }
            else if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "📢 NOTIFICATION PAYLOAD DETECTED");
                String title = remoteMessage.getNotification().getTitle();
                String body = remoteMessage.getNotification().getBody();

                if (title == null) title = "Promo Baru! 🎉";
                if (body == null) body = "Ada promo baru yang ditambahkan";

                sendNotificationForKilledApp(title, body, data, "notification_payload");
            }

        } finally {
            // ✅ RELEASE WAKE LOCK SETELAH SELESAI
            releaseWakeLock();
        }
    }

    // ✅ METHOD BARU: NOTIFIKASI UNTUK APP YANG DI-KILL
    private void sendNotificationForKilledApp(String title, String messageBody, Map<String, String> data, String source) {
        try {
            Log.d(TAG, "🔔 Creating notification for killed app state");

            // ✅ INTENT YANG LEBIH AGGRESIVE
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("from_notification", true);
            intent.putExtra("source", "killed_app");
            intent.putExtra("timestamp", System.currentTimeMillis());
            intent.setAction("OPEN_APP_" + System.currentTimeMillis());

            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }

            int requestCode = new Random().nextInt(10000);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // ✅ FULL SCREEN INTENT UNTUK IMPORTANT NOTIFICATION
            PendingIntent fullScreenIntent = PendingIntent.getActivity(this, requestCode + 1, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // ✅ NOTIFICATION BUILDER YANG LEBIH AGGRESIVE
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setFullScreenIntent(fullScreenIntent, true) // ✅ FORCE SHOW
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setCategory(NotificationCompat.CATEGORY_CALL) // ✅ GUNAKAN CATEGORY_CALL untuk high priority
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true)
                    .setColor(Color.parseColor("#FF2196F3"))
                    .setLights(Color.BLUE, 1000, 1000)
                    .setTimeoutAfter(30000); // 30 seconds timeout

            // ✅ VIBRATE YANG LEBIH AGGRESIVE
            long[] vibratePattern = {0, 1000, 500, 1000};
            notificationBuilder.setVibrate(vibratePattern);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                if (notificationManager.areNotificationsEnabled()) {
                    int notificationId = (int) System.currentTimeMillis();
                    notificationManager.notify(notificationId, notificationBuilder.build());

                    Log.d(TAG, "✅ Notification displayed for killed app - ID: " + notificationId);
                    saveNotificationLog(title, messageBody, notificationId, "killed_app");
                    sendNotificationReceivedBroadcast(title, messageBody, "killed_app");

                    // ✅ TRY TO WAKE UP SCREEN
                    wakeUpScreen();
                } else {
                    Log.e(TAG, "❌ Notifications are disabled");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Error showing notification for killed app: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: WAKE UP SCREEN
    private void wakeUpScreen() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                PowerManager.WakeLock screenWakeLock = powerManager.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "MyApp:ScreenWakeLock"
                );
                screenWakeLock.acquire(5000); // 5 seconds
                screenWakeLock.release();
                Log.d(TAG, "📱 Screen wake up attempted");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error waking up screen: " + e.getMessage());
        }
    }

    // ✅ IMPROVED WAKE LOCK
    private void acquireWakeLock() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "MyApp:FCMWakeLock"
            );
            wakeLock.acquire(30000); // 30 seconds
            Log.d(TAG, "🔋 Wake lock acquired for 30s");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error acquiring wake lock: " + e.getMessage());
        }
    }

    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "🔋 Wake lock released");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error releasing wake lock: " + e.getMessage());
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        try {
            String title = data.get("title");
            String body = data.get("body");
            String type = data.get("type");

            if (title == null || title.isEmpty()) title = "Promo Baru! 🎉";
            if (body == null || body.isEmpty()) body = "Ada promo baru yang ditambahkan";

            if ("new_promo".equals(type)) {
                sendNotificationForKilledApp(title, body, data, "data_payload");
            } else {
                sendNotificationForKilledApp(title, body, data, "other_type");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Failed to handle data message: " + e.getMessage());
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
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );

                channel.setDescription(CHANNEL_DESCRIPTION);
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                channel.setShowBadge(true);
                channel.setBypassDnd(true); // ✅ BYPASS DND UNTUK VIVO/XIAOMI

                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                channel.setSound(soundUri, new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build());

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ High priority channel created with bypass DND");

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

    private void subscribeToPromoTopic() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("all_devices")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "✅ Successfully subscribed to 'all_devices' topic");
                            SharedPreferences prefs = getSharedPreferences("FCM_Prefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("topic_subscribed", true).apply();
                        } else {
                            Log.e(TAG, "❌ Failed to subscribe to topic: " + task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "💥 Error subscribing to topic: " + e.getMessage());
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "🔄 Refreshed FCM token: " + token);
        SharedPreferences prefs = getSharedPreferences("FCM_Prefs", MODE_PRIVATE);
        prefs.edit().putString("fcm_token", token).apply();
        subscribeToPromoTopic();
    }

    // ... (method helper lainnya)
    private void sendNotificationReceivedBroadcast(String title, String body, String source) {
        try {
            Intent broadcastIntent = new Intent("FCM_NOTIFICATION_RECEIVED");
            broadcastIntent.putExtra("title", title);
            broadcastIntent.putExtra("body", body);
            broadcastIntent.putExtra("source", source);
            broadcastIntent.putExtra("timestamp", System.currentTimeMillis());
            sendBroadcast(broadcastIntent);
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Error sending broadcast: " + e.getMessage());
        }
    }

    private void saveNotificationLog(String title, String body, int notificationId, String source) {
        try {
            SharedPreferences prefs = getSharedPreferences("FCM_Prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_notification_title", title);
            editor.putString("last_notification_body", body);
            editor.putInt("last_notification_id", notificationId);
            editor.putString("last_notification_source", source);
            editor.putLong("last_notification_time", System.currentTimeMillis());
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "💥 Error saving notification log: " + e.getMessage());
        }
    }

    private void saveNotificationReceipt() {
        try {
            SharedPreferences prefs = getSharedPreferences("FCM_Prefs", MODE_PRIVATE);
            prefs.edit().putLong("last_notification_received", System.currentTimeMillis()).apply();
            prefs.edit().putInt("notification_count", prefs.getInt("notification_count", 0) + 1).apply();
        } catch (Exception e) {
            Log.e(TAG, "💥 Error saving notification receipt: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        Log.d(TAG, "🛑 FCM Service Destroyed");
    }
}