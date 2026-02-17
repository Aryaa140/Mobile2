package com.example.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class  BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🚀 Boot completed - Restarting FCM service");

        // Restart FCM service ketika device boot
        try {
            Intent serviceIntent = new Intent(context, MyFirebaseMessagingService.class);
            context.startService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error restarting FCM service: " + e.getMessage());
        }
    }
}