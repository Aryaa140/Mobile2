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

public class LihatDataProyekActivity extends AppCompatActivity {

    private static final String TAG = "LihatDataProyekActivity";
    private List<ProyekWithInfo> proyekListFull = new ArrayList<>();

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;

    private ProyekAdapter proyekAdapter;
    private List<ProyekWithInfo> proyekList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_proyek);

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
            proyekAdapter = new ProyekAdapter(new ArrayList<>());
            recyclerView.setAdapter(proyekAdapter);
            setupAdapterClickListener();
            Log.d(TAG, "RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
            Toast.makeText(this, "Error setting up list", Toast.LENGTH_LONG).show();
        }

        setupToolbar();
        setupBottomNavigation();
        setupSearch();

        // JANGAN load data di onCreate, tunggu sampai view fully created
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "=== ACTIVITY STARTED ===");

        // Load data ketika activity benar-benar started
        new android.os.Handler().postDelayed(() -> {
            loadProyekData();
        }, 100); // Delay kecil untuk memastikan UI fully loaded
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataProyekActivity.this, LihatDataActivity.class);
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
        Log.d(TAG, "Setting up search functionality");

        if (searchEditText != null) {
            searchEditText.setHint("Cari nama proyek...");
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Filter data ketika teks berubah
                    filterProyekData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } else {
            Log.e(TAG, "Search EditText is null");
        }
    }

    private void filterProyekData(String searchText) {
        try {
            List<ProyekWithInfo> filteredList = new ArrayList<>();

            if (searchText.isEmpty()) {
                // Jika search kosong, tampilkan semua data
                filteredList.addAll(proyekListFull);
            } else {
                // Filter berdasarkan nama proyek (case insensitive)
                String searchPattern = searchText.toLowerCase().trim();
                for (ProyekWithInfo proyek : proyekListFull) {
                    if (proyek.getNamaProyek().toLowerCase().contains(searchPattern)) {
                        filteredList.add(proyek);
                    }
                }
            }

            // Update adapter dengan data yang sudah difilter
            updateAdapterWithFilteredData(filteredList);

        } catch (Exception e) {
            Log.e(TAG, "Error filtering data: " + e.getMessage());
        }
    }

    private void updateAdapterWithFilteredData(List<ProyekWithInfo> filteredList) {
        runOnUiThread(() -> {
            try {
                if (proyekAdapter != null) {
                    proyekAdapter.updateData(filteredList);

                    // Optional: Beri feedback jumlah hasil pencarian
                    if (!searchEditText.getText().toString().isEmpty()) {
                        String message = "Ditemukan " + filteredList.size() + " proyek";
                        if (filteredList.isEmpty()) {
                            message = "Tidak ditemukan proyek dengan nama '" +
                                    searchEditText.getText().toString() + "'";
                        }
                        // Bisa tambahkan Toast atau text indicator di sini jika perlu
                        Log.d(TAG, message);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating filtered data: " + e.getMessage());
            }
        });
    }

    private void updateUIWithData(List<ProyekWithInfo> data) {
        runOnUiThread(() -> {
            try {
                Log.d(TAG, "Updating UI with data: " + data.size() + " items");

                // SIMPAN DATA FULL untuk keperluan pencarian
                proyekListFull.clear();
                proyekListFull.addAll(data);

                if (proyekAdapter == null) {
                    Log.e(TAG, "Adapter is null during UI update!");
                    proyekAdapter = new ProyekAdapter(data);
                    if (recyclerView != null) {
                        recyclerView.setAdapter(proyekAdapter);
                    }
                } else {
                    proyekAdapter.updateData(data);
                }

                // Beri feedback ke user
                if (data.isEmpty()) {
                    Toast.makeText(this, "Tidak ada data proyek", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Data loaded: " + data.size() + " proyek", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "UI update completed successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI: " + e.getMessage());
                Toast.makeText(this, "Error menampilkan data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadProyekData() {
        Log.d(TAG, "=== LOADING PROYEK DATA ===");

        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<ProyekWithInfoResponse> call = apiService.getProyekWithInfo();

            Log.d(TAG, "API Call URL: " + call.request().url());

            call.enqueue(new Callback<ProyekWithInfoResponse>() {
                @Override
                public void onResponse(Call<ProyekWithInfoResponse> call, Response<ProyekWithInfoResponse> response) {
                    Log.d(TAG, "=== API RESPONSE RECEIVED ===");
                    Log.d(TAG, "Response Code: " + response.code());
                    Log.d(TAG, "Response isSuccessful: " + response.isSuccessful());

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Response is successful");

                        if (response.body() != null) {
                            ProyekWithInfoResponse proyekResponse = response.body();
                            Log.d(TAG, "Response Body Success: " + proyekResponse.isSuccess());
                            Log.d(TAG, "Response Message: " + proyekResponse.getMessage());
                            Log.d(TAG, "Response Total: " + proyekResponse.getTotal());

                            if (proyekResponse.getData() != null) {
                                Log.d(TAG, "Data from API: " + proyekResponse.getData().size() + " items");

                                // Debug: print semua data dari API
                                for (int i = 0; i < proyekResponse.getData().size(); i++) {
                                    ProyekWithInfo item = proyekResponse.getData().get(i);
                                    Log.d(TAG, "API Data " + i + ": " + item.getNamaProyek() +
                                            " (Hunian: " + item.getJumlahHunian() +
                                            ", Stok: " + item.getJumlahStok() + ")");
                                }

                                if (proyekResponse.isSuccess()) {
                                    updateUIWithData(proyekResponse.getData());
                                } else {
                                    Log.w(TAG, "API returned success=false");
                                    Toast.makeText(LihatDataProyekActivity.this, "Gagal: " + proyekResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "Data from API is NULL");
                                Toast.makeText(LihatDataProyekActivity.this, "Data null dari server", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Response body is NULL");
                            Toast.makeText(LihatDataProyekActivity.this, "Response body null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response not successful: " + response.code());
                        Toast.makeText(LihatDataProyekActivity.this, "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ProyekWithInfoResponse> call, Throwable t) {
                    Log.e(TAG, "=== API CALL FAILED ===");
                    Log.e(TAG, "Network error: " + t.getMessage());
                    Toast.makeText(LihatDataProyekActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in API call setup: " + e.getMessage());
            Toast.makeText(this, "Error sistem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
// Di dalam class LihatDataProyekActivity, tambahkan method ini:

    private void setupAdapterClickListener() {
        proyekAdapter.setOnItemClickListener(new ProyekAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(ProyekWithInfo proyek) {
                Intent intent = new Intent(LihatDataProyekActivity.this, EditDataProyekActivity.class);
                intent.putExtra("ID_PROYEK", proyek.getIdProyek());
                intent.putExtra("NAMA_PROYEK", proyek.getNamaProyek());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(ProyekWithInfo proyek) {
                showDeleteConfirmation(proyek);
            }
        });
    }

    private void showDeleteConfirmation(ProyekWithInfo proyek) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Proyek")
                .setMessage("Apakah Anda yakin ingin menghapus proyek \"" + proyek.getNamaProyek() + "\"?\n\n" +
                        "📋 Detail Proyek:\n" +
                        "• Nama: " + proyek.getNamaProyek() + "\n" +
                        "• Jumlah Hunian: " + proyek.getJumlahHunian() + "\n" +
                        "• Total Stok: " + proyek.getJumlahStok() + "\n\n" +
                        "⚠️ PERHATIAN: Tindakan ini akan menghapus:\n" +
                        "• Data proyek \"" + proyek.getNamaProyek() + "\"\n" +
                        "• Semua data hunian terkait (" + proyek.getJumlahHunian() + " hunian)\n" +
                        "• Semua data kavling terkait (" + proyek.getJumlahStok() + " kavling)\n" +
                        "• Semua data promo terkait\n\n" +
                        "ℹ️ INFORMASI:\n" +
                        "• Penghapusan akan DIBATALKAN jika masih ada data userprospek yang menggunakan proyek ini\n" +
                        "• Tindakan ini TIDAK DAPAT DIBATALKAN!")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deleteProyek(proyek);
                })
                .setNegativeButton("Batal", null)
                .show();
    }
    private void deleteProyek(ProyekWithInfo proyek) {
        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<BasicResponse> call = apiService.deleteProyek(
                    proyek.getIdProyek(),
                    proyek.getNamaProyek()
            );

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse deleteResponse = response.body();
                        if (deleteResponse.isSuccess()) {
                            Toast.makeText(LihatDataProyekActivity.this,
                                    "Proyek berhasil dihapus", Toast.LENGTH_SHORT).show();
                            // Refresh data setelah delete
                            loadProyekData();
                        } else {
                            Toast.makeText(LihatDataProyekActivity.this,
                                    "Gagal: " + deleteResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LihatDataProyekActivity.this,
                                "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Toast.makeText(LihatDataProyekActivity.this,
                            "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity diresume
        loadProyekData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "=== ACTIVITY DESTROYED ===");
    }
}