package com.example.mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailProyekActivity extends AppCompatActivity {

    private static final String TAG = "DetailProyekActivity";

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private MaterialButton btnLihatUnit;
    private MaterialButton btnViewFull;

    // Views untuk Card 1 - Detail Proyek
    private ImageView imgProyek;
    private TextView txtNamaProyek;
    private TextView txtLokasiProyek;
    private TextView txtDeskripsiProyek;

    // Views untuk Card 2 - Fasilitas
    private RecyclerView recyclerViewFasilitas;
    private FasilitasAdapter fasilitasAdapter;
    private List<FasilitasItem> fasilitasList;

    // Views untuk Card 3 - Siteplan
    private ImageView imgSitePlan;
    private CardView cardSitePlan;

    // Data
    private int idProyek;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_detail_proyek);
            Log.d(TAG, "Layout inflated successfully");

            // HANYA ambil ID dari intent
            idProyek = getIntent().getIntExtra("ID_PROYEK", -1);
            Log.d(TAG, "Received ID: " + idProyek);

            apiService = RetrofitClient.getClient().create(ApiService.class);
            initViews();
            setupNavigation();

            // Load semua data dari API berdasarkan ID
            loadProyekData();

            Log.d(TAG, "Activity setup completed");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading page", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        try {
            topAppBar = findViewById(R.id.topAppBar);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            btnLihatUnit = findViewById(R.id.btnLihatUnit);
            btnViewFull = findViewById(R.id.btnViewFull);

            // Card 1 - Detail Proyek
            imgProyek = findViewById(R.id.imgProyek);
            txtNamaProyek = findViewById(R.id.txtNama);
            txtLokasiProyek = findViewById(R.id.txtLokasi);
            txtDeskripsiProyek = findViewById(R.id.txtDeskripsi);

            // Card 2 - Fasilitas
            recyclerViewFasilitas = findViewById(R.id.recyclerViewFasilitas);
            fasilitasList = new ArrayList<>();
            fasilitasAdapter = new FasilitasAdapter(fasilitasList);
            recyclerViewFasilitas.setLayoutManager(new GridLayoutManager(this, 1));
            recyclerViewFasilitas.setAdapter(fasilitasAdapter);

            // Card 3 - Siteplan
            imgSitePlan = findViewById(R.id.imgSitePlan);
            cardSitePlan = findViewById(R.id.cardSitePlanRiverside);

            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }

            Log.d(TAG, "Views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    private void loadProyekData() {
        if (idProyek == -1) {
            Toast.makeText(this, "ID Proyek tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading project data for ID: " + idProyek);

        // Load data proyek dari API
        Call<List<Proyek>> call = apiService.getAllProyek("getProyek");
        call.enqueue(new Callback<List<Proyek>>() {
            @Override
            public void onResponse(Call<List<Proyek>> call, Response<List<Proyek>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cari proyek dengan ID yang sesuai
                    Proyek selectedProyek = null;
                    for (Proyek proyek : response.body()) {
                        if (proyek.getIdProyek() == idProyek) {
                            selectedProyek = proyek;
                            break;
                        }
                    }

                    if (selectedProyek != null) {
                        setupData(selectedProyek);
                        loadFasilitasData(selectedProyek.getNamaProyek());
                    } else {
                        Toast.makeText(DetailProyekActivity.this, "Proyek tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(DetailProyekActivity.this, "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Proyek>> call, Throwable t) {
                Log.e(TAG, "Network error loading project: " + t.getMessage());
                Toast.makeText(DetailProyekActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupData(Proyek proyek) {
        try {
            // Set data untuk Card 1
            if (txtNamaProyek != null) txtNamaProyek.setText(proyek.getNamaProyek());
            if (txtLokasiProyek != null) txtLokasiProyek.setText(proyek.getLokasiProyek());
            if (txtDeskripsiProyek != null) txtDeskripsiProyek.setText(proyek.getDeskripsiProyek());

            // Set logo proyek
            if (proyek.getLogoBase64() != null && !proyek.getLogoBase64().isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(proyek.getLogoBase64(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgProyek.setImageBitmap(decodedByte);
                    Log.d(TAG, "Logo successfully set");
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding logo: " + e.getMessage());
                    imgProyek.setImageResource(R.drawable.ic_placeholder);
                }
            } else {
                Log.w(TAG, "Logo base64 is null or empty");
                imgProyek.setImageResource(R.drawable.ic_placeholder);
            }

            // Set siteplan
            if (proyek.getSiteplanBase64() != null && !proyek.getSiteplanBase64().isEmpty()) {
                Log.d(TAG, "Siteplan base64 length: " + proyek.getSiteplanBase64().length());
                try {
                    byte[] decodedString = Base64.decode(proyek.getSiteplanBase64(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgSitePlan.setImageBitmap(decodedByte);
                    cardSitePlan.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Siteplan successfully set and card shown");
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding siteplan: " + e.getMessage());
                    cardSitePlan.setVisibility(View.GONE);
                }
            } else {
                Log.w(TAG, "Siteplan base64 is null or empty, hiding card");
                cardSitePlan.setVisibility(View.GONE);
            }

            // Update toolbar title
            if (topAppBar != null) {
                topAppBar.setTitle("Detail " + proyek.getNamaProyek());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up data: " + e.getMessage(), e);
        }
    }

    private void loadFasilitasData(String namaProyek) {
        if (namaProyek == null || namaProyek.isEmpty()) {
            Log.e(TAG, "Nama proyek is null or empty");
            hideFasilitasCard();
            return;
        }

        Log.d(TAG, "Loading facilities for project: " + namaProyek);

        try {
            Call<List<FasilitasItem>> call = apiService.getFasilitasByProyek("getFasilitasByProyek", namaProyek);
            call.enqueue(new Callback<List<FasilitasItem>>() {
                @Override
                public void onResponse(Call<List<FasilitasItem>> call, Response<List<FasilitasItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            fasilitasList.clear();
                            fasilitasList.addAll(response.body());
                            fasilitasAdapter.notifyDataSetChanged();

                            Log.d(TAG, "Loaded " + fasilitasList.size() + " facilities");
                            showOrHideFasilitasCard();
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing facilities data: " + e.getMessage());
                            hideFasilitasCard();
                        }
                    } else {
                        Log.e(TAG, "Response error: " + response.code());
                        hideFasilitasCard();
                    }
                }

                @Override
                public void onFailure(Call<List<FasilitasItem>> call, Throwable t) {
                    Log.e(TAG, "Network error loading facilities: " + t.getMessage());
                    hideFasilitasCard();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadFasilitasData: " + e.getMessage());
            hideFasilitasCard();
        }
    }

    private void showOrHideFasilitasCard() {
        try {
            CardView cardFasilitas = findViewById(R.id.cardFasilitasSarana);
            if (cardFasilitas != null) {
                cardFasilitas.setVisibility(fasilitasList.isEmpty() ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing/hiding facilities card: " + e.getMessage());
        }
    }

    private void hideFasilitasCard() {
        try {
            CardView cardFasilitas = findViewById(R.id.cardFasilitasSarana);
            if (cardFasilitas != null) {
                cardFasilitas.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding facilities card: " + e.getMessage());
        }
    }

    // ... (setupClickListeners, setupNavigation, openFullScreenImage tetap sama)
    private void setupClickListeners() {
        try {
            // Button Lihat Unit
            if (btnLihatUnit != null) {
                btnLihatUnit.setOnClickListener(v -> {
                    try {
                        Toast.makeText(DetailProyekActivity.this, "Fitur Lihat Unit akan datang", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening UnitActivity: " + e.getMessage());
                        Toast.makeText(this, "Cannot open unit page", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Button View Full Site Plan
            if (btnViewFull != null) {
                btnViewFull.setOnClickListener(v -> openFullScreenImage());
            }

            // Image Site Plan juga bisa diklik untuk fullscreen
            if (imgSitePlan != null) {
                imgSitePlan.setOnClickListener(v -> openFullScreenImage());
            }

            Log.d(TAG, "Click listeners setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void setupNavigation() {
        try {
            // TopAppBar navigation
            if (topAppBar != null) {
                topAppBar.setNavigationOnClickListener(v -> {
                    try {
                        onBackPressed();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating back: " + e.getMessage());
                        finish();
                    }
                });
            }

            // Bottom Navigation
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    try {
                        if (id == R.id.nav_home) {
                            startActivity(new Intent(this, NewBeranda.class));
                            overridePendingTransition(0, 0);
                            return true;
                        } else if (id == R.id.nav_folder) {
                            startActivity(new Intent(this, LihatDataActivity.class));
                            overridePendingTransition(0, 0);
                            return true;
                        } else if (id == R.id.nav_news) {
                            startActivity(new Intent(this, NewsActivity.class));
                            overridePendingTransition(0, 0);
                            return true;
                        } else if (id == R.id.nav_profile) {
                            startActivity(new Intent(this, ProfileActivity.class));
                            overridePendingTransition(0, 0);
                            return true;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in bottom navigation: " + e.getMessage());
                    }
                    return false;
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage(), e);
        }
    }

    private void openFullScreenImage() {
        try {
            // Untuk sementara, beri pesan bahwa fitur akan datang
            Toast.makeText(this, "Fitur Full Screen Siteplan akan datang", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Full screen image feature clicked");
        } catch (Exception e) {
            Log.e(TAG, "Error opening full screen image: " + e.getMessage(), e);
            Toast.makeText(this, "Cannot open image viewer", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}