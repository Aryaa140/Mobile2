package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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

public class StatusAkunActivity extends AppCompatActivity implements StatusAkunAdapter.OnStatusAkunActionListener {

    private MaterialToolbar topAppBar;
    private com.google.android.material.bottomnavigation.BottomNavigationView BottomNavigationView;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private StatusAkunAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_status_akun);

        initViews();
        setupRecyclerView();
        loadUserData();
        setupSearch();
        setupToolbar();

        BottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Navigation
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(StatusAkunActivity.this, NewBeranda.class);
            startActivity(intent);
            finish();
        });

        BottomNavigationView.setOnItemSelectedListener(item -> {
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerProspek);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StatusAkunAdapter(this, filteredList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(user);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty() && !query.isEmpty()) {
            Toast.makeText(this, "Tidak ditemukan user dengan username: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        Log.d("StatusAkunActivity", "Loading user data...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<UserResponse> call = apiService.getSemuaUser();

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if (userResponse.isSuccess()) {
                        userList.clear();
                        filteredList.clear();

                        // Filter hanya user dengan level Operator
                        for (User user : userResponse.getData()) {
                            if ("Operator".equals(user.getLevel())) {
                                userList.add(user);
                            }
                        }

                        filteredList.addAll(userList);
                        adapter.notifyDataSetChanged();

                        Log.d("StatusAkunActivity", "User data loaded: " + userList.size() + " Operator users");

                        if (userList.isEmpty()) {
                            Toast.makeText(StatusAkunActivity.this, "Tidak ada user dengan level Operator", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(StatusAkunActivity.this, "Gagal memuat data user: " + userResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StatusAkunActivity.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(StatusAkunActivity.this, "Gagal memuat data user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("StatusAkunActivity", "Load user error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onAktifkanUser(int userId, String username) {
        updateUserStatus(userId, username, "Aktif");
    }

    @Override
    public void onNonaktifkanUser(int userId, String username) {
        updateUserStatus(userId, username, "Nonaktif");
    }

    private void updateUserStatus(int userId, String username, String newStatus) {
        Log.d("StatusAkunActivity", "Updating user status - ID: " + userId + ", Username: " + username + ", New Status: " + newStatus);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updateStatusUser(userId, newStatus);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Toast.makeText(StatusAkunActivity.this, "Status " + username + " berhasil diubah menjadi " + newStatus, Toast.LENGTH_SHORT).show();

                        // Reload data untuk memperbarui tampilan
                        loadUserData();
                    } else {
                        Toast.makeText(StatusAkunActivity.this, "Gagal mengubah status: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StatusAkunActivity.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(StatusAkunActivity.this, "Gagal mengubah status: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("StatusAkunActivity", "Update status error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data setiap kali activity resume
        loadUserData();
    }
}