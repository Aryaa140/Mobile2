package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class LihatDataProspekActivity extends BaseActivity {

    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private ProspekAdapter2 adapter;
    private ArrayList<Prospek2> prospekList;
    private EditText searchEditText;
    private Spinner filterStatusSpinner;
    private SharedPreferences sharedPreferences;
    private String userLevel;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadDataFromMySQL();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data_prospek);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        userLevel = sharedPreferences.getString("level", "Operator");

        Log.d("LihatDataProspek", "User Level: " + userLevel);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchEditText = findViewById(R.id.searchEditText);
        filterStatusSpinner = findViewById(R.id.filterStatusSpinner);
        recyclerView = findViewById(R.id.recyclerProspek);

        prospekList = new ArrayList<>();

        if (userLevel == null) {
            userLevel = "Operator";
        }

        adapter = new ProspekAdapter2(this, prospekList, userLevel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup filter status spinner
        setupFilterSpinner();

        // Load data dari MySQL
        loadDataFromMySQL();

        // Setup pencarian
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterBySearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Update judul app bar berdasarkan level user
        updateAppBarTitle();

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataProspekActivity.this, LihatDataActivity.class);
            startActivity(intent);
            finish();
        });

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupFilterSpinner() {
        String[] statusOptions = {"Semua", "Cold", "Follow up", "Kunjungan", "Batal"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterStatusSpinner.setAdapter(spinnerAdapter);

        filterStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                adapter.filterByStatus(selectedStatus);

                int itemCount = adapter.getItemCount();
                String filterText = selectedStatus.equals("Semua") ?
                        "Menampilkan semua data (" + itemCount + " item)" :
                        "Menampilkan status: " + selectedStatus + " (" + itemCount + " item)";

                Toast.makeText(LihatDataProspekActivity.this, filterText, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                adapter.filterByStatus("Semua");
            }
        });
    }

    private void updateAppBarTitle() {
        if ("Admin".equals(userLevel)) {
            TopAppBar.setTitle("Semua Data Prospek");
        } else {
            TopAppBar.setTitle("Data Prospek Saya");
        }
    }

    private void loadDataFromMySQL() {
        String username = sharedPreferences.getString("username", "");
        Log.d("LihatDataProspek", "Loading data for username: " + username + ", level: " + userLevel);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProspekResponse> call = apiService.getProspekByPenginput(username, userLevel);

        call.enqueue(new Callback<ProspekResponse>() {
            @Override
            public void onResponse(Call<ProspekResponse> call, Response<ProspekResponse> response) {
                Log.d("LihatDataProspek", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ProspekResponse prospekResponse = response.body();
                    Log.d("LihatDataProspek", "Success: " + prospekResponse.isSuccess());

                    if (prospekResponse.isSuccess()) {
                        List<Prospek2> data = prospekResponse.getData();
                        Log.d("LihatDataProspek", "Data count: " + (data != null ? data.size() : "null"));

                        if (data != null) {
                            adapter.updateData(new ArrayList<>(data));
                            filterStatusSpinner.setSelection(0);

                            if (data.isEmpty()) {
                                if ("Admin".equals(userLevel)) {
                                    Toast.makeText(LihatDataProspekActivity.this,
                                            "Tidak ada data prospek di sistem", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LihatDataProspekActivity.this,
                                            "Tidak ada data prospek milik Anda", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String message = "Menampilkan ";
                                if ("Admin".equals(userLevel)) {
                                    message += "semua data (" + data.size() + " item)";
                                } else {
                                    message += "data milik Anda (" + data.size() + " item)";
                                }
                                Toast.makeText(LihatDataProspekActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(LihatDataProspekActivity.this,
                                "Gagal memuat data: " + prospekResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LihatDataProspekActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProspekResponse> call, Throwable t) {
                Log.e("LihatDataProspek", "Failure: " + t.getMessage(), t);
                Toast.makeText(LihatDataProspekActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromMySQL();
    }
}