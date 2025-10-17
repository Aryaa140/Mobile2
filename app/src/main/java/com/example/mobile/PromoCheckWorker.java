package com.example.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromoCheckWorker extends Worker {
    private static final String TAG = "PromoCheckWorker";
    private static final String PREFS_NAME = "PromoPrefs";

    public PromoCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "PromoCheckWorker started at: " + new Date());

        try {
            // Gunakan synchronous call untuk Worker
            checkForNewPromosSync();
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error in PromoCheckWorker: " + e.getMessage());
            return Result.failure();
        }
    }

    private void checkForNewPromosSync() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastCheckTime = prefs.getString("last_promo_check", "");

        Log.d(TAG, "Last check time: " + lastCheckTime);

        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<PromoResponse> call = apiService.getSemuaPromo();

            // Synchronous execution for Worker
            Response<PromoResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                PromoResponse promoResponse = response.body();
                if (promoResponse.isSuccess()) {
                    processNewPromos(promoResponse.getData(), lastCheckTime);
                } else {
                    Log.d(TAG, "Server response not successful: " + promoResponse.getMessage());
                }
            } else {
                Log.e(TAG, "Network error: " + response.code());
            }

        } catch (Exception e) {
            Log.e(TAG, "Network call failed: " + e.getMessage());
        } finally {
            // Update last check time
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            prefs.edit().putString("last_promo_check", currentTime).apply();
            Log.d(TAG, "Updated last check time: " + currentTime);
        }
    }

    private void processNewPromos(java.util.List<Promo> promos, String lastCheckTime) {
        if (promos == null || promos.isEmpty()) {
            Log.d(TAG, "No promos found in response");
            return;
        }

        Log.d(TAG, "Processing " + promos.size() + " promos");

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastCheck = lastCheckTime.isEmpty() ? new Date(0) : dateFormat.parse(lastCheckTime);

            int newPromoCount = 0;
            for (Promo promo : promos) {
                if (promo.getTanggalInput() == null) {
                    continue;
                }

                try {
                    Date promoDate = dateFormat.parse(promo.getTanggalInput());
                    if (promoDate.after(lastCheck)) {
                        showNewPromoNotification(promo);
                        newPromoCount++;
                        Log.d(TAG, "New promo found: " + promo.getNamaPromo());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date for promo: " + promo.getNamaPromo());
                }
            }

            Log.d(TAG, "Found " + newPromoCount + " new promos since last check");

        } catch (Exception e) {
            Log.e(TAG, "Error processing promos: " + e.getMessage());
        }
    }

    private void showNewPromoNotification(Promo promo) {
        try {
            String title = "Promo Baru! 🎉";
            String body = (promo.getNamaPenginput() != null ? promo.getNamaPenginput() : "Someone") +
                    " menambahkan: " + promo.getNamaPromo();

            // Show notification using NotificationHelper
            NotificationHelper.showPromoNotification(
                    getApplicationContext(),
                    title,
                    body,
                    null
            );

            Log.d(TAG, "Notification created for: " + promo.getNamaPromo());

        } catch (Exception e) {
            Log.e(TAG, "Error creating notification: " + e.getMessage());
        }
    }
}