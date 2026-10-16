package com.example.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogger {

    // ===== KONSTANTA AKSI =====
    public static final String AKSI_CREATE  = "CREATE";
    public static final String AKSI_UPDATE  = "UPDATE";
    public static final String AKSI_DELETE  = "DELETE";
    public static final String AKSI_VIEW    = "VIEW";
    public static final String AKSI_BOOKING = "BOOKING";
    public static final String AKSI_LOGIN   = "LOGIN";
    public static final String AKSI_LOGOUT  = "LOGOUT";

    // ===== KONSTANTA FITUR =====
    public static final String FITUR_PROSPEK   = "PROSPEK";
    public static final String FITUR_USERP     = "USERP";
    public static final String FITUR_PROYEK    = "PROYEK";
    public static final String FITUR_HUNIAN    = "HUNIAN";
    public static final String FITUR_KAVLING   = "KAVLING";
    public static final String FITUR_PROMO     = "PROMO";
    public static final String FITUR_FASILITAS = "FASILITAS";
    public static final String FITUR_USER      = "USER";
    public static final String FITUR_BOOKING   = "BOOKING";
    public static final String FITUR_REALISASI = "REALISASI";
    public static final String FITUR_AUTH      = "AUTH";
    public static final String FITUR_OTHER     = "OTHER";

    private static final String PREFS = "LoginPrefs";
    private static final String TAG   = "ActivityLogger";

    // ===== FORMATTER WAKTU =====
    private static final String[] HARI = {
            "Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"
    };

    public static void log(Context context,
                           String aksi,
                           String fitur,
                           String deskripsi,
                           String activityName,
                           Integer idTarget,
                           String namaTarget) {

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String nip      = prefs.getString("nip", "");
        String divisi   = prefs.getString("division", "");
        String level    = prefs.getString("level", "");

        if (username.isEmpty()) {
            Log.w(TAG, "Skip log: belum login");
            return;
        }

        // ===== DEVICE TIME =====
        long deviceMillis = System.currentTimeMillis();

        // Format: "2026-10-15 20:26:52.123"
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        String deviceTimeString = fmt.format(new Date(deviceMillis));

        // Timezone
        TimeZone tz = TimeZone.getDefault();
        String timezone = tz.getID();   // "Asia/Jakarta"
        String tzOffset = buildOffset(tz);  // "+07:00"

        // Hari
        Calendar cal = Calendar.getInstance();
        String hari = HARI[cal.get(Calendar.DAY_OF_WEEK) - 1];  // "Kamis"

        // Device info
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL
                + " (Android " + Build.VERSION.RELEASE + ")";

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Call<LogResponse> call = api.addLog(
                "add_log",
                username, nip, divisi, level,
                aksi, fitur,
                deskripsi != null ? deskripsi : "",
                activityName != null ? activityName : "",
                deviceInfo,
                idTarget,
                namaTarget != null ? namaTarget : "",
                deviceMillis,
                deviceTimeString,
                timezone,
                tzOffset,
                hari
        );

        call.enqueue(new Callback<LogResponse>() {
            @Override
            public void onResponse(Call<LogResponse> call, Response<LogResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✔ [" + deviceTimeString + "] "
                            + aksi + " | " + fitur + " | " + deskripsi);
                } else {
                    Log.w(TAG, "✘ Log gagal, code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LogResponse> call, Throwable t) {
                Log.e(TAG, "✘ Log error: " + t.getMessage());
            }
        });
    }

    // Helper: hitung offset timezone format "+07:00"
    private static String buildOffset(TimeZone tz) {
        int offsetMs  = tz.getOffset(System.currentTimeMillis());
        int totalMin  = offsetMs / 60000;
        int hours     = Math.abs(totalMin) / 60;
        int minutes   = Math.abs(totalMin) % 60;
        String sign   = totalMin >= 0 ? "+" : "-";
        return String.format(Locale.getDefault(), "%s%02d:%02d", sign, hours, minutes);
    }

    // ===== OVERLOAD =====
    public static void log(Context context, String aksi, String fitur,
                           String deskripsi, String activityName) {
        log(context, aksi, fitur, deskripsi, activityName, null, null);
    }
    public static void log(Context context, String aksi, String fitur, String deskripsi) {
        log(context, aksi, fitur, deskripsi, null, null, null);
    }
    public static void log(Context context, String aksi, String fitur,
                           String deskripsi, Integer idTarget, String namaTarget) {
        log(context, aksi, fitur, deskripsi, null, idTarget, namaTarget);
    }
}