package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

public class HistoriStatusLibActivity extends AppCompatActivity {

    private static final String TAG = "HistoriActivity";

    private MaterialToolbar topAppBar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Button btnHapusSemua;

    private HistoriStatusLibAdapter adapter;
    private List<HistoriStatusLib> historiList = new ArrayList<>();

    private int idProspek = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histori_status_lib);

        // DEBUG: Cek intent yang masuk
        Intent intent = getIntent();
        if (intent != null) {
            idProspek = intent.getIntExtra("ID_PROSPEK", -1);
            Log.d(TAG, "==========================================");
            Log.d(TAG, "ON CREATE - ID_PROSPEK dari Intent: " + idProspek);

            // Cek semua extras
            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Log.d(TAG, "Extra Key: " + key + " = " + extras.get(key));
                }
            } else {
                Log.d(TAG, "Tidak ada extras di intent!");
            }
        } else {
            Log.d(TAG, "Intent NULL!");
        }

        initViews();
        setupRecyclerView();
        loadHistoriData();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        recyclerView = findViewById(R.id.recyclerViewHistori);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnHapusSemua = findViewById(R.id.btnHapusSemua);

        topAppBar.setNavigationOnClickListener(v -> finish());
        btnHapusSemua.setOnClickListener(v -> showDeleteAllConfirmation());

        // Set judul berdasarkan apakah ada ID Prospek atau tidak
        if (idProspek != -1) {
            topAppBar.setTitle("Histori Prospek #" + idProspek);
        } else {
            topAppBar.setTitle("Semua Histori");
        }
    }

    private void setupRecyclerView() {
        adapter = new HistoriStatusLibAdapter(this, historiList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadHistoriData() {
        showLoading(true);
        Log.d(TAG, "loadHistoriData() dipanggil, idProspek = " + idProspek);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<HistoriStatusLibResponse> call;

        if (idProspek != -1) {
            // PASTIKAN ID Prospek valid
            Log.d(TAG, "Memanggil API getHistoriStatusLibByProspek dengan id_prospek = " + idProspek);
            call = apiService.getHistoriStatusLibByProspek(idProspek);
        } else {
            Log.d(TAG, "Memanggil API getAllHistoriStatusLib (semua histori)");
            call = apiService.getAllHistoriStatusLib();
        }

        call.enqueue(new Callback<HistoriStatusLibResponse>() {
            @Override
            public void onResponse(Call<HistoriStatusLibResponse> call, Response<HistoriStatusLibResponse> response) {
                showLoading(false);
                Log.d(TAG, "onResponse dipanggil");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    HistoriStatusLibResponse historiResponse = response.body();
                    Log.d(TAG, "Response body success: " + historiResponse.isSuccess());
                    Log.d(TAG, "Response message: " + historiResponse.getMessage());

                    if (historiResponse.isSuccess()) {
                        historiList.clear();

                        if (historiResponse.getData() != null && !historiResponse.getData().isEmpty()) {
                            int count = historiResponse.getData().size();
                            Log.d(TAG, "Jumlah data diterima: " + count);

                            // DEBUG: Tampilkan data pertama
                            if (count > 0) {
                                HistoriStatusLib firstItem = historiResponse.getData().get(0);
                                Log.d(TAG, "Data pertama:");
                                Log.d(TAG, "  ID Histori: " + firstItem.getIdHistoriLib());
                                Log.d(TAG, "  ID Prospek: " + firstItem.getIdProspek());
                                Log.d(TAG, "  Nama Prospek: " + firstItem.getNamaProspek());
                                Log.d(TAG, "  Perubahan: " + firstItem.getPerubahanStatus());
                            }

                            historiList.addAll(historiResponse.getData());
                            adapter.notifyDataSetChanged();

                            tvEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            // Update tombol hapus semua
                            btnHapusSemua.setVisibility(View.VISIBLE);
                            if (idProspek != -1) {
                                btnHapusSemua.setText("Hapus Histori Prospek Ini");
                            }
                        } else {
                            Log.d(TAG, "Data kosong dari API");
                            tvEmptyState.setText("Tidak ada data histori");
                            tvEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            btnHapusSemua.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(TAG, "Response tidak success: " + historiResponse.getMessage());
                        showToast("Gagal: " + historiResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "Response tidak sukses atau body null");
                    showToast("Error response server");
                }
            }

            @Override
            public void onFailure(Call<HistoriStatusLibResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                showToast("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    private void deleteAllHistori() {
        if (historiList.isEmpty()) {
            showToast("Tidak ada histori untuk dihapus");
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.deleteAllHistoriStatusLib();

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    historiList.clear();
                    adapter.notifyDataSetChanged();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    btnHapusSemua.setVisibility(View.GONE);
                    showToast("Semua histori berhasil dihapus");
                } else {
                    showToast("Gagal menghapus histori");
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showToast("Koneksi gagal: " + t.getMessage());
            }
        });
    }

    private void showDeleteAllConfirmation() {
        if (historiList.isEmpty()) {
            showToast("Tidak ada histori untuk dihapus");
            return;
        }

        String message;
        if (idProspek != -1) {
            message = "Hapus " + historiList.size() + " histori untuk prospek ini?";
        } else {
            message = "Hapus semua " + historiList.size() + " histori?";
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage(message)
                .setPositiveButton("Hapus", (dialog, which) -> deleteAllHistori())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}