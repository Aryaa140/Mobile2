package com.example.mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoriRealisasiActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private RecyclerView recyclerView;
    private TextView tvEmptyHistori;
    private HistoriRealisasiAdapter adapter;
    private List<HistoriRealisasi> historiList;
    private ApiService apiService;
    private int idRealisasi;

    private static final String TAG = "HistoriRealisasi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histori_realisasi);

        // Dapatkan ID realisasi dari intent
        idRealisasi = getIntent().getIntExtra("ID_REALISASI", -1);
        if (idRealisasi == -1) {
            Toast.makeText(this, "Error: ID Realisasi tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews();
        loadHistoriData();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        recyclerView = findViewById(R.id.recyclerHistori);
        tvEmptyHistori = findViewById(R.id.tvEmptyHistori);

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Setup recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historiList = new ArrayList<>();
        adapter = new HistoriRealisasiAdapter(historiList);
        recyclerView.setAdapter(adapter);
    }

    private void loadHistoriData() {
        Log.d(TAG, "🎯 Memuat data histori untuk ID Realisasi: " + idRealisasi);

        Call<HistoriRealisasiResponse> call = apiService.getHistoriRealisasi(idRealisasi);
        call.enqueue(new Callback<HistoriRealisasiResponse>() {
            @Override
            public void onResponse(Call<HistoriRealisasiResponse> call, Response<HistoriRealisasiResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        HistoriRealisasiResponse apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            List<HistoriRealisasi> data = apiResponse.getData();
                            if (data != null && !data.isEmpty()) {
                                // Update data adapter
                                adapter.updateData(data);

                                // Sembunyikan pesan kosong, tampilkan recyclerview
                                tvEmptyHistori.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);

                                Toast.makeText(HistoriRealisasiActivity.this,
                                        "✅ Data histori: " + data.size() + " items", Toast.LENGTH_SHORT).show();
                            } else {
                                showEmptyHistori();
                            }
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error";
                            Toast.makeText(HistoriRealisasiActivity.this, "❌ Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                            showEmptyHistori();
                        }
                    } else {
                        Toast.makeText(HistoriRealisasiActivity.this, "❌ HTTP Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        showEmptyHistori();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception in loadHistoriData", e);
                    Toast.makeText(HistoriRealisasiActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyHistori();
                }
            }

            @Override
            public void onFailure(Call<HistoriRealisasiResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Log.e(TAG, "API CALL FAILED: " + t.getMessage(), t);
                    Toast.makeText(HistoriRealisasiActivity.this, "❌ Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyHistori();
                });
            }
        });
    }

    private void showEmptyHistori() {
        runOnUiThread(() -> {
            adapter.updateData(new ArrayList<>());
            tvEmptyHistori.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoriData();
    }
}