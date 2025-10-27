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

public class LihatDataHunianActivity extends AppCompatActivity {
    private static final String TAG = "LihatDataHunianActivity";

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;

    private HunianAdapter hunianAdapter;
    private List<HunianWithInfo> hunianList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_hunian);

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
            hunianAdapter = new HunianAdapter(new ArrayList<>());
            recyclerView.setAdapter(hunianAdapter);

            Log.d(TAG, "RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
            Toast.makeText(this, "Error setting up list", Toast.LENGTH_LONG).show();
        }

        setupToolbar();
        setupBottomNavigation();
        setupSearch();
        setupAdapterClickListener();

        // JANGAN load data di onCreate, tunggu sampai view fully created
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "=== ACTIVITY STARTED ===");

        // Load data ketika activity benar-benar started
        new android.os.Handler().postDelayed(() -> {
            loadHunianData();
        }, 100); // Delay kecil untuk memastikan UI fully loaded
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataHunianActivity.this, LihatDataActivity.class);
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
        if (searchEditText != null && hunianAdapter != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // ✅ FITUR SEARCH: Filter berdasarkan nama hunian
                    hunianAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupAdapterClickListener() {
        hunianAdapter.setOnItemClickListener(new HunianAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(HunianWithInfo hunian) {
                Intent intent = new Intent(LihatDataHunianActivity.this, EditDataHunianActivity.class);
                intent.putExtra("HUNIAN_DATA", hunian);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(HunianWithInfo hunian) {
                showDeleteConfirmation(hunian);
            }
        });
    }

    private void showDeleteConfirmation(HunianWithInfo hunian) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Hunian")
                .setMessage("Apakah Anda yakin ingin menghapus hunian \"" + hunian.getNamaHunian() + "\"?\n\n" +
                        "📋 Detail Hunian:\n" +
                        "• Nama: " + hunian.getNamaHunian() + "\n" +
                        "• Proyek: " + hunian.getNamaProyek() + "\n" +
                        "• Jumlah Tipe: " + hunian.getJumlahTipeHunian() + "\n" +
                        "• Total Stok: " + hunian.getJumlahStok() + "\n\n" +
                        "⚠️ PERHATIAN: Tindakan ini akan menghapus:\n" +
                        "• Data hunian \"" + hunian.getNamaHunian() + "\"\n" +
                        "• Semua data kavling terkait (" + hunian.getJumlahStok() + " kavling)\n\n" +
                        "ℹ️ INFORMASI:\n" +
                        "• Penghapusan akan DIBATALKAN jika masih ada data userprospek yang menggunakan hunian ini\n" +
                        "• Data proyek dan promo TIDAK akan terpengaruh\n" +
                        "• Tindakan ini TIDAK DAPAT DIBATALKAN!")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deleteHunian(hunian);
                })
                .setNegativeButton("Batal", null)
                .show();
    }
    private void deleteHunian(HunianWithInfo hunian) {
        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<BasicResponse> call = apiService.deleteHunian(
                    hunian.getIdHunian(),
                    hunian.getNamaHunian(),
                    hunian.getNamaProyek()
            );

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse deleteResponse = response.body();
                        if (deleteResponse.isSuccess()) {
                            Toast.makeText(LihatDataHunianActivity.this,
                                    "Hunian berhasil dihapus", Toast.LENGTH_SHORT).show();
                            // Refresh data setelah delete
                            loadHunianData();
                        } else {
                            Toast.makeText(LihatDataHunianActivity.this,
                                    "Gagal: " + deleteResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LihatDataHunianActivity.this,
                                "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Toast.makeText(LihatDataHunianActivity.this,
                            "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHunianData() {
        Log.d(TAG, "=== LOADING HUNIAN DATA ===");

        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<HunianWithInfoResponse> call = apiService.getHunianWithInfo();

            Log.d(TAG, "API Call URL: " + call.request().url());

            call.enqueue(new Callback<HunianWithInfoResponse>() {
                @Override
                public void onResponse(Call<HunianWithInfoResponse> call, Response<HunianWithInfoResponse> response) {
                    Log.d(TAG, "=== API RESPONSE RECEIVED ===");
                    Log.d(TAG, "Response Code: " + response.code());
                    Log.d(TAG, "Response isSuccessful: " + response.isSuccessful());

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Response is successful");

                        if (response.body() != null) {
                            HunianWithInfoResponse hunianResponse = response.body();
                            Log.d(TAG, "Response Body Success: " + hunianResponse.isSuccess());
                            Log.d(TAG, "Response Message: " + hunianResponse.getMessage());
                            Log.d(TAG, "Response Total: " + hunianResponse.getTotal());

                            if (hunianResponse.getData() != null) {
                                Log.d(TAG, "Data from API: " + hunianResponse.getData().size() + " items");

                                // Debug: print semua data dari API
                                for (int i = 0; i < hunianResponse.getData().size(); i++) {
                                    HunianWithInfo item = hunianResponse.getData().get(i);
                                    Log.d(TAG, "API Data " + i + ": " + item.getNamaHunian() +
                                            " (Proyek: " + item.getNamaProyek() +
                                            ", Tipe: " + item.getJumlahTipeHunian() +
                                            ", Stok: " + item.getJumlahStok() + ")");
                                }

                                if (hunianResponse.isSuccess()) {
                                    updateUIWithData(hunianResponse.getData());
                                } else {
                                    Log.w(TAG, "API returned success=false");
                                    Toast.makeText(LihatDataHunianActivity.this, "Gagal: " + hunianResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "Data from API is NULL");
                                Toast.makeText(LihatDataHunianActivity.this, "Data null dari server", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Response body is NULL");
                            Toast.makeText(LihatDataHunianActivity.this, "Response body null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response not successful: " + response.code());
                        Toast.makeText(LihatDataHunianActivity.this, "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<HunianWithInfoResponse> call, Throwable t) {
                    Log.e(TAG, "=== API CALL FAILED ===");
                    Log.e(TAG, "Network error: " + t.getMessage());
                    Toast.makeText(LihatDataHunianActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in API call setup: " + e.getMessage());
            Toast.makeText(this, "Error sistem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIWithData(List<HunianWithInfo> data) {
        runOnUiThread(() -> {
            try {
                Log.d(TAG, "Updating UI with data: " + data.size() + " items");

                if (hunianAdapter == null) {
                    Log.e(TAG, "Adapter is null during UI update!");
                    hunianAdapter = new HunianAdapter(data);
                    if (recyclerView != null) {
                        recyclerView.setAdapter(hunianAdapter);
                    }
                } else {
                    hunianAdapter.updateData(data);
                }

                // Beri feedback ke user
                if (data.isEmpty()) {
                    Toast.makeText(this, "Tidak ada data hunian", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Data loaded: " + data.size() + " hunian", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "UI update completed successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI: " + e.getMessage());
                Toast.makeText(this, "Error menampilkan data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity diresume
        loadHunianData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "=== ACTIVITY DESTROYED ===");
    }
}