package com.example.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Date;
import java.util.List;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "🔄 Checking for new promos...");

        try {
            checkNewPromos();
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error checking promos: " + e.getMessage());
            return Result.retry();
        }
    }

    private void checkNewPromos() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        String lastCheck = prefs.getString("last_promo_check", "");
        String username = prefs.getString("username", "");

        if (username.isEmpty()) {
            Log.d(TAG, "❌ Username not found, skipping check");
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        // ✅ PAKAI getData() BUKAN getPromos()
                        processNewPromos(promoResponse.getData(), lastCheck, username);

                        // Update last check time
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("last_promo_check", new Date().toString());
                        editor.apply();

                        Log.d(TAG, "✅ Promo check completed");
                    } else {
                        Log.d(TAG, "❌ Server response not successful: " + promoResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ API call failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error checking promos: " + t.getMessage());
            }
        });
    }

    private void processNewPromos(List<Promo> promos, String lastCheck, String currentUser) {
        if (promos == null) {
            Log.d(TAG, "❌ No promos data");
            return;
        }

        int newPromoCount = 0;

        for (Promo promo : promos) {
            // Skip jika promo dibuat oleh user sendiri
            if (promo.getNamaPenginput() != null && promo.getNamaPenginput().equals(currentUser)) {
                continue;
            }

            // Tampilkan notifikasi untuk promo baru
            showPromoNotification(promo);
            newPromoCount++;
        }

        if (newPromoCount > 0) {
            Log.d(TAG, "🎉 Found " + newPromoCount + " new promos");
        } else {
            Log.d(TAG, "ℹ️ No new promos found");
        }
    }

    private void showPromoNotification(Promo promo) {
        String title = "Promo Baru! 🎉";
        String body = promo.getNamaPenginput() + " menambahkan: " + promo.getNamaPromo();

        NotificationHelper.showPromoNotification(
                getApplicationContext(),
                title,
                body,
                null
        );

        Log.d(TAG, "📢 Notification shown: " + body);
    }
}