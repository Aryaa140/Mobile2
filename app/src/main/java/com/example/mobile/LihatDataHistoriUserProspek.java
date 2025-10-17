package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LihatDataHistoriUserProspek extends AppCompatActivity {

    private static final String TAG = "LihatDataHistori";
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ApiService apiService;
    private Toolbar toolbar;
    private HistoriUserProspekAdapter adapter;
    private List<HistoriUserProspek> historiList;
    private List<HistoriUserProspek> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_data_histori_user_prospek);

        Log.d(TAG, "Activity created");

        // Inisialisasi view dengan null safety
        try {
            toolbar = findViewById(R.id.topAppBar);
            searchEditText = findViewById(R.id.searchEditText);
            recyclerView = findViewById(R.id.recyclerProspek);

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            historiList = new ArrayList<>();
            filteredList = new ArrayList<>();
            adapter = new HistoriUserProspekAdapter(filteredList);
            recyclerView.setAdapter(adapter);

            // Setup toolbar
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Data Histori User Prospek");
            }

            toolbar.setNavigationOnClickListener(v -> {
                finish();
            });

            apiService = RetrofitClient.getClient().create(ApiService.class);

            // Ambil data dari intent
            Intent intent = getIntent();
            int userProspekId = intent.getIntExtra("USER_PROSPEK_ID", -1);
            String nama = intent.getStringExtra("NAMA");
            String penginput = intent.getStringExtra("PENGINPUT");

            // Debug log
            Log.d(TAG, "Data dari intent:");
            Log.d(TAG, "USER_PROSPEK_ID: " + userProspekId);
            Log.d(TAG, "NAMA: " + nama);
            Log.d(TAG, "PENGINPUT: " + penginput);

            // Load data histori
            if (userProspekId != -1) {
                loadHistoriData(userProspekId);
            } else {
                Toast.makeText(this, "Error: ID tidak valid", Toast.LENGTH_SHORT).show();
                finish();
            }

            // Search functionality
            setupSearchFunctionality();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupSearchFunctionality() {
        searchEditText.setOnClickListener(v -> {
            // Optional: Handle click on search icon if needed
        });

        // Jika ingin implementasi search real-time, tambahkan TextWatcher
        /*
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        */
    }

    private void filterData(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(historiList);
        } else {
            for (HistoriUserProspek histori : historiList) {
                if (histori.getNamaUser() != null &&
                        histori.getNamaUser().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(histori);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadHistoriData(int userProspekId) {
        Toast.makeText(this, "Memuat data histori...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Loading histori for ID: " + userProspekId);

        Call<HistoriUserProspekResponse> call = apiService.getHistoriUserProspek(
                "get_histori_userprospek",
                userProspekId
        );

        call.enqueue(new Callback<HistoriUserProspekResponse>() {
            @Override
            public void onResponse(Call<HistoriUserProspekResponse> call, Response<HistoriUserProspekResponse> response) {
                try {
                    Log.d(TAG, "Histori API Response Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        HistoriUserProspekResponse apiResponse = response.body();
                        Log.d(TAG, "Histori API Success: " + apiResponse.isSuccess());

                        if (apiResponse.isSuccess()) {
                            List<HistoriUserProspek> data = apiResponse.getData();

                            if (data != null && !data.isEmpty()) {
                                historiList.clear();
                                historiList.addAll(data);

                                filteredList.clear();
                                filteredList.addAll(historiList);
                                adapter.notifyDataSetChanged();

                                Toast.makeText(LihatDataHistoriUserProspek.this,
                                        "Data histori loaded: " + data.size() + " items",
                                        Toast.LENGTH_SHORT).show();

                                Log.d(TAG, "Histori data loaded successfully: " + data.size() + " items");
                            } else {
                                Toast.makeText(LihatDataHistoriUserProspek.this,
                                        "Tidak ada data histori", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "No histori data found");
                            }
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error";
                            Toast.makeText(LihatDataHistoriUserProspek.this,
                                    "Gagal memuat histori: " + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Histori API Error: " + errorMsg);
                        }
                    } else {
                        String errorBody = "Unknown error";
                        if (response.errorBody() != null) {
                            try {
                                errorBody = response.errorBody().string();
                            } catch (Exception e) {
                                errorBody = "Error reading error body";
                            }
                        }
                        Toast.makeText(LihatDataHistoriUserProspek.this,
                                "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Histori HTTP Error: " + response.code() + " - " + errorBody);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing histori response: " + e.getMessage());
                    Toast.makeText(LihatDataHistoriUserProspek.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HistoriUserProspekResponse> call, Throwable t) {
                Toast.makeText(LihatDataHistoriUserProspek.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Histori Network Error: " + t.getMessage(), t);
            }
        });
    }
}