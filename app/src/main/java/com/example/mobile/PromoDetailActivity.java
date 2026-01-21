package com.example.mobile;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromoDetailActivity extends AppCompatActivity {

    private static final String TAG = "PromoDetailActivity";
    private MaterialToolbar topAppBar;
    private ImageView imgPromoDetail;
    private TextView tvPromoTitle, tvPromoInputter, tvPromoReference,
            tvPromoDate, tvPromoExpiry;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private BroadcastReceiver promoUpdateReceiver;
    private int currentPromoId;
    private Promo currentPromo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_detail);
        EdgeToEdge.enable(this);

        initViews();
        setupToolbar();
        setupSwipeRefresh();
        loadPromoData();

        currentPromoId = getIntent().getIntExtra("PROMO_ID", -1);
        setupBroadcastReceiver();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        imgPromoDetail = findViewById(R.id.imgPromoDetail);
        tvPromoTitle = findViewById(R.id.tvPromoTitle);
        tvPromoInputter = findViewById(R.id.tvPromoInputter);
        tvPromoReference = findViewById(R.id.tvPromoReference);
        tvPromoDate = findViewById(R.id.tvPromoDate);
        tvPromoExpiry = findViewById(R.id.tvPromoExpiry);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        // Tambahkan menu untuk refresh
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_refresh) {
                refreshPromoData();
                return true;
            }
            return false;
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshPromoData();
        });
    }

    private void loadPromoData() {
        showLoading(true);

        try {
            // Ambil data dari intent
            int promoId = getIntent().getIntExtra("PROMO_ID", -1);
            String promoTitle = getIntent().getStringExtra("PROMO_TITLE");
            String promoInputter = getIntent().getStringExtra("PROMO_INPUTTER");
            String promoReference = getIntent().getStringExtra("PROMO_REFERENCE");
            String promoImage = getIntent().getStringExtra("PROMO_IMAGE");
            String promoDate = getIntent().getStringExtra("PROMO_DATE");
            String promoExpiry = getIntent().getStringExtra("PROMO_KADALUWARSA");

            Log.d(TAG, "Loading promo detail - ID: " + promoId + ", Title: " + promoTitle);

            // Simpan data untuk referensi
            currentPromo = new Promo();
            currentPromo.setIdPromo(promoId);
            currentPromo.setNamaPromo(promoTitle);
            currentPromo.setNamaPenginput(promoInputter);
            currentPromo.setReferensiProyek(promoReference);
            currentPromo.setGambarBase64(promoImage);
            currentPromo.setTanggalInput(promoDate);
            currentPromo.setKadaluwarsa(promoExpiry);

            // Tampilkan data
            updateUIWithData(currentPromo);

            showLoading(false);

        } catch (Exception e) {
            Log.e(TAG, "Error loading promo data: " + e.getMessage());
            Toast.makeText(this, "Error memuat detail promo", Toast.LENGTH_SHORT).show();
            showLoading(false);
        }
    }

    private void updateUIWithData(Promo promo) {
        try {
            if (promo.getNamaPromo() != null) {
                tvPromoTitle.setText(promo.getNamaPromo());
            }

            if (promo.getNamaPenginput() != null) {
                tvPromoInputter.setText("Ditambahkan oleh : " + promo.getNamaPenginput());
            }

            if (promo.getReferensiProyek() != null) {
                tvPromoReference.setText("Referensi Proyek : " + promo.getReferensiProyek());
            }

            if (promo.getTanggalInput() != null) {
                tvPromoDate.setText("Tanggal Input : " + formatDate(promo.getTanggalInput()));
            }

            if (promo.getKadaluwarsa() != null && !promo.getKadaluwarsa().equals("null") && !promo.getKadaluwarsa().isEmpty()) {
                tvPromoExpiry.setText("Tanggal Kadaluwarsa : " + formatDate(promo.getKadaluwarsa()));

                // Tampilkan warna berbeda jika promo sudah kadaluwarsa
                if (isPromoExpired(promo.getKadaluwarsa())) {
                    tvPromoExpiry.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    tvPromoExpiry.setTextColor(getResources().getColor(R.color.black));
                }
            } else {
                tvPromoExpiry.setText("Kadaluwarsa: Tidak ditentukan");
            }

            // Load gambar
            loadPromoImage(promo.getGambarBase64());

        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
        }
    }

    private void loadPromoImage(String imageData) {
        if (imageData != null && !imageData.isEmpty() && !imageData.equals("null")) {
            try {
                byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    imgPromoDetail.setImageBitmap(bitmap);
                } else {
                    imgPromoDetail.setImageResource(R.drawable.ic_placeholder);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading promo image: " + e.getMessage());
                imgPromoDetail.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            imgPromoDetail.setImageResource(R.drawable.ic_placeholder);
        }
    }

    private String formatDate(String dateString) {
        try {
            if (dateString == null || dateString.isEmpty()) {
                return "Tidak diketahui";
            }

            // Jika dateString mengandung waktu, ambil hanya tanggalnya
            if (dateString.contains(" ")) {
                dateString = dateString.split(" ")[0];
            }

            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault());

            java.util.Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString; // Kembalikan asli jika error
        }
    }

    private boolean isPromoExpired(String expiryDate) {
        try {
            if (expiryDate == null || expiryDate.isEmpty() || expiryDate.equals("null")) {
                return false;
            }

            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date expiry = format.parse(expiryDate);
            java.util.Date today = new java.util.Date();

            // Reset waktu untuk perbandingan yang akurat
            java.util.Calendar calExpiry = java.util.Calendar.getInstance();
            calExpiry.setTime(expiry);
            calExpiry.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calExpiry.set(java.util.Calendar.MINUTE, 0);
            calExpiry.set(java.util.Calendar.SECOND, 0);
            calExpiry.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar calToday = java.util.Calendar.getInstance();
            calToday.setTime(today);
            calToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calToday.set(java.util.Calendar.MINUTE, 0);
            calToday.set(java.util.Calendar.SECOND, 0);
            calToday.set(java.util.Calendar.MILLISECOND, 0);

            return calToday.after(calExpiry);
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void setupBroadcastReceiver() {
        promoUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getStringExtra("ACTION");
                    int updatedPromoId = intent.getIntExtra("PROMO_ID", -1);

                    // Jika promo yang sedang dilihat di-update
                    if (("PROMO_UPDATED".equals(action) || "NEW_PROMO_ADDED".equals(action))
                            && updatedPromoId == currentPromoId) {

                        Log.d(TAG, "Promo detail needs refresh - ID: " + currentPromoId);

                        // Tampilkan notifikasi
                        runOnUiThread(() -> {
                            Toast.makeText(PromoDetailActivity.this,
                                    "Data promo diperbarui, memuat ulang...",
                                    Toast.LENGTH_SHORT).show();
                        });

                        // Refresh data dari server
                        refreshPromoData();
                    }
                }
            }
        };

        // Register receiver dengan penanganan untuk Android 13+
        IntentFilter filter = new IntentFilter("REFRESH_NEWS_DATA");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Untuk Android 13+ (API 33+)
            registerReceiver(promoUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // Untuk Android di bawah 13
            registerReceiver(promoUpdateReceiver, filter);
        }
    }

    private void refreshPromoData() {
        if (currentPromoId != -1) {
            showLoading(true);

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<PromoResponse> call = apiService.getSemuaPromo();

            call.enqueue(new Callback<PromoResponse>() {
                @Override
                public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.isSuccessful() && response.body() != null) {
                        PromoResponse promoResponse = response.body();
                        if (promoResponse.isSuccess() && promoResponse.getData() != null) {

                            // Cari promo dengan ID yang sesuai
                            boolean found = false;
                            for (Promo promo : promoResponse.getData()) {
                                if (promo.getIdPromo() == currentPromoId) {
                                    currentPromo = promo;
                                    updateUIWithNewData(promo);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                // Promo mungkin sudah dihapus
                                Toast.makeText(PromoDetailActivity.this,
                                        "Promo tidak ditemukan (mungkin sudah dihapus)",
                                        Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(PromoDetailActivity.this,
                                    "Gagal memuat data promo",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PromoDetailActivity.this,
                                "Error response dari server",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PromoResponse> call, Throwable t) {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);

                    Log.e(TAG, "Error refreshing promo data: " + t.getMessage());
                    Toast.makeText(PromoDetailActivity.this,
                            "Error koneksi: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateUIWithNewData(Promo promo) {
        runOnUiThread(() -> {
            try {
                updateUIWithData(promo);
                Toast.makeText(PromoDetailActivity.this,
                        "Data promo diperbarui", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e(TAG, "Error updating UI: " + e.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        runOnUiThread(() -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister receiver
        if (promoUpdateReceiver != null) {
            unregisterReceiver(promoUpdateReceiver);
        }
    }
}