package com.example.mobile;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LihatDataUserpActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private UserProspekAdapter adapter;
    private List<UserProspekSimple> userProspekList;
    private List<UserProspekSimple> filteredList;
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private String userLevel; // Simpan level (Admin, Operator, dll)
    private String userName;  // Simpan username untuk parameter penginput
    private ApiService apiService;
    private static final String TAG = "LihatDataUserp";

    // FIX: Gunakan KEY yang sama dengan MainActivity
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LEVEL = "level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_userp);

        // Inisialisasi Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // FIX: Ambil data dari SharedPreferences yang benar
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userLevel = prefs.getString(KEY_LEVEL, "");  // Level: Admin, Operator, dll
        userName = prefs.getString(KEY_USERNAME, ""); // Username: untuk parameter penginput

        // DEBUG: Cek SharedPreferences
        Log.d(TAG, "=== SHARED PREFERENCES DEBUG ===");
        Log.d(TAG, "Prefs Name: " + PREFS_NAME);
        Log.d(TAG, "All stored data: " + prefs.getAll().toString());
        Log.d(TAG, "User Level: '" + userLevel + "'");
        Log.d(TAG, "User Name: '" + userName + "'");

        // Inisialisasi view
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userProspekList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserProspekAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // Load data
        loadUserProspekData();

        // Navigation
        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataUserpActivity.this, LihatDataActivity.class);
            startActivity(intent);
            finish();
        });

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

        // Search functionality
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserProspekData() {
        // FIX: Logic parameter berdasarkan LEVEL bukan ROLE
        String penginputParam;

        if (userLevel.equals("Admin")) {
            // Admin bisa lihat semua data
            penginputParam = "all";
        } else {
            // Operator, Freelance, Inhouse hanya lihat data mereka sendiri
            penginputParam = userName; // Username sebagai filter
        }

        Log.d(TAG, "🎯 === LOAD DATA PARAMETERS ===");
        Log.d(TAG, "🎯 User Level: " + userLevel);
        Log.d(TAG, "🎯 User Name: " + userName);
        Log.d(TAG, "🎯 Parameter dikirim: " + penginputParam);

        Call<UserProspekSimpleResponse> call = apiService.getUserProspekSimpleData("getUserProspekSimple", penginputParam);
        call.enqueue(new Callback<UserProspekSimpleResponse>() {
            @Override
            public void onResponse(Call<UserProspekSimpleResponse> call, Response<UserProspekSimpleResponse> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG, "🎯 === API RESPONSE ===");
                            Log.d(TAG, "🎯 Response code: " + response.code());

                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    UserProspekSimpleResponse apiResponse = response.body();
                                    Log.d(TAG, "🎯 API Success: " + apiResponse.isSuccess());
                                    Log.d(TAG, "🎯 API Message: " + apiResponse.getMessage());

                                    if (apiResponse.isSuccess()) {
                                        List<UserProspekSimple> data = apiResponse.getData();

                                        Log.d(TAG, "🎯 Data reference: " + data);
                                        Log.d(TAG, "🎯 Data == null: " + (data == null));

                                        if (data != null) {
                                            Log.d(TAG, "🎯 Data.size(): " + data.size());
                                            Log.d(TAG, "🎯 Data.isEmpty(): " + data.isEmpty());

                                            // DEBUG: Log first few items
                                            if (data.size() > 0) {
                                                for (int i = 0; i < Math.min(data.size(), 3); i++) {
                                                    UserProspekSimple item = data.get(i);
                                                    Log.d(TAG, "🎯 Item " + i + ": " + item.getNama() + " by " + item.getPenginput());
                                                }
                                            }

                                            if (!data.isEmpty()) {
                                                userProspekList.clear();
                                                userProspekList.addAll(data);

                                                filteredList.clear();
                                                filteredList.addAll(userProspekList);

                                                if (adapter != null) {
                                                    adapter.notifyDataSetChanged();
                                                }

                                                String message = userLevel.equals("Admin") ?
                                                        "✅ Data semua user: " + userProspekList.size() + " items" :
                                                        "✅ Data Anda: " + userProspekList.size() + " items";

                                                Toast.makeText(LihatDataUserpActivity.this, message, Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "🎯 SUCCESS: Data loaded to UI - " + userProspekList.size() + " items");
                                            } else {
                                                Log.d(TAG, "📭 Data list is EMPTY");
                                                userProspekList.clear();
                                                filteredList.clear();
                                                if (adapter != null) {
                                                    adapter.notifyDataSetChanged();
                                                }

                                                String message = userLevel.equals("Admin") ?
                                                        "📭 Tidak ada data prospek untuk semua user" :
                                                        "📭 Tidak ada data prospek untuk user: " + userName;

                                                Toast.makeText(LihatDataUserpActivity.this, message, Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Log.d(TAG, "❌ Data is NULL from API");
                                            Toast.makeText(LihatDataUserpActivity.this, "❌ Data null dari API", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error";
                                        Log.e(TAG, "❌ API Error: " + errorMsg);
                                        Toast.makeText(LihatDataUserpActivity.this, "❌ Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.e(TAG, "❌ Response body is NULL");
                                    Toast.makeText(LihatDataUserpActivity.this, "❌ Response body null", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "❌ HTTP Error: " + response.code());
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                    Log.e(TAG, "❌ Error body: " + errorBody);
                                } catch (Exception e) {
                                    Log.e(TAG, "❌ Error reading error body: " + e.getMessage());
                                }
                                Toast.makeText(LihatDataUserpActivity.this, "❌ HTTP Error: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Exception in onResponse", e);
                            Toast.makeText(LihatDataUserpActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "🎯 === END DEBUG ===");
                    }
                });
            }

            @Override
            public void onFailure(Call<UserProspekSimpleResponse> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "❌ API CALL FAILED: " + t.getMessage(), t);
                        Toast.makeText(LihatDataUserpActivity.this, "❌ Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void filterData(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userProspekList);
        } else {
            for (UserProspekSimple userProspek : userProspekList) {
                if (userProspek.getNama() != null && userProspek.getNama().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(userProspek);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private String formatCurrency(int amount) {
        try {
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            formatter.applyPattern("#,###");
            return formatter.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    private void showEditDialog(UserProspekSimple userProspek) {
        // FIX: Cek level untuk hak akses edit
        if (!userLevel.equals("Admin")) {
            Toast.makeText(this, "Hanya Admin yang dapat mengedit data", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(LihatDataUserpActivity.this, EditDataUserpActivity.class);
        intent.putExtra("USER_PROSPEK_ID", userProspek.getId());
        intent.putExtra("PENGINPUT", userProspek.getPenginput());
        intent.putExtra("NAMA", userProspek.getNama());
        intent.putExtra("EMAIL", userProspek.getEmail());
        intent.putExtra("NO_HP", userProspek.getNohp());
        intent.putExtra("ALAMAT", userProspek.getAlamat());
        intent.putExtra("PROYEK", userProspek.getProyek());
        intent.putExtra("HUNIAN", userProspek.getHunian());
        intent.putExtra("TIPE_HUNIAN", userProspek.getTipeHunian());
        intent.putExtra("DP", userProspek.getDp());
        intent.putExtra("STATUS_BPJS", userProspek.getBpjs());
        intent.putExtra("STATUS_NPWP", userProspek.getNpwp());
        startActivityForResult(intent, 1);
    }

    private void showDeleteConfirmation(UserProspekSimple userProspek) {
        // FIX: Cek level untuk hak akses delete
        if (!userLevel.equals("Admin")) {
            Toast.makeText(this, "Hanya Admin yang dapat menghapus data", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus data " + userProspek.getNama() + "?")
                .setPositiveButton("Ya", (dialog, which) -> {
                   // deleteUserProspek(userProspek);
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

   /* private void deleteUserProspek(UserProspekSimple userProspek) {
        Call<BasicResponse> call = apiService.deleteProspekByData(
                userProspek.getPenginput(),
                userProspek.getNama()
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isSuccess()) {
                                Toast.makeText(LihatDataUserpActivity.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                                loadUserProspekData();
                            } else {
                                Toast.makeText(LihatDataUserpActivity.this,
                                        "Gagal menghapus data: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LihatDataUserpActivity.this, "Gagal menghapus data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LihatDataUserpActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadUserProspekData();
        }
    }

    // Adapter class dengan null safety
    private class UserProspekAdapter extends RecyclerView.Adapter<UserProspekAdapter.ViewHolder> {

        private List<UserProspekSimple> userProspekList;

        public UserProspekAdapter(List<UserProspekSimple> userProspekList) {
            this.userProspekList = userProspekList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_userp, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            UserProspekSimple userProspek = userProspekList.get(position);

            // Null safety untuk semua field
            holder.tvPenginput.setText("Penginput: " + (userProspek.getPenginput() != null ? userProspek.getPenginput() : "-"));
            holder.tvTanggal.setText("Tanggal: " + (userProspek.getTanggal() != null ? formatDate(userProspek.getTanggal()) : "-"));
            holder.tvNama.setText("Nama: " + (userProspek.getNama() != null ? userProspek.getNama() : "-"));
            holder.tvEmail.setText("Email: " + (userProspek.getEmail() != null ? userProspek.getEmail() : "-"));
            holder.tvNoHp.setText("No. HP: " + (userProspek.getNohp() != null ? userProspek.getNohp() : "-"));
            holder.tvAlamat.setText("Alamat: " + (userProspek.getAlamat() != null ? userProspek.getAlamat() : "-"));

            String formattedDP = "DP: Rp " + formatCurrency(userProspek.getDp());
            holder.tvJumlahUangTandaJadi.setText(formattedDP);
            // TAMPILKAN HUNIAN
            holder.tvHunian.setText("Hunian: " + (userProspek.getHunian() != null ? userProspek.getHunian() : "-"));

            // TAMPILKAN TIPE HUNIAN
            holder.tvTipeHunian.setText("Tipe Hunian: " + (userProspek.getTipeHunian() != null ? userProspek.getTipeHunian() : "-"));

            // FIX: Tampilkan edit button hanya untuk Admin
            if (holder.btnEdit != null) {
                if (userLevel.equals("Admin")) {
                    holder.btnEdit.setVisibility(View.VISIBLE);
                    holder.btnEdit.setOnClickListener(v -> showEditDialog(userProspek));
                } else {
                    holder.btnEdit.setVisibility(View.GONE);
                }
            }
            if (holder.btnHistori != null) {
                holder.btnHistori.setVisibility(View.VISIBLE);
                holder.btnHistori.setOnClickListener(v -> {
                    try {
                        // Intent ke halaman histori - PERBAIKI NAMA ACTIVITY
                        Intent intent = new Intent(LihatDataUserpActivity.this, LihatDataHistoriUserProspek.class);

                        // Kirim data yang diperlukan
                        intent.putExtra("USER_PROSPEK_ID", userProspek.getId());
                        intent.putExtra("NAMA", userProspek.getNama());
                        intent.putExtra("PENGINPUT", userProspek.getPenginput());

                        // Debug log
                        Log.d(TAG, "🎯 Button Histori diklik:");
                        Log.d(TAG, "🎯 USER_PROSPEK_ID: " + userProspek.getId());
                        Log.d(TAG, "🎯 NAMA: " + userProspek.getNama());
                        Log.d(TAG, "🎯 PENGINPUT: " + userProspek.getPenginput());

                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error saat membuka histori: " + e.getMessage());
                        Toast.makeText(LihatDataUserpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> showDeleteConfirmation(userProspek));
            }
        }

        @Override
        public int getItemCount() {
            return userProspekList != null ? userProspekList.size() : 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPenginput, tvTanggal, tvNama, tvEmail, tvNoHp, tvAlamat, tvJumlahUangTandaJadi, tvHunian, tvTipeHunian;
            MaterialButton btnEdit, btnDelete,btnHistori;

            public ViewHolder(View itemView) {
                super(itemView);
                tvPenginput = itemView.findViewById(R.id.tvPenginput);
                tvTanggal = itemView.findViewById(R.id.tvTanggal);
                tvNama = itemView.findViewById(R.id.tvNama);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvNoHp = itemView.findViewById(R.id.tvNoHp);
                tvAlamat = itemView.findViewById(R.id.tvAlamat);
                tvHunian = itemView.findViewById(R.id.tvHunian); // TAMBAHAN
                tvTipeHunian = itemView.findViewById(R.id.tvTipeHunian); // TAMBAHAN
                tvJumlahUangTandaJadi = itemView.findViewById(R.id.tvJumlahUangTandaJadi);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnHistori = itemView.findViewById(R.id.btnHistori);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProspekData();
    }
}