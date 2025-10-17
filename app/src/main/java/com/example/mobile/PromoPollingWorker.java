package com.example.mobile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Response;

public class PromoPollingWorker extends Worker {
    private static final String TAG = "PromoPollingWorker";
    private UserSessionManager session;

    public PromoPollingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        session = new UserSessionManager(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "🔄 Polling worker started");

        if (!session.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping polling");
            return Result.success();
        }

        checkForNewPromos();
        return Result.success();
    }

    private void checkForNewPromos() {
        try {
            int userId = session.getUserId();
            String username = session.getUsername();
            String deviceId = session.getDeviceId();
            String lastCheck = session.getLastCheckTime();

            if (lastCheck.isEmpty()) {
                lastCheck = getOneHourAgo();
                Log.d(TAG, "First time polling for user " + username + ", using: " + lastCheck);
            }

            Log.d(TAG, "Polling for user ID: " + userId + ", device: " + deviceId);

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<PollingResponse> call = apiService.checkNewPromos(userId, username, deviceId, lastCheck);

            Response<PollingResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                PollingResponse pollingResponse = response.body();

                if (pollingResponse.isSuccess()) {
                    List<Promo> newPromos = pollingResponse.getNewPromos();
                    int newCount = pollingResponse.getCount();

                    Log.d(TAG, "Polling result: " + newCount + " new promos for user " + username);

                    if (newCount > 0) {
                        for (Promo promo : newPromos) {
                            showPromoNotification(promo);
                        }
                        showSummaryNotification(newCount);
                    }

                    String currentTime = pollingResponse.getCurrentTime();
                    session.saveLastCheckTime(currentTime);

                    Log.d(TAG, "✅ Polling completed for user " + username + ". Last check: " + currentTime);

                } else {
                    Log.e(TAG, "Polling API error: " + pollingResponse.getMessage());
                }
            } else {
                String errorMsg = response.message() != null ? response.message() : "Unknown error";
                Log.e(TAG, "Network error: " + errorMsg);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in polling: " + e.getMessage());
        }
    }

    private String getOneHourAgo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        return sdf.format(cal.getTime());
    }

    private void showPromoNotification(Promo promo) {
        try {
            String channelId = "promo_polling_channel";
            String title = "🎉 " + promo.getNamaPromo();
            String content = "Oleh: " + promo.getNamaPenginput();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("open_promos", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    promo.getIdPromo(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            createNotificationChannel(notificationManager, channelId);
            notificationManager.notify(promo.getIdPromo(), builder.build());

            Log.d(TAG, "📢 Notification shown: " + promo.getNamaPromo());

        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
        }
    }

    private void showSummaryNotification(int count) {
        if (count <= 1) return;

        String channelId = "promo_polling_channel";
        String title = "🎊 " + count + " Promo Baru!";
        String content = "Ada " + count + " promo baru tersedia";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("open_promos", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel(notificationManager, channelId);
        notificationManager.notify(9999, builder.build());

        Log.d(TAG, "📢 Summary notification shown for " + count + " promos");
    }

    private void createNotificationChannel(NotificationManager notificationManager, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Promo Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new promotions");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
}