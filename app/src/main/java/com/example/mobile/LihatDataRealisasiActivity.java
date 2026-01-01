package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LihatDataRealisasiActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private RealisasiAdapter adapter;
    private List<Realisasi> realisasiList;
    private List<Realisasi> filteredList;
    private ApiService apiService;
    private String userLevel;

    private static final String TAG = "LihatDataRealisasi";
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_LEVEL = "level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_data_realisasi);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Ambil level user dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userLevel = prefs.getString(KEY_LEVEL, "");

        topAppBar = findViewById(R.id.topAppBar);
        recyclerView = findViewById(R.id.recyclerRealisasi);
        searchEditText = findViewById(R.id.searchEditText);

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        // Setup recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        realisasiList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new RealisasiAdapter(filteredList, userLevel);
        recyclerView.setAdapter(adapter);

        // Load data realisasi
        loadRealisasiData();

        // Setup search functionality
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
    }

    private void loadRealisasiData() {
        Log.d(TAG, "🎯 Memuat data realisasi...");

        // Tentukan parameter penginput berdasarkan level user
        String penginputParam;
        if ("Admin".equals(userLevel)) {
            penginputParam = "all";
        } else {
            // Ambil username dari SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String userName = prefs.getString("username", "");
            penginputParam = userName;
        }

        Log.d(TAG, "User Level: " + userLevel);
        Log.d(TAG, "Penginput Parameter: " + penginputParam);

        Call<RealisasiResponse> call = apiService.getRealisasiData(penginputParam);
        call.enqueue(new Callback<RealisasiResponse>() {
            @Override
            public void onResponse(Call<RealisasiResponse> call, Response<RealisasiResponse> response) {
                runOnUiThread(() -> {
                    try {
                        Log.d(TAG, "Response Code: " + response.code());

                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                RealisasiResponse apiResponse = response.body();
                                Log.d(TAG, "API Success: " + apiResponse.isSuccess());
                                Log.d(TAG, "API Message: " + apiResponse.getMessage());

                                if (apiResponse.isSuccess()) {
                                    List<Realisasi> data = apiResponse.getData();

                                    if (data != null) {
                                        Log.d(TAG, "Data.size(): " + data.size());

                                        realisasiList.clear();
                                        realisasiList.addAll(data);

                                        filteredList.clear();
                                        filteredList.addAll(realisasiList);
                                        adapter.notifyDataSetChanged();

                                        String message = "Admin".equals(userLevel) ?
                                                "✅ Data semua realisasi: " + realisasiList.size() + " items" :
                                                "✅ Data realisasi Anda: " + realisasiList.size() + " items";

                                        Toast.makeText(LihatDataRealisasiActivity.this, message, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "Data is NULL from API");
                                        showNoDataMessage();
                                    }
                                } else {
                                    String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error";
                                    Log.e(TAG, "API Error: " + errorMsg);
                                    Toast.makeText(LihatDataRealisasiActivity.this, "❌ Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                                    showNoDataMessage();
                                }
                            } else {
                                Log.e(TAG, "Response body is NULL");
                                Toast.makeText(LihatDataRealisasiActivity.this, "❌ Response body null", Toast.LENGTH_SHORT).show();
                                showNoDataMessage();
                            }
                        } else {
                            Log.e(TAG, "HTTP Error: " + response.code());
                            Toast.makeText(LihatDataRealisasiActivity.this, "❌ HTTP Error: " + response.code(), Toast.LENGTH_SHORT).show();
                            showNoDataMessage();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception in onResponse", e);
                        Toast.makeText(LihatDataRealisasiActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showNoDataMessage();
                    }
                });
            }

            @Override
            public void onFailure(Call<RealisasiResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Log.e(TAG, "API CALL FAILED: " + t.getMessage(), t);
                    Toast.makeText(LihatDataRealisasiActivity.this, "❌ Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    showNoDataMessage();
                });
            }
        });
    }

    private void showNoDataMessage() {
        realisasiList.clear();
        filteredList.clear();
        adapter.notifyDataSetChanged();

        String message = "Admin".equals(userLevel) ?
                "📭 Tidak ada data realisasi untuk semua user" :
                "📭 Tidak ada data realisasi untuk user: " + userLevel;

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void filterData(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(realisasiList);
        } else {
            for (Realisasi realisasi : realisasiList) {
                if (realisasi.getNamaUser() != null && realisasi.getNamaUser().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(realisasi);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    // Di LihatDataRealisasiActivity.java - UBAH DARI PRIVATE KE PUBLIC
    public void showEditRealisasiDialog(Realisasi realisasi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Tanggal Realisasi");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_realisasi, null);
        builder.setView(dialogView);

        TextView tvNamaUser = dialogView.findViewById(R.id.tvNamaUser);
        TextView tvTanggalSebelumnya = dialogView.findViewById(R.id.tvTanggalSebelumnya);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        MaterialButton btnSimpanEdit = dialogView.findViewById(R.id.btnSimpanEdit);
        MaterialButton btnBatalEdit = dialogView.findViewById(R.id.btnBatalEdit);

        // Set data sebelumnya
        tvNamaUser.setText(realisasi.getNamaUser() != null ? realisasi.getNamaUser() : "-");
        tvTanggalSebelumnya.setText(realisasi.getTanggalRealisasi() != null ?
                formatDateForDisplay(realisasi.getTanggalRealisasi()) : "-");

        // Set tanggal sebelumnya di DatePicker jika ada
        if (realisasi.getTanggalRealisasi() != null && !realisasi.getTanggalRealisasi().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(realisasi.getTanggalRealisasi());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                datePicker.updateDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );
            } catch (Exception e) {
                Log.e(TAG, "Error parsing previous date: " + e.getMessage());
                // Jika gagal, gunakan tanggal hari ini
                Calendar cal = Calendar.getInstance();
                datePicker.updateDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );
            }
        }

        AlertDialog dialog = builder.create();

        btnSimpanEdit.setOnClickListener(v -> {
            // Get selected date
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            // Format tanggal untuk database
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String tanggalRealisasiBaru = dateFormat.format(calendar.getTime());

            // Panggil API untuk update realisasi
            updateRealisasi(realisasi.getId(), tanggalRealisasiBaru);
            dialog.dismiss();
        });

        btnBatalEdit.setOnClickListener(v -> dialog.dismiss());

        dialog.show();



    }

    private void updateRealisasi(int idRealisasi, String tanggalRealisasiBaru) {
        Log.d(TAG, "🎯 Mengupdate realisasi ID: " + idRealisasi);
        Log.d(TAG, "Tanggal baru: " + tanggalRealisasiBaru);

        Call<BasicResponse> call = apiService.updateRealisasi(idRealisasi, tanggalRealisasiBaru);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            BasicResponse apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(LihatDataRealisasiActivity.this,
                                        "✅ Tanggal realisasi berhasil diupdate!", Toast.LENGTH_LONG).show();
                                // Refresh data setelah berhasil update
                                loadRealisasiData();
                            } else {
                                String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error";
                                Toast.makeText(LihatDataRealisasiActivity.this,
                                        "❌ Gagal update: " + errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LihatDataRealisasiActivity.this,
                                    "❌ Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception in update response", e);
                        Toast.makeText(LihatDataRealisasiActivity.this,
                                "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    Log.e(TAG, "API CALL FAILED: " + t.getMessage(), t);
                    Toast.makeText(LihatDataRealisasiActivity.this,
                            "❌ Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String formatDateForDisplay(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "-";
        }

        try {
            // Coba format dengan time
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            try {
                // Coba format tanpa time
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception ex) {
                // Jika semua gagal, return string asli
                return dateString;
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadRealisasiData();
    }
}