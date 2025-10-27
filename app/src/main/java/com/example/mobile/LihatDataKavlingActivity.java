package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LihatDataKavlingActivity extends AppCompatActivity {
    private static final String TAG = "LihatDataKavlingActivity";

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;

    private KavlingAdapter kavlingAdapter;
    private List<KavlingWithInfo> kavlingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_kavling);

        Log.d(TAG, "=== ACTIVITY CREATED ===");

        // Inisialisasi views
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        // Validasi views
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView is NULL!");
            Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "All views initialized successfully");

        // Setup RecyclerView
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Inisialisasi adapter dengan list kosong
            kavlingAdapter = new KavlingAdapter(new ArrayList<>());
            recyclerView.setAdapter(kavlingAdapter);

            Log.d(TAG, "RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
            Toast.makeText(this, "Error setting up list", Toast.LENGTH_LONG).show();
        }

        setupToolbar();
        setupBottomNavigation();
        setupSearch();
        setupAdapterClickListener();

        // Load data ketika activity benar-benar started
        new android.os.Handler().postDelayed(() -> {
            loadKavlingData();
        }, 100);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "=== ACTIVITY STARTED ===");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity diresume
        loadKavlingData();
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataKavlingActivity.this, LihatDataActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

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
            return false;
        });
    }

    private void setupSearch() {
        if (searchEditText != null && kavlingAdapter != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Filter berdasarkan tipe hunian, proyek, hunian, atau status penjualan
                    kavlingAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupAdapterClickListener() {
        kavlingAdapter.setOnItemClickListener(new KavlingAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(KavlingWithInfo kavling) {
                // Intent ke EditDataKavlingActivity dengan data kavling
                Intent intent = new Intent(LihatDataKavlingActivity.this, EditDataKavlingActivity.class);
                intent.putExtra("KAVLING_DATA", kavling);
                startActivity(intent);
            }
            @Override
            public void onDeleteClick(KavlingWithInfo kavling) {
                showDeleteConfirmation(kavling);
            }
        });
    }

    private void showDeleteConfirmation(KavlingWithInfo kavling) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Kavling")
                .setMessage("Apakah Anda yakin ingin menghapus kavling ini?\n\n" +
                        "📋 Detail Kavling:\n" +
                        "• Tipe Hunian: " + kavling.getTipeHunian() + "\n" +
                        "• Proyek: " + kavling.getProyek() + "\n" +
                        "• Hunian: " + kavling.getHunian() + "\n" +
                        "• Status: " + kavling.getStatusPenjualan() + "\n\n" +
                        "ℹ️ INFORMASI:\n" +
                        "• Data userprospek TIDAK akan ikut terhapus\n" +
                        "• Penghapusan akan DIBATALKAN jika masih ada userprospek yang menggunakan tipe hunian ini\n" +
                        "• Tindakan ini TIDAK DAPAT DIBATALKAN!")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deleteKavling(kavling);
                })
                .setNegativeButton("Batal", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void deleteKavling(KavlingWithInfo kavling) {
        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<BasicResponse> call = apiService.deleteKavling(kavling.getIdKavling());

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse deleteResponse = response.body();
                        if (deleteResponse.isSuccess()) {
                            Toast.makeText(LihatDataKavlingActivity.this,
                                    "Kavling berhasil dihapus", Toast.LENGTH_SHORT).show();
                            // Refresh data setelah delete
                            loadKavlingData();
                        } else {
                            // Tampilkan pesan error dari server
                            String errorMessage = deleteResponse.getMessage();
                            if (errorMessage.contains("foreign key constraint") ||
                                    errorMessage.contains("constraint fails")) {
                                Toast.makeText(LihatDataKavlingActivity.this,
                                        "Tidak dapat menghapus: Masih ada data userprospek yang menggunakan kavling ini",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LihatDataKavlingActivity.this,
                                        "Gagal: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(LihatDataKavlingActivity.this,
                                "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Toast.makeText(LihatDataKavlingActivity.this,
                            "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void loadKavlingData() {
        Log.d(TAG, "=== LOADING KAVLING DATA ===");

        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<KavlingWithInfoResponse> call = apiService.getKavlingWithInfo();

            Log.d(TAG, "API Call URL: " + call.request().url());

            call.enqueue(new Callback<KavlingWithInfoResponse>() {
                @Override
                public void onResponse(Call<KavlingWithInfoResponse> call, Response<KavlingWithInfoResponse> response) {
                    Log.d(TAG, "=== API RESPONSE RECEIVED ===");
                    Log.d(TAG, "Response Code: " + response.code());
                    Log.d(TAG, "Response isSuccessful: " + response.isSuccessful());

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Response is successful");

                        if (response.body() != null) {
                            KavlingWithInfoResponse kavlingResponse = response.body();
                            Log.d(TAG, "Response Body Success: " + kavlingResponse.isSuccess());
                            Log.d(TAG, "Response Message: " + kavlingResponse.getMessage());
                            Log.d(TAG, "Response Total: " + kavlingResponse.getTotal());

                            if (kavlingResponse.getData() != null) {
                                Log.d(TAG, "Data from API: " + kavlingResponse.getData().size() + " items");

                                // Debug: print semua data dari API
                                for (int i = 0; i < kavlingResponse.getData().size(); i++) {
                                    KavlingWithInfo item = kavlingResponse.getData().get(i);
                                    Log.d(TAG, "API Data " + i + ": " + item.getTipeHunian() +
                                            " (Proyek: " + item.getProyek() +
                                            ", Hunian: " + item.getHunian() +
                                            ", Status: " + item.getStatusPenjualan() + ")");
                                }

                                if (kavlingResponse.isSuccess()) {
                                    updateUIWithData(kavlingResponse.getData());
                                } else {
                                    Log.w(TAG, "API returned success=false");
                                    Toast.makeText(LihatDataKavlingActivity.this, "Gagal: " + kavlingResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "Data from API is NULL");
                                Toast.makeText(LihatDataKavlingActivity.this, "Data null dari server", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Response body is NULL");
                            Toast.makeText(LihatDataKavlingActivity.this, "Response body null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response not successful: " + response.code());
                        Toast.makeText(LihatDataKavlingActivity.this, "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<KavlingWithInfoResponse> call, Throwable t) {
                    Log.e(TAG, "=== API CALL FAILED ===");
                    Log.e(TAG, "Network error: " + t.getMessage());
                    Toast.makeText(LihatDataKavlingActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in API call setup: " + e.getMessage());
            Toast.makeText(this, "Error sistem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIWithData(List<KavlingWithInfo> data) {
        runOnUiThread(() -> {
            try {
                Log.d(TAG, "Updating UI with data: " + data.size() + " items");

                if (kavlingAdapter == null) {
                    Log.e(TAG, "Adapter is null during UI update!");
                    kavlingAdapter = new KavlingAdapter(data);
                    if (recyclerView != null) {
                        recyclerView.setAdapter(kavlingAdapter);
                    }
                } else {
                    kavlingAdapter.updateData(data);
                }

                // Beri feedback ke user
                if (data.isEmpty()) {
                    Toast.makeText(this, "Tidak ada data kavling", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Data loaded: " + data.size() + " kavling", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "UI update completed successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI: " + e.getMessage());
                Toast.makeText(this, "Error menampilkan data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "=== ACTIVITY DESTROYED ===");
    }
}