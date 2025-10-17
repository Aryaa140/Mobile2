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
import java.util.Random;

public class UserSessionManager {
    private static final String TAG = "UserSessionManager";
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NIP = "nip";
    private static final String KEY_DIVISI = "divisi";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_CHECK = "last_promo_check";
    private static final String KEY_DEVICE_ID = "device_id";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(int userId, String username, String nip,
                                   String divisi, String level) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // ✅ Jika user_id = 0, generate ID sementara dari username
        if (userId == 0) {
            userId = generateUserIdFromUsername(username);
        }

        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NIP, nip);
        editor.putString(KEY_DIVISI, divisi);
        editor.putString(KEY_LEVEL, level);
        editor.putString(KEY_DEVICE_ID, generateDeviceId());
        editor.apply();

        Log.d(TAG, "Login session created for user: " + username + " (ID: " + userId + ")");
        startPollingService();
    }
    private int generateUserIdFromUsername(String username) {
        // Hash sederhana dari username untuk dijadikan ID sementara
        return Math.abs(username.hashCode());
    }
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, 0);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getNip() {
        return pref.getString(KEY_NIP, "");
    }

    public String getDivisi() {
        return pref.getString(KEY_DIVISI, "");
    }

    public String getLevel() {
        return pref.getString(KEY_LEVEL, "");
    }

    public String getDeviceId() {
        String deviceId = pref.getString(KEY_DEVICE_ID, "");
        if (deviceId.isEmpty()) {
            deviceId = generateDeviceId();
            editor.putString(KEY_DEVICE_ID, deviceId);
            editor.apply();
        }
        return deviceId;
    }

    public void saveLastCheckTime(String timestamp) {
        editor.putString(KEY_LAST_CHECK, timestamp);
        editor.apply();
    }

    public String getLastCheckTime() {
        return pref.getString(KEY_LAST_CHECK, "");
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
        stopPollingService();
        Log.d(TAG, "User logged out and polling stopped");
    }

    private String generateDeviceId() {
        return "device_" + System.currentTimeMillis() + "_" + new Random().nextInt(10000);
    }

    private void startPollingService() {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest pollingRequest =
                    new PeriodicWorkRequest.Builder(
                            PromoPollingWorker.class,
                            15, // Setiap 15 menit
                            TimeUnit.MINUTES
                    )
                            .setConstraints(constraints)
                            .setInitialDelay(1, TimeUnit.MINUTES)
                            .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "promo_polling_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    pollingRequest
            );

            Log.d(TAG, "🔔 Polling service started for user: " + getUsername());
        } catch (Exception e) {
            Log.e(TAG, "Error starting polling service: " + e.getMessage());
        }
    }

    private void stopPollingService() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("promo_polling_work");
            Log.d(TAG, "🔕 Polling service stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping polling service: " + e.getMessage());
        }
    }
}