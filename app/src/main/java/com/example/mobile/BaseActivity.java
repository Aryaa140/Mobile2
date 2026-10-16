package com.example.mobile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Log otomatis saat activity dibuka
        String activityName = this.getClass().getSimpleName();
        autoLogView(activityName);
    }

    private void autoLogView(String activityName) {
        try {
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String username = prefs.getString("username", "");
            if (username.isEmpty()) {
                Log.d(TAG, "Skip log untuk " + activityName + ": belum login");
                return;
            }

            // Tentukan fitur dari nama activity
            String fitur = mapActivityToFitur(activityName);

            // Skip activity tertentu yang tidak perlu di-log (login, splash, dll.)
            if (fitur == null) {
                Log.d(TAG, "Skip log untuk " + activityName);
                return;
            }

            ActivityLogger.log(
                    this,
                    ActivityLogger.AKSI_VIEW,
                    fitur,
                    "Membuka " + activityName,
                    activityName,
                    null, null
            );

        } catch (Exception e) {
            Log.e(TAG, "autoLogView error: " + e.getMessage());
        }
    }

    /**
     * Mapping nama Activity → nama Fitur.
     * Tambahkan/ubah sesuai kebutuhan project.
     * Return null = activity tidak di-log (mis. Login, Splash).
     */
    private String mapActivityToFitur(String activityName) {
        switch (activityName) {
            // ===== PROSPEK =====
            case "TambahProspekActivity":
            case "EditDataProspekActivity":
            case "LihatDataProspekActivity":
            case "LihatDataHistoriUserProspek":
                return ActivityLogger.FITUR_PROSPEK;

            // ===== USER PROSPEK (USERP) =====
            case "TambahUserpActivity":
            case "EditDataUserpActivity":
            case "LihatDataUserpActivity":
                return ActivityLogger.FITUR_USERP;

            // ===== PROYEK =====
            case "InputDataProyekActivity":
            case "EditDataProyekActivity":
            case "LihatDataProyekActivity":
            case "ProyekActivity":
            case "DetailProyekActivity":
            case "UnitProyekActivity":
            case "DetailUnitProyekActivity":
                return ActivityLogger.FITUR_PROYEK;

            // ===== HUNIAN =====
            case "InputHunianActivity":
            case "EditDataHunianActivity":
            case "LihatDataHunianActivity":
            case "EditDataUnitHunianActivity":
                return ActivityLogger.FITUR_HUNIAN;

            // ===== KAVLING =====
            case "InputKavlingActivity":
            case "EditDataKavlingActivity":
            case "LihatDataKavlingActivity":
                return ActivityLogger.FITUR_KAVLING;

            // ===== PROMO =====
            case "InputPromoActivity":
            case "EditDataPromooActivity":
            case "TampilPromoActivity":
            case "NewsActivity":
            case "DeletedNewsActivity":
                return ActivityLogger.FITUR_PROMO;

            // ===== FASILITAS =====
            case "FasilitasActivity":
            case "TambahFasilitasHunianActivity":
            case "TambahFasilitasProyekActivity":
            case "EditFasilitasHunianActivity":
                return ActivityLogger.FITUR_FASILITAS;

            // ===== USER MANAGEMENT =====
            case "InputNipActivity":
            case "StatusAkunActivity":
            case "ProfileActivity":
            case "EditProfileActivity":
            case "GantiPasswordActivity":
                return ActivityLogger.FITUR_USER;

            // ===== BOOKING (kalau sudah ada) =====
            // case "BookingActivity": return ActivityLogger.FITUR_BOOKING;

            // Activity yang TIDAK perlu di-log
            case "MainActivity":
            case "NewBeranda":
            case "SignUpActivity":
            case "LupaPasswordActivity":
            case "VerifikasiKodeActivity":
            case "ResetPasswordActivity":
            case "LihatDataActivity":
                return null;

            default:
                // Default: activity lain tetap di-log dengan kategori OTHER
                return ActivityLogger.FITUR_OTHER;
        }
    }
}